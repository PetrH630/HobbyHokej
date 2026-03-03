package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.notifications.entities.NotificationEntity;
import cz.phsoft.hokej.user.repositories.AppUserRepository;
import cz.phsoft.hokej.notifications.repositories.NotificationRepository;
import cz.phsoft.hokej.user.exceptions.UserNotFoundException;
import cz.phsoft.hokej.notifications.dto.NotificationBadgeDTO;
import cz.phsoft.hokej.notifications.dto.NotificationDTO;
import cz.phsoft.hokej.notifications.mappers.NotificationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Implementace služby pro čtení a správu aplikačních notifikací
 * z pohledu aktuálně přihlášeného uživatele.
 *
 * Odpovědnost třídy spočívá v poskytování dotazovacích operací nad notifikacemi,
 * včetně výpočtu badge, získávání seznamů notifikací a označování notifikací
 * jako přečtených. Třída pracuje vždy s uživatelem odvozeným z Authentication
 * kontextu.
 *
 * Práce s perzistentní vrstvou je delegována do NotificationRepository
 * a načítání uživatele je delegováno do AppUserRepository.
 * Transformace entit na DTO je zajištěna NotificationMapper.
 */
@Service
public class NotificationQueryServiceImpl implements NotificationQueryService {

    private static final Logger log = LoggerFactory.getLogger(NotificationQueryServiceImpl.class);

    /**
     * Výchozí počet dní, za které se načtou notifikace,
     * pokud uživatel ještě nemá nastavené lastLoginAt.
     */
    private static final int DEFAULT_DAYS_IF_NO_LAST_LOGIN = 14;

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final AppUserRepository appUserRepository;
    private final Clock clock;

