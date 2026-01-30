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

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final AppUserRepository appUserRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final SmsMessageBuilder smsMessageBuilder;
    private final EmailMessageBuilder emailMessageBuilder;
    private final NotificationPreferencesService notificationPreferencesService;

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

        // vyhledám manažery
        List<AppUserEntity> managers = appUserRepository.findAll().stream()
                .filter(m -> m.getRole() == Role.ROLE_MANAGER)
                .toList();

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

        // EMAIL – manažeři (všichni s rolí ROLE_MANAGER)
        for (AppUserEntity manager : managers) {
            sendEmailToManager(manager, player, type, context);
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

        // manažeři
        List<AppUserEntity> managers = appUserRepository.findAll().stream()
                .filter(m -> m.getRole() == Role.ROLE_MANAGER)
                .toList();

        // ==========
        // EMAIL PRO UŽIVATELE
        // ==========
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

        // ==========
        // EMAIL PRO MANAŽERY
        // ==========
        for (AppUserEntity manager : managers) {
            if (manager == null || manager.getEmail() == null || manager.getEmail().isBlank()) {
                continue;
            }

            EmailMessageBuilder.EmailContent managerContent =
                    emailMessageBuilder.buildForManager(type, null, manager, effectiveContext); // context = user

            if (managerContent == null) {
                log.debug("Typ {} nemá definovanou email šablonu pro manažera (USER), nic se neposílá", type);
                continue;
            }

            if (managerContent.html()) {
                emailService.sendHtmlEmail(manager.getEmail(), managerContent.subject(), managerContent.body());
            } else {
                emailService.sendSimpleEmail(manager.getEmail(), managerContent.subject(), managerContent.body());
            }
        }
    }




    // ----------------------------------------------------
    // EMAIL
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
}
