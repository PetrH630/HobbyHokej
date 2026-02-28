package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.notifications.entities.NotificationEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.notifications.enums.NotificationType;
import cz.phsoft.hokej.notifications.repositories.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

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
        storeForPlayer(player, type, context, null, null);
    }

    @Override
    public void storeForPlayer(PlayerEntity player,
                               NotificationType type,
                               Object context,
                               String emailTo,
                               String smsTo) {

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

        // Pokus o navázání notifikace na konkrétní zápas.
        // Pokud je context instanceof MatchEntity, využije se pro deduplikaci.
        MatchEntity match = resolveMatchFromContext(context);

        if (match != null) {
            // Deduplikace podle user + match + type.
            Optional<NotificationEntity> existingOpt =
                    notificationRepository.findByUserAndMatchAndType(owner, match, type);

            if (existingOpt.isPresent()) {
                NotificationEntity existing = existingOpt.get();

                existing.setMessageShort(messageShort);
                existing.setMessageFull(messageFull);
                existing.setEmailTo(emailTo);
                existing.setSmsTo(smsTo);
                existing.setCreatedAt(Instant.now(clock));

                notificationRepository.save(existing);

                log.debug(
                        "InAppNotificationService.storeForPlayer: aktualizována existující notifikace type={} userId={} playerId={} matchId={} emailTo={} smsTo={}",
                        type,
                        owner.getId(),
                        player.getId(),
                        match.getId(),
                        emailTo,
                        smsTo
                );
                return;
            }
        }

        // Pokud není match, nebo neexistuje záznam pro kombinaci (user, match, type),
        // vytvoří se nová notifikace.
        NotificationEntity entity = new NotificationEntity();
        entity.setUser(owner);
        entity.setPlayer(player);
        if (match != null) {
            entity.setMatch(match);
        }
        entity.setType(type);
        entity.setMessageShort(messageShort);
        entity.setMessageFull(messageFull);
        entity.setCreatedAt(Instant.now(clock));

        entity.setEmailTo(emailTo);
        entity.setSmsTo(smsTo);

        notificationRepository.save(entity);

        log.debug(
                "InAppNotificationService.storeForPlayer: uložena notifikace type={} userId={} playerId={} matchId={} emailTo={} smsTo={}",
                type,
                owner.getId(),
                player.getId(),
                match != null ? match.getId() : null,
                emailTo,
                smsTo
        );
    }

    @Override
    public void storeForUser(AppUserEntity user,
                             NotificationType type,
                             Object context) {
        storeForUser(user, type, context, null);
    }

    @Override
    public void storeForUser(AppUserEntity user,
                             NotificationType type,
                             Object context,
                             String emailTo) {

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

        // Uživatelské notifikace nejsou vázány na match – match se zde nenastavuje.
        entity.setEmailTo(emailTo);
        entity.setSmsTo(null);

        notificationRepository.save(entity);

        log.debug("InAppNotificationService.storeForUser: uložena notifikace type={} userId={} emailTo={}",
                type, user.getId(), emailTo);
    }

    @Override
    public void storeSpecialMessage(AppUserEntity user,
                                    PlayerEntity player,
                                    String messageShort,
                                    String messageFull) {
        storeSpecialMessage(user, player, messageShort, messageFull, null, null);
    }

    @Override
    public void storeSpecialMessage(AppUserEntity user,
                                    PlayerEntity player,
                                    String messageShort,
                                    String messageFull,
                                    String emailTo,
                                    String smsTo) {

        if (user == null) {
            log.debug("InAppNotificationService.storeSpecialMessage: user is null, nic se neukládá");
            return;
        }

        String shortText = (messageShort != null && !messageShort.isBlank())
                ? messageShort
                : NotificationType.SPECIAL_MESSAGE.name();

        String fullText = (messageFull != null && !messageFull.isBlank())
                ? messageFull
                : NotificationType.SPECIAL_MESSAGE.name();

        NotificationEntity entity = new NotificationEntity();
        entity.setUser(user);
        if (player != null) {
            entity.setPlayer(player);
        }
        entity.setType(NotificationType.SPECIAL_MESSAGE);
        entity.setMessageShort(shortText);
        entity.setMessageFull(fullText);
        entity.setCreatedAt(Instant.now(clock));

        // Speciální zprávy nenvážeme na konkrétní zápas – jedná se
        // o obecnou administrátorskou komunikaci.
        entity.setEmailTo(emailTo);
        entity.setSmsTo(smsTo);

        notificationRepository.save(entity);

        log.debug(
                "InAppNotificationService.storeSpecialMessage: uložena SPECIAL_MESSAGE userId={} playerId={} emailTo={} smsTo={}",
                user.getId(),
                player != null ? player.getId() : null,
                emailTo,
                smsTo
        );
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

    /**
     * Pokusí se z contextu vyčíst zápas pro navázání notifikace.
     *
     * Aktuálně se podporuje varianta, kdy je context přímo instancí MatchEntity.
     * Pokud context neobsahuje zápas, vrací se null.
     *
     * Do budoucna lze rozšířit o další typy kontextů (např. vlastní wrapper).
     *
     * @param context kontext předaný volajícím
     * @return MatchEntity, pokud jej lze z contextu získat, jinak null
     */
    private MatchEntity resolveMatchFromContext(Object context) {
        if (context instanceof MatchEntity) {
            return (MatchEntity) context;
        }
        return null;
    }
}