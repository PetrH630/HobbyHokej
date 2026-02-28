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
 * Třída:
 * - načítá uživatele podle Authentication pomocí AppUserRepository,
 * - používá NotificationRepository pro práci s entitami,
 * - provádí mapování na DTO pomocí NotificationMapper,
 * - určuje časovou hranici podle lastLoginAt nebo výchozího okna.
 *
 * Controller deleguje veškerou logiku do této služby
 * a nepracuje přímo s repository.
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

    public NotificationQueryServiceImpl(NotificationRepository notificationRepository,
                                        NotificationMapper notificationMapper,
                                        AppUserRepository appUserRepository,
                                        Clock clock) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.appUserRepository = appUserRepository;
        this.clock = clock;
    }

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

    @Override
    public List<NotificationDTO> getSinceLastLogin(Authentication authentication) {
        AppUserEntity user = getCurrentUser(authentication);
        Instant boundary = resolveBoundary(user);

        List<NotificationEntity> entities =
                notificationRepository.findByUserAndCreatedAtAfterOrderByCreatedAtDesc(user, boundary);

        return notificationMapper.toDtoList(entities);
    }

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

    @Override
    public List<NotificationDTO> getAllNotifications(int limit) {
        List<NotificationEntity> entities = notificationRepository.findAllByOrderByCreatedAtDesc();

        if (limit > 0 && entities.size() > limit) {
            entities = entities.subList(0, limit);
        }

        return notificationMapper.toDtoList(entities);
    }

    /**
     * Určuje časovou hranici pro výběr notifikací.
     *
     * Pokud má uživatel nastaven lastLoginAt, použije se tato hodnota.
     * Jinak se použije aktuální čas mínus DEFAULT_DAYS_IF_NO_LAST_LOGIN dní.
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