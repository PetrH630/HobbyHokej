package cz.phsoft.hokej.models.services.notification;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.NotificationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.NotificationType;
import cz.phsoft.hokej.data.repositories.NotificationRepository;
import cz.phsoft.hokej.models.services.email.EmailMessageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

/**
 * Implementace služby pro ukládání aplikačních notifikací.
 *
 * Třída neřeší odesílání e-mailů ani SMS. Vytváří pouze
 * zjednodušené notifikace v databázi pro zobrazení v UI.
 */
@Service
public class InAppNotificationServiceImpl implements InAppNotificationService {

    private static final Logger log = LoggerFactory.getLogger(InAppNotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final EmailMessageBuilder emailMessageBuilder;
    private final Clock clock;

    public InAppNotificationServiceImpl(NotificationRepository notificationRepository,
                                        EmailMessageBuilder emailMessageBuilder,
                                        Clock clock) {
        this.notificationRepository = notificationRepository;
        this.emailMessageBuilder = emailMessageBuilder;
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
            log.debug("InAppNotificationService.storeForPlayer: player {} nemá přiřazeného uživatele, nic se neukládá",
                    player.getId());
            return;
        }

        String messageShort = buildShortMessageForPlayer(type, player, context);
        String messageFull = null; // lze doplnit později, pokud bude potřeba

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

        String messageShort = buildShortMessageForUser(type, user, context);
        String messageFull = null;

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
     * Vytváří stručný text notifikace pro hráče.
     *
     * Primárně se používá předmět e-mailu. Pokud není k dispozici,
     * použije se fallback s názvem typu.
     */
    private String buildShortMessageForPlayer(NotificationType type,
                                              PlayerEntity player,
                                              Object context) {
        try {
            // E-mail nepotřebujeme odeslat, stačí použít šablonu pro subject.
            EmailMessageBuilder.EmailContent content =
                    emailMessageBuilder.buildForPlayer(type, player, null, context);

            if (content != null && content.subject() != null && !content.subject().isBlank()) {
                return content.subject();
            }
        } catch (Exception ex) {
            log.debug("buildShortMessageForPlayer: chyba při sestavování subjectu pro type {}: {}",
                    type, ex.getMessage());
        }

        return type.name();
    }

    /**
     * Vytváří stručný text notifikace pro uživatele.
     *
     * Primárně se používá předmět e-mailu. Pokud není k dispozici,
     * použije se fallback s názvem typu.
     */
    private String buildShortMessageForUser(NotificationType type,
                                            AppUserEntity user,
                                            Object context) {
        try {
            String email = user.getEmail();
            EmailMessageBuilder.EmailContent content =
                    emailMessageBuilder.buildForUser(type, null, email, context);

            if (content != null && content.subject() != null && !content.subject().isBlank()) {
                return content.subject();
            }
        } catch (Exception ex) {
            log.debug("buildShortMessageForUser: chyba při sestavování subjectu pro type {}: {}",
                    type, ex.getMessage());
        }

        return type.name();
    }
}