package cz.phsoft.hokej.models.services.notification;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.NotificationType;
import cz.phsoft.hokej.models.services.NotificationDecision;
import cz.phsoft.hokej.models.services.email.EmailMessageBuilder;
import cz.phsoft.hokej.models.services.email.EmailService;
import cz.phsoft.hokej.models.services.sms.SmsMessageBuilder;
import cz.phsoft.hokej.models.services.sms.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final EmailService emailService;
    private final SmsService smsService;
    private final SmsMessageBuilder smsMessageBuilder;
    private final EmailMessageBuilder emailMessageBuilder;
    private final NotificationPreferencesService notificationPreferencesService;

    public NotificationServiceImpl(
            EmailService emailService,
            SmsService smsService,
            SmsMessageBuilder smsMessageBuilder,
            EmailMessageBuilder emailMessageBuilder,
            NotificationPreferencesService notificationPreferencesService
    ) {
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

        // EMAILY
        if (decision.isSendEmailToUser() && decision.getUserEmail() != null) {
            sendEmailToUser(decision.getUserEmail(), player, type, context);
        }
        if (decision.isSendEmailToPlayer() && decision.getPlayerEmail() != null) {
            sendEmailToPlayer(decision.getPlayerEmail(), player, type, context);
        }

        // SMS
        if (decision.isSendSmsToPlayer() && decision.getPlayerPhone() != null) {
            sendSmsToPhone(decision.getPlayerPhone(), player, type, context);
        }
    }

    // ----------------------------------------------------
    // EMAIL
    // ----------------------------------------------------

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
