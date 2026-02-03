package cz.phsoft.hokej.models.services.notification;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.NotificationType;
import cz.phsoft.hokej.data.enums.Role;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.models.services.NotificationDecision;
import cz.phsoft.hokej.models.services.email.EmailMessageBuilder;
import cz.phsoft.hokej.models.services.email.EmailService;
import cz.phsoft.hokej.models.services.sms.SmsMessageBuilder;
import cz.phsoft.hokej.models.services.sms.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Implementace NotificationService.
 *
 * Zajišťuje:
 * - použití NotificationPreferencesService pro rozhodnutí, komu notifikaci poslat,
 * - sestavení obsahu zpráv pomocí EmailMessageBuilder a SmsMessageBuilder,
 * - odesílání e-mailů pomocí EmailService,
 * - odesílání SMS pomocí SmsService,
 * - rozesílání kopií vybraných notifikací manažerům.
 *
 * Třída neřeší:
 * - perzistenci notifikací,
 * - detailní business pravidla, kdy se má notifikace vyvolat.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final AppUserRepository appUserRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final SmsMessageBuilder smsMessageBuilder;
    private final EmailMessageBuilder emailMessageBuilder;
    private final NotificationPreferencesService notificationPreferencesService;

    /**
     * Typy notifikací, pro které se nemá posílat kopie manažerům.
     * Platí jak pro notifyPlayer, tak pro notifyUser.
     */
    private static final Set<NotificationType> MANAGER_COPY_BLACKLIST = EnumSet.of(
            NotificationType.MATCH_CANCELED,
            NotificationType.MATCH_TIME_CHANGED,
            NotificationType.MATCH_UNCANCELED,
            NotificationType.MATCH_REMINDER
    );

    public NotificationServiceImpl(
            AppUserRepository appUserRepository,
            EmailService emailService,
            SmsService smsService,
            SmsMessageBuilder smsMessageBuilder,
            EmailMessageBuilder emailMessageBuilder,
            NotificationPreferencesService notificationPreferencesService
    ) {
        this.appUserRepository = appUserRepository;
        this.emailService = emailService;
        this.smsService = smsService;
        this.smsMessageBuilder = smsMessageBuilder;
        this.emailMessageBuilder = emailMessageBuilder;
        this.notificationPreferencesService = notificationPreferencesService;
    }

    @Override
    public void notifyPlayer(PlayerEntity player,
                             NotificationType type,
                             Object context) {

        if (player == null) {
            log.warn("notifyPlayer() called with null player for type {}", type);
            return;
        }

        NotificationDecision decision = notificationPreferencesService.evaluate(player, type);

        // E-mail pro uživatele (AppUser).
        if (decision.isSendEmailToUser() && decision.getUserEmail() != null) {
            sendEmailToUser(decision.getUserEmail(), player, type, context);
        }

        // E-mail pro hráče.
        if (decision.isSendEmailToPlayer() && decision.getPlayerEmail() != null) {
            sendEmailToPlayer(decision.getPlayerEmail(), player, type, context);
        }

        // SMS pro hráče.
        if (decision.isSendSmsToPlayer() && decision.getPlayerPhone() != null) {
            sendSmsToPhone(decision.getPlayerPhone(), player, type, context);
        }

        // E-mail pro manažery (kopie zpráv pro hráče), pokud typ není v blacklistu.
        if (shouldSendManagerCopy(type)) {

            List<AppUserEntity> managers = appUserRepository.findAll().stream()
                    .filter(m -> m.getRole() == Role.ROLE_MANAGER)
                    .toList();

            AppUserEntity owner = player.getUser(); // uživatel, kterému hráč patří

            for (AppUserEntity manager : managers) {
                if (manager == null || manager.getEmail() == null || manager.getEmail().isBlank()) {
                    continue;
                }

                // Neposílat, pokud je manažer zároveň vlastníkem hráče.
                if (owner != null && owner.getId() != null
                        && Objects.equals(manager.getId(), owner.getId())) {
                    log.debug("Manager {} je zároveň vlastníkem hráče {} – kopie se neposílá (notifyPlayer).",
                            manager.getId(), player.getId());
                    continue;
                }

                String managerEmail = manager.getEmail();

                // Neposílat, pokud manažer už dostane e-mail jako USER nebo PLAYER (stejný e-mail).
                if (Objects.equals(managerEmail, decision.getUserEmail())
                        || Objects.equals(managerEmail, decision.getPlayerEmail())) {
                    log.debug("Manager {} má stejný e-mail jako příjemce (USER/PLAYER) – kopie se neposílá (notifyPlayer).",
                            manager.getId());
                    continue;
                }

                sendEmailToManager(manager, player, type, context);
            }
        } else {
            log.debug("Typ {} je v MANAGER_COPY_BLACKLIST – kopie manažerům se neposílá (notifyPlayer).", type);
        }
    }

    @Override
    public void notifyUser(AppUserEntity user,
                           NotificationType type,
                           Object context) {

        if (user == null) {
            log.warn("notifyUser() called with null user for type {}", type);
            return;
        }

        Object effectiveContext = (context != null) ? context : user;

        // E-mail pro uživatele.
        String userEmail = user.getEmail();

        if (userEmail != null && !userEmail.isBlank()) {

            // Player je null, user se případně předá v kontextu.
            EmailMessageBuilder.EmailContent content =
                    emailMessageBuilder.buildForUser(type, null, userEmail, effectiveContext);

            if (content != null) {
                if (content.html()) {
                    emailService.sendHtmlEmail(userEmail, content.subject(), content.body());
                } else {
                    emailService.sendSimpleEmail(userEmail, content.subject(), content.body());
                }
            } else {
                log.debug("Typ {} nemá definovanou e-mailovou šablonu pro uživatele (USER), nic se neposílá", type);
            }
        } else {
            log.debug("notifyUser: uživatel {} nemá e-mail, nic se neposílá", user.getId());
        }

        // E-mail pro manažery.
        if (shouldSendManagerCopy(type)) {

            List<AppUserEntity> managers = appUserRepository.findAll().stream()
                    .filter(m -> m.getRole() == Role.ROLE_MANAGER)
                    .toList();

            for (AppUserEntity manager : managers) {
                if (manager == null || manager.getEmail() == null || manager.getEmail().isBlank()) {
                    continue;
                }

                // Neposílat, pokud je manažer zároveň tento uživatel.
                if (user.getId() != null && Objects.equals(manager.getId(), user.getId())) {
                    log.debug("Manager {} je zároveň adresátem (USER) – kopie se neposílá (notifyUser).",
                            manager.getId());
                    continue;
                }

                String managerEmail = manager.getEmail();

                // Neposílat, pokud má manažer stejný e-mail jako uživatel.
                if (Objects.equals(managerEmail, userEmail)) {
                    log.debug("Manager {} má stejný e-mail jako uživatel – kopie se neposílá (notifyUser).",
                            manager.getId());
                    continue;
                }

                EmailMessageBuilder.EmailContent managerContent =
                        emailMessageBuilder.buildForManager(type, null, manager, effectiveContext);

                if (managerContent == null) {
                    log.debug("Typ {} nemá definovanou e-mailovou šablonu pro manažera (USER), nic se neposílá", type);
                    continue;
                }

                if (managerContent.html()) {
                    emailService.sendHtmlEmail(managerEmail, managerContent.subject(), managerContent.body());
                } else {
                    emailService.sendSimpleEmail(managerEmail, managerContent.subject(), managerContent.body());
                }
            }
        } else {
            log.debug("Typ {} je v MANAGER_COPY_BLACKLIST – kopie manažerům se neposílá (notifyUser).", type);
        }
    }

    // Pomocné metody pro e-mail

    private void sendEmailToManager(AppUserEntity manager,
                                    PlayerEntity player,
                                    NotificationType type,
                                    Object context) {

        if (manager == null || manager.getEmail() == null || manager.getEmail().isBlank()) {
            log.debug("sendEmailToManager: prázdný manager nebo e-mail, nic se neposílá");
            return;
        }

        String email = manager.getEmail();

        EmailMessageBuilder.EmailContent content =
                emailMessageBuilder.buildForManager(type, player, manager, context);

        if (content == null) {
            log.debug("Typ {} nemá definovanou e-mailovou šablonu pro manažera, nic se neposílá", type);
            return;
        }

        if (content.html()) {
            emailService.sendHtmlEmail(email, content.subject(), content.body());
        } else {
            emailService.sendSimpleEmail(email, content.subject(), content.body());
        }
    }

    private void sendEmailToUser(String email,
                                 PlayerEntity player,
                                 NotificationType type,
                                 Object context) {

        if (email == null || email.isBlank()) {
            log.debug("sendEmailToUser: prázdný e-mail, nic se neposílá");
            return;
        }

        EmailMessageBuilder.EmailContent content =
                emailMessageBuilder.buildForUser(type, player, email, context);

        if (content == null) {
            log.debug("Typ {} nemá definovanou e-mailovou šablonu pro uživatele, nic se neposílá", type);
            return;
        }

        if (content.html()) {
            emailService.sendHtmlEmail(email, content.subject(), content.body());
        } else {
            emailService.sendSimpleEmail(email, content.subject(), content.body());
        }
    }

    private void sendEmailToPlayer(String email,
                                   PlayerEntity player,
                                   NotificationType type,
                                   Object context) {

        if (email == null || email.isBlank()) {
            log.debug("sendEmailToPlayer: prázdný e-mail, nic se neposílá");
            return;
        }

        EmailMessageBuilder.EmailContent content =
                emailMessageBuilder.buildForPlayer(type, player, email, context);

        if (content == null) {
            log.debug("Typ {} nemá definovanou e-mailovou šablonu pro hráče, nic se neposílá", type);
            return;
        }

        if (content.html()) {
            emailService.sendHtmlEmail(email, content.subject(), content.body());
        } else {
            emailService.sendSimpleEmail(email, content.subject(), content.body());
        }
    }

    // SMS pomocná metoda

    private void sendSmsToPhone(String phone,
                                PlayerEntity player,
                                NotificationType type,
                                Object context) {

        if (phone == null || phone.isBlank()) {
            log.debug("sendSmsToPhone: prázdný telefon – SMS se nepošle (player {})", player.getId());
            return;
        }

        String msg = smsMessageBuilder.buildForNotification(type, player, context);

        if (msg == null || msg.isBlank()) {
            log.debug("Typ {} nemá definovanou SMS šablonu nebo chybí context – SMS se neposílá", type);
            return;
        }

        smsService.sendSms(phone, msg);
    }

    /**
     * Určuje, zda se pro daný typ notifikace mají posílat kopie manažerům.
     *
     * Pokud je typ uveden v MANAGER_COPY_BLACKLIST, kopie se neposílají.
     */
    private boolean shouldSendManagerCopy(NotificationType type) {
        return !MANAGER_COPY_BLACKLIST.contains(type);
    }
}
