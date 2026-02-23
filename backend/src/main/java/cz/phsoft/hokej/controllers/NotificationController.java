package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.NotificationBadgeDTO;
import cz.phsoft.hokej.models.dto.NotificationDTO;
import cz.phsoft.hokej.models.services.notification.NotificationQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
 * Veškerá logika čtení a změny stavu notifikací
 * je delegována do NotificationQueryService.
 * Controller pracuje vždy s aktuálně přihlášeným uživatelem.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationQueryService notificationQueryService;

    public NotificationController(NotificationQueryService notificationQueryService) {
        this.notificationQueryService = notificationQueryService;
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
        NotificationBadgeDTO dto = notificationQueryService.getBadge(authentication);
        return ResponseEntity.ok(dto);
    }

    /**
     * Vrací seznam notifikací vytvořených po posledním přihlášení uživatele.
     *
     * Pokud uživatel nemá lastLoginAt, použije se výchozí časové okno
     * definované v servisní vrstvě.
     *
     * @param authentication autentizační kontext aktuálního uživatele
     * @return seznam notifikací ve formě DTO
     */
    @GetMapping("/since-last-login")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDTO>> getSinceLastLogin(Authentication authentication) {
        List<NotificationDTO> dtos = notificationQueryService.getSinceLastLogin(authentication);
        return ResponseEntity.ok(dtos);
    }

    /**
     * Vrací poslední notifikace aktuálního uživatele.
     *
     * Parametr limit určuje maximální počet vrácených záznamů.
     * Pokud není zadán, použije se výchozí hodnota 50
     * (závisí na implementaci servisní vrstvy).
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
        List<NotificationDTO> dtos = notificationQueryService.getRecent(authentication, limit);
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
        notificationQueryService.markAsRead(authentication, id);
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
        notificationQueryService.markAllAsRead(authentication);
        return ResponseEntity.noContent().build();
    }

    /**
     * Vrací všechny notifikace v systému pro administrátorský přehled.
     *
     * Endpoint je určen pro role ADMIN a MANAGER.
     * Parametr limit omezuje maximální počet vrácených záznamů.
     *
     * @param limit maximální počet vrácených záznamů
     * @return seznam notifikací ve formě DTO
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<NotificationDTO>> getAllNotifications(
            @RequestParam(name = "limit", required = false, defaultValue = "500") int limit
    ) {
        List<NotificationDTO> dtos = notificationQueryService.getAllNotifications(limit);
        return ResponseEntity.ok(dtos);
    }
}

