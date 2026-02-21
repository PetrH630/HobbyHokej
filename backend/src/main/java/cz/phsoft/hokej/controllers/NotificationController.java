package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.NotificationEntity;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.data.repositories.NotificationRepository;
import cz.phsoft.hokej.exceptions.UserNotFoundException;
import cz.phsoft.hokej.models.dto.NotificationBadgeDTO;
import cz.phsoft.hokej.models.dto.NotificationDTO;
import cz.phsoft.hokej.models.mappers.NotificationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * REST controller pro práci s aplikačními notifikacemi.
 *
 * Poskytuje endpointy pro:
 * - výpočet badge (počet nepřečtených notifikací),
 * - načtení notifikací od posledního přihlášení,
 * - načtení posledních notifikací,
 * - označení notifikací jako přečtených.
 *
 * Veškerá logika čtení je delegována do NotificationRepository
 * a mapování do NotificationMapper. Controller pracuje vždy
 * s aktuálně přihlášeným uživatelem.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    /**
     * Výchozí počet dní, za které se načtou notifikace,
     * pokud uživatel ještě nemá nastavené lastLoginAt.
     */
    private static final int DEFAULT_DAYS_IF_NO_LAST_LOGIN = 14;

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final AppUserRepository appUserRepository;
    private final Clock clock;

    public NotificationController(NotificationRepository notificationRepository,
                                  NotificationMapper notificationMapper,
                                  AppUserRepository appUserRepository,
                                  Clock clock) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.appUserRepository = appUserRepository;
        this.clock = clock;
    }

    /**
     * Vrací badge s počtem nepřečtených notifikací od posledního přihlášení.
     *
     * Endpoint se používá například pro zobrazení čísla u ikony zvonku
     * v navigaci aplikace.
     *
     * @param authentication autentizační kontext aktuálního uživatele
     * @return DTO s informacemi o badge
     */
    @GetMapping("/badge")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationBadgeDTO> getBadge(Authentication authentication) {
        AppUserEntity user = getCurrentUser(authentication);
        Instant boundary = resolveBoundary(user);

        long count = notificationRepository
                .countByUserAndCreatedAtAfterAndReadAtIsNull(user, boundary);

        NotificationBadgeDTO dto = new NotificationBadgeDTO();
        dto.setUnreadCountSinceLastLogin(count);
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setCurrentLoginAt(user.getCurrentLoginAt());

        return ResponseEntity.ok(dto);
    }

    /**
     * Vrací seznam notifikací vytvořených po posledním přihlášení uživatele.
     *
     * Pokud uživatel nemá lastLoginAt, použije se výchozí časové okno
     * definované konstantou DEFAULT_DAYS_IF_NO_LAST_LOGIN.
     *
     * @param authentication autentizační kontext aktuálního uživatele
     * @return seznam notifikací ve formě DTO
     */
    @GetMapping("/since-last-login")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDTO>> getSinceLastLogin(Authentication authentication) {
        AppUserEntity user = getCurrentUser(authentication);
        Instant boundary = resolveBoundary(user);

        List<NotificationEntity> entities =
                notificationRepository.findByUserAndCreatedAtAfterOrderByCreatedAtDesc(user, boundary);

        List<NotificationDTO> dtos = notificationMapper.toDtoList(entities);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Vrací poslední notifikace aktuálního uživatele.
     *
     * Parametr limit určuje maximální počet vrácených záznamů.
     * Pokud není zadán, použije se výchozí hodnota 50
     * (závisí na implementaci NotificationRepository).
     *
     * @param authentication autentizační kontext aktuálního uživatele
     * @param limit volitelný limit počtu záznamů
     * @return seznam notifikací ve formě DTO
     */
    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDTO>> getRecent(
            Authentication authentication,
            @RequestParam(name = "limit", required = false, defaultValue = "50") int limit
    ) {
        AppUserEntity user = getCurrentUser(authentication);

        List<NotificationEntity> entities =
                notificationRepository.findTop50ByUserOrderByCreatedAtDesc(user);

        if (limit > 0 && entities.size() > limit) {
            entities = entities.subList(0, limit);
        }

        List<NotificationDTO> dtos = notificationMapper.toDtoList(entities);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Označí konkrétní notifikaci jako přečtenou.
     *
     * Operace je idempotentní – pokud je notifikace již přečtená
     * nebo neexistuje, nevyvolá se chyba.
     *
     * @param authentication autentizační kontext aktuálního uživatele
     * @param id identifikátor notifikace
     * @return HTTP 204 v případě úspěchu
     */
    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(Authentication authentication,
                                           @PathVariable("id") Long id) {
        AppUserEntity user = getCurrentUser(authentication);

        notificationRepository.findByIdAndUser(id, user)
                .ifPresent(entity -> {
                    if (entity.getReadAt() == null) {
                        entity.setReadAt(Instant.now(clock));
                        notificationRepository.save(entity);
                        log.debug("Notifikace {} označena jako přečtená pro user {}", id, user.getId());
                    }
                });

        return ResponseEntity.noContent().build();
    }

    /**
     * Označí všechny notifikace aktuálního uživatele jako přečtené.
     *
     * @param authentication autentizační kontext aktuálního uživatele
     * @return HTTP 204 v případě úspěchu
     */
    @PostMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        AppUserEntity user = getCurrentUser(authentication);

        List<NotificationEntity> unread =
                notificationRepository.findByUserAndReadAtIsNullOrderByCreatedAtDesc(user);

        if (!unread.isEmpty()) {
            Instant now = Instant.now(clock);
            for (NotificationEntity entity : unread) {
                entity.setReadAt(now);
            }
            notificationRepository.saveAll(unread);
            log.debug("Označeno {} notifikací jako přečtených pro user {}", unread.size(), user.getId());
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Určuje časovou hranici pro výběr notifikací.
     *
     * Pokud má uživatel nastaven lastLoginAt, použije se tato hodnota.
     * Jinak se použije aktuální čas mínus DEFAULT_DAYS_IF_NO_LAST_LOGIN dní.
     */
    private Instant resolveBoundary(AppUserEntity user) {
        if (user.getLastLoginAt() != null) {
            return user.getLastLoginAt();
        }
        return Instant.now(clock).minus(DEFAULT_DAYS_IF_NO_LAST_LOGIN, ChronoUnit.DAYS);
    }

    /**
     * Načte entitu aktuálního uživatele podle e-mailu
     * z autentizačního kontextu.
     *
     * @param authentication autentizační kontext
     * @return entita uživatele
     */
    private AppUserEntity getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
}