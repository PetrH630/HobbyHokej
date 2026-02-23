package cz.phsoft.hokej.models.services.notification;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.NotificationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.NotificationType;
import cz.phsoft.hokej.data.repositories.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

/**
 * Implementace služby pro ukládání aplikačních notifikací.
 *
 * Třída neřeší odesílání e-mailů ani SMS.
 * Vytváří zjednodušené i detailní notifikace v databázi
 * pro zobrazení v uživatelském rozhraní.
 *
 * Text notifikací se sestavuje pomocí InAppNotificationBuilder.
 */
@Service
public class InAppNotificationServiceImpl implements InAppNotificationService {

    private static final Logger log = LoggerFactory.getLogger(InAppNotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final InAppNotificationBuilder inAppNotificationBuilder;
    private final Clock clock;

    public InAppNotificationServiceImpl(NotificationRepository notificationRepository,
                                        InAppNotificationBuilder inAppNotificationBuilder,
                                        Clock clock) {
        this.notificationRepository = notificationRepository;
        this.inAppNotificationBuilder = inAppNotificationBuilder;
        this.clock = clock;
    }

    @Override
    public void storeForPlayer(PlayerEntity player,
                               NotificationType type,
                               Object context) {

        if (player == null) {
            log.debug("InAppNotificationService.storeForPlayer: player is null, nic se neukládá");
            return;
        }

        AppUserEntity owner = player.getUser();
        if (owner == null) {
            log.debug(
                    "InAppNotificationService.storeForPlayer: player {} nemá přiřazeného uživatele, nic se neukládá",
                    player.getId()
            );
            return;
        }

        InAppNotificationBuilder.InAppNotificationContent content =
                buildContent(type, owner, player, context);

        String messageShort = content != null && content.title() != null && !content.title().isBlank()
                ? content.title()
                : type.name();

        String messageFull = content != null && content.message() != null && !content.message().isBlank()
                ? content.message()
                : type.name();

        NotificationEntity entity = new NotificationEntity();
        entity.setUser(owner);
        entity.setPlayer(player);
        entity.setType(type);
        entity.setMessageShort(messageShort);
        entity.setMessageFull(messageFull);
        entity.setCreatedAt(Instant.now(clock));

        notificationRepository.save(entity);

        log.debug("InAppNotificationService.storeForPlayer: uložena notifikace type={} userId={} playerId={}",
                type, owner.getId(), player.getId());
    }

    @Override
    public void storeForUser(AppUserEntity user,
                             NotificationType type,
                             Object context) {

        if (user == null) {
            log.debug("InAppNotificationService.storeForUser: user is null, nic se neukládá");
            return;
        }

        InAppNotificationBuilder.InAppNotificationContent content =
                buildContent(type, user, null, context);

        String messageShort = content != null && content.title() != null && !content.title().isBlank()
                ? content.title()
                : type.name();

        String messageFull = content != null && content.message() != null && !content.message().isBlank()
                ? content.message()
                : type.name();

        NotificationEntity entity = new NotificationEntity();
        entity.setUser(user);
        entity.setType(type);
        entity.setMessageShort(messageShort);
        entity.setMessageFull(messageFull);
        entity.setCreatedAt(Instant.now(clock));

        notificationRepository.save(entity);

        log.debug("InAppNotificationService.storeForUser: uložena notifikace type={} userId={}",
                type, user.getId());
    }

    /**
     * Sestavuje obsah in-app notifikace pomocí InAppNotificationBuilder.
     *
     * Pokud builder vrátí null (pro daný NotificationType není definována
     * šablona), vrací se také null a volající použije fallback type.name().
     */
    private InAppNotificationBuilder.InAppNotificationContent buildContent(NotificationType type,
                                                                           AppUserEntity user,
                                                                           PlayerEntity player,
                                                                           Object context) {
        try {
            return inAppNotificationBuilder.build(type, user, player, context);
        } catch (Exception ex) {
            log.debug("InAppNotificationService.buildContent: chyba při sestavování notifikace pro type {}: {}",
                    type, ex.getMessage());
            return null;
        }
    }
}