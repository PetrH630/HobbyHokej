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
     * Typy notifikací, pro které se NEMÁ posílat kopie manažerům.
     * (Platí jak pro notifyPlayer, tak pro notifyUser.)
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

        // EMAIL – uživatel (AppUser)
        if (decision.isSendEmailToUser() && decision.getUserEmail() != null) {
            sendEmailToUser(decision.getUserEmail(), player, type, context);
        }

        // EMAIL – hráč (kontakt na PlayerEntity)
        if (decision.isSendEmailToPlayer() && decision.getPlayerEmail() != null) {
            sendEmailToPlayer(decision.getPlayerEmail(), player, type, context);
        }

        // SMS – hráč
        if (decision.isSendSmsToPlayer() && decision.getPlayerPhone() != null) {
            sendSmsToPhone(decision.getPlayerPhone(), player, type, context);
        }

        // EMAIL – manažeři (kopie zpráv pro hráče), jen pokud typ není v blacklistu
        if (shouldSendManagerCopy(type)) {

            List<AppUserEntity> managers = appUserRepository.findAll().stream()
                    .filter(m -> m.getRole() == Role.ROLE_MANAGER)
                    .toList();

            AppUserEntity owner = player.getUser(); // uživatel, kterému hráč patří

            for (AppUserEntity manager : managers) {
                if (manager == null || manager.getEmail() == null || manager.getEmail().isBlank()) {
                    continue;
                }

                // nepřeposílat, pokud je manager zároveň owner hráče
                if (owner != null && owner.getId() != null
                        && Objects.equals(manager.getId(), owner.getId())) {
                    log.debug("Manager {} je zároveň vlastníkem hráče {} – kopie se neposílá (notifyPlayer).",
                            manager.getId(), player.getId());
                    continue;
                }

                String managerEmail = manager.getEmail();

                // nepřeposílat, pokud už manager dostane mail jako USER nebo PLAYER (shodný email)
                if (Objects.equals(managerEmail, decision.getUserEmail())
                        || Objects.equals(managerEmail, decision.getPlayerEmail())) {
                    log.debug("Manager {} má stejný email jako příjemce (USER/PLAYER) – kopie se neposílá (notifyPlayer).",
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

        // ========== EMAIL PRO UŽIVATELE ==========
        String userEmail = user.getEmail();

        if (userEmail != null && !userEmail.isBlank()) {
            // player = null, user pošleme v contextu
            EmailMessageBuilder.EmailContent content =
                    emailMessageBuilder.buildForUser(type, null, userEmail, effectiveContext);

            if (content != null) {
                if (content.html()) {
                    emailService.sendHtmlEmail(userEmail, content.subject(), content.body());
                } else {
                    emailService.sendSimpleEmail(userEmail, content.subject(), content.body());
                }
            } else {
                log.debug("Typ {} nemá definovanou email šablonu pro uživatele (USER), nic se neposílá", type);
            }
        } else {
            log.debug("notifyUser: uživatel {} nemá email, nic se neposílá", user.getId());
        }

        // ========== EMAIL PRO MANAŽERY ==========
        if (shouldSendManagerCopy(type)) {

            List<AppUserEntity> managers = appUserRepository.findAll().stream()
                    .filter(m -> m.getRole() == Role.ROLE_MANAGER)
                    .toList();

            for (AppUserEntity manager : managers) {
                if (manager == null || manager.getEmail() == null || manager.getEmail().isBlank()) {
                    continue;
                }

                // nepřeposílat, pokud je manager zároveň tento user
                if (user.getId() != null && Objects.equals(manager.getId(), user.getId())) {
                    log.debug("Manager {} je zároveň adresátem (USER) – kopie se neposílá (notifyUser).",
                            manager.getId());
                    continue;
                }

                String managerEmail = manager.getEmail();

                // nepřeposílat, pokud je jeho email stejný jako email uživatele
                if (Objects.equals(managerEmail, userEmail)) {
                    log.debug("Manager {} má stejný email jako uživatel – kopie se neposílá (notifyUser).",
                            manager.getId());
                    continue;
                }

                EmailMessageBuilder.EmailContent managerContent =
                        emailMessageBuilder.buildForManager(type, null, manager, effectiveContext); // context = user

                if (managerContent == null) {
                    log.debug("Typ {} nemá definovanou email šablonu pro manažera (USER), nic se neposílá", type);
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

    // ----------------------------------------------------
    // EMAIL helper metody
    // ----------------------------------------------------

    private void sendEmailToManager(AppUserEntity manager,
                                    PlayerEntity player,
                                    NotificationType type,
                                    Object context) {

        if (manager == null || manager.getEmail() == null || manager.getEmail().isBlank()) {
            log.debug("sendEmailToManager: prázdný manager nebo email, nic se neposílá");
            return;
        }

        String email = manager.getEmail();

        EmailMessageBuilder.EmailContent content =
                emailMessageBuilder.buildForManager(type, player, manager, context);

        if (content == null) {
            log.debug("Typ {} nemá definovanou email šablonu pro manažera, nic se neposílá", type);
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
            log.debug("sendEmailToUser: prázdný email, nic se neposílá");
            return;
        }

        EmailMessageBuilder.EmailContent content =
                emailMessageBuilder.buildForUser(type, player, email, context);

        if (content == null) {
            log.debug("Typ {} nemá definovanou email šablonu pro uživatele, nic se neposílá", type);
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
            log.debug("sendEmailToPlayer: prázdný email, nic se neposílá");
            return;
        }

        EmailMessageBuilder.EmailContent content =
                emailMessageBuilder.buildForPlayer(type, player, email, context);

        if (content == null) {
            log.debug("Typ {} nemá definovanou email šablonu pro hráče, nic se neposílá", type);
            return;
        }

        if (content.html()) {
            emailService.sendHtmlEmail(email, content.subject(), content.body());
        } else {
            emailService.sendSimpleEmail(email, content.subject(), content.body());
        }
    }

    // ----------------------------------------------------
    // SMS – beze změny
    // ----------------------------------------------------
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

    // ----------------------------------------------------
    // helper pro posílání kopií manažerům
    // ----------------------------------------------------
    private boolean shouldSendManagerCopy(NotificationType type) {
        return !MANAGER_COPY_BLACKLIST.contains(type);
    }
}