    /**
     * Vytváří instanci služby pro práci s notifikacemi.
     *
     * Závislosti na repository, mapperu a hodinách jsou předány konstruktorem,
     * aby mohla být služba jednoduše testována a konfigurována v rámci Spring kontextu.
     *
     * @param notificationRepository repository pro práci s entitami notifikací
     * @param notificationMapper mapper pro převod entit na DTO
     * @param appUserRepository repository pro práci s entitami uživatelů
     * @param clock systémové hodiny používané pro práci s časem
     */
    public NotificationQueryServiceImpl(NotificationRepository notificationRepository,
                                        NotificationMapper notificationMapper,
                                        AppUserRepository appUserRepository,
                                        Clock clock) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.appUserRepository = appUserRepository;
        this.clock = clock;
    }

    /**
     * Vrací badge s počtem nepřečtených notifikací od časové hranice
     * odvozené z posledního přihlášení uživatele.
     *
     * Uživatel je identifikován z Authentication kontextu. Časová hranice
     * je určena metodou resolveBoundary a následně je v NotificationRepository
     * spočítán počet nepřečtených notifikací. Výsledek je vrácen jako DTO
     * doplněné o informace o časech přihlášení.
     *
     * @param authentication autentizační kontext aktuálně přihlášeného uživatele
     * @return DTO s informacemi o počtu nepřečtených notifikací a časech přihlášení
     */
    @Override
    public NotificationBadgeDTO getBadge(Authentication authentication) {
        AppUserEntity user = getCurrentUser(authentication);
        Instant boundary = resolveBoundary(user);

        long count = notificationRepository
                .countByUserAndCreatedAtAfterAndReadAtIsNull(user, boundary);

        NotificationBadgeDTO dto = new NotificationBadgeDTO();
        dto.setUnreadCountSinceLastLogin(count);
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setCurrentLoginAt(user.getCurrentLoginAt());

        return dto;
    }

    /**
     * Vrací seznam notifikací vytvořených po časové hranici
     * odvozené z posledního přihlášení uživatele.
     *
     * Uživatel je identifikován z Authentication kontextu a časová hranice
     * je určena metodou resolveBoundary. Notifikace jsou načteny z
     * NotificationRepository v sestupném pořadí podle vytvoření a převedeny
     * na DTO pomocí NotificationMapper.
     *
     * @param authentication autentizační kontext aktuálně přihlášeného uživatele
     * @return seznam notifikací ve formě DTO
     */
    @Override
    public List<NotificationDTO> getSinceLastLogin(Authentication authentication) {
        AppUserEntity user = getCurrentUser(authentication);
        Instant boundary = resolveBoundary(user);

        List<NotificationEntity> entities =
                notificationRepository.findByUserAndCreatedAtAfterOrderByCreatedAtDesc(user, boundary);

        return notificationMapper.toDtoList(entities);
    }

    /**
     * Vrací omezený seznam posledních notifikací aktuálního uživatele.
     *
     * Uživatel je identifikován z Authentication kontextu. Nejvýše 50
     * nejnovějších notifikací je načteno z NotificationRepository a následně
     * je aplikováno dodatečné omezení pomocí parametru limit.
     * Výsledek je převeden na DTO pomocí NotificationMapper.
     *
     * @param authentication autentizační kontext aktuálně přihlášeného uživatele
     * @param limit maximální počet vrácených notifikací; pokud je hodnota menší nebo rovna nule,
     *              použije se maximálně dostupný počet
     * @return seznam posledních notifikací ve formě DTO
     */
    @Override
    public List<NotificationDTO> getRecent(Authentication authentication, int limit) {
        AppUserEntity user = getCurrentUser(authentication);

        List<NotificationEntity> entities =
                notificationRepository.findTop50ByUserOrderByCreatedAtDesc(user);

        if (limit > 0 && entities.size() > limit) {
            entities = entities.subList(0, limit);
        }

        return notificationMapper.toDtoList(entities);
    }

    /**
     * Označí konkrétní notifikaci aktuálního uživatele jako přečtenou.
     *
     * Uživatel je identifikován z Authentication kontextu a notifikace je
     * vyhledána v NotificationRepository podle jejího identifikátoru a uživatele.
     * Pokud je notifikace nalezena a ještě nemá nastaven readAt, je jí nastaven
     * aktuální čas a změna je uložena. Operace je idempotentní a v případě,
     * že je notifikace již přečtená, nedochází k žádným změnám.
     *
     * @param authentication autentizační kontext aktuálně přihlášeného uživatele
     * @param id identifikátor notifikace
     */
    @Override
    public void markAsRead(Authentication authentication, Long id) {
        AppUserEntity user = getCurrentUser(authentication);

        notificationRepository.findByIdAndUser(id, user)
                .ifPresent(entity -> {
                    if (entity.getReadAt() == null) {
                        entity.setReadAt(Instant.now(clock));
                        notificationRepository.save(entity);
                        log.debug("Notifikace {} označena jako přečtená pro user {}", id, user.getId());
                    }
                });
    }

    /**
     * Označí všechny nepřečtené notifikace aktuálního uživatele jako přečtené.
     *
     * Uživatel je identifikován z Authentication kontextu.
     * Všechny notifikace bez hodnoty readAt jsou načteny z NotificationRepository,
     * je jim hromadně nastaven aktuální čas a změny jsou uloženy pomocí saveAll.
     * Operace je navržena tak, aby minimalizovala počet databázových operací.
     *
     * @param authentication autentizační kontext aktuálně přihlášeného uživatele
     */
    @Override
    public void markAllAsRead(Authentication authentication) {
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
    }

    /**
     * Vrací omezený seznam všech notifikací v systému pro administrativní přehled.
     *
     * Notifikace jsou načteny z NotificationRepository v sestupném pořadí podle
     * času vytvoření. Výsledný seznam může být omezen parametrem limit.
     * Výstup je vrácen ve formě DTO pro další zpracování nebo zobrazení.
     *
     * @param limit maximální počet vrácených notifikací; pokud je hodnota menší nebo rovna nule,
     *              použije se maximálně dostupný počet
     * @return seznam všech notifikací ve formě DTO
     */
    @Override
    public List<NotificationDTO> getAllNotifications(int limit) {
        List<NotificationEntity> entities = notificationRepository.findAllByOrderByCreatedAtDesc();

        if (limit > 0 && entities.size() > limit) {
            entities = entities.subList(0, limit);
        }

        return notificationMapper.toDtoList(entities);
    }

    /**
     * Určuje časovou hranici pro výběr notifikací aktuálního uživatele.
     *
     * Pokud má uživatel nastaven lastLoginAt, použije se tato hodnota jako
     * dolní hranice pro výběr notifikací. V opačném případě je hranice
     * odvozena od aktuálního času odečtením DEFAULT_DAYS_IF_NO_LAST_LOGIN dní.
     *
     * Metoda se používá v dotazovacích operacích nad notifikacemi, aby bylo
     * možné filtrování podle posledního přihlášení nebo rozumného výchozího
     * časového okna.
     *
     * @param user uživatel, pro kterého se časová hranice určuje
     * @return časová hranice pro výběr notifikací
     */
    private Instant resolveBoundary(AppUserEntity user) {
        if (user.getLastLoginAt() != null) {
            return user.getLastLoginAt();
        }
        return Instant.now(clock).minus(DEFAULT_DAYS_IF_NO_LAST_LOGIN, ChronoUnit.DAYS);
    }

    /**
     * Načítá entitu aktuálního uživatele podle e-mailu získaného
     * z autentizačního kontextu.
     *
     * E-mail je získán z Authentication.getName a následně je
     * v AppUserRepository vyhledán odpovídající uživatel. Pokud
     * uživatel neexistuje, je vyvolána výjimka UserNotFoundException.
     *
     * Metoda představuje centralizované místo pro získání entitního
     * zástupce aktuálně přihlášeného uživatele v rámci této služby.
     *
     * @param authentication autentizační kontext aktuálně přihlášeného uživatele
     * @return entita uživatele odpovídající autentizačnímu kontextu
     * @throws UserNotFoundException pokud uživatel s daným e-mailem neexistuje
     */
    private AppUserEntity getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return appUserRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
}