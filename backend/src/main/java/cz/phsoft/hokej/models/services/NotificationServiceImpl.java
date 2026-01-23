package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.NotificationSettings;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.NotificationType;
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

    public NotificationServiceImpl(
            EmailService emailService,
            SmsService smsService,
            SmsMessageBuilder smsMessageBuilder
    ) {
        this.emailService = emailService;
        this.smsService = smsService;
        this.smsMessageBuilder = smsMessageBuilder;
    }

    @Override
    public void notifyPlayer(PlayerEntity player,
                             NotificationType type,
                             Object context) {

        if (player == null) {
            log.warn("notifyPlayer() called with null player for type {}", type);
            return;
        }

        NotificationSettings settings = player.getNotificationSettings();
        if (settings == null) {
            // hráč nemá nastavené preference – nebudeme posílat nic
            log.debug("Player {} nemá NotificationSettings – žádné notifikace se neposílají", player.getId());
            return;
        }

        // ================= EMAIL =================
        if (settings.isEmailEnabled() && player.getUser() != null) {
            sendEmail(player, type, context);
        }

        // ================= SMS =================
        if (settings.isSmsEnabled() && player.getPhoneNumber() != null) {
            sendSms(player, type, context);
        }
    }

    // ----------------------------------------------------
    // EMAIL
    // ----------------------------------------------------
    private void sendEmail(PlayerEntity player,
                           NotificationType type,
                           Object context) {

        if (player.getUser() == null || player.getUser().getEmail() == null) {
            log.debug("Player {} nemá email v userovi – email se nepošle", player.getId());
            return;
        }

        String email = player.getUser().getEmail();

        switch (type) {

            case PLAYER_CREATED -> {
                emailService.sendSimpleEmail(
                        email,
                        "Hráč vytvořen",
                        "Hráč " + player.getFullName() + " byl úspěšně vytvořen."
                );
            }

            case PLAYER_UPDATED -> {
                emailService.sendSimpleEmail(
                        email,
                        "Hráč upraven",
                        "Údaje hráče " + player.getFullName() + " byly aktualizovány."
                );
            }

            case PLAYER_APPROVED -> {
                emailService.sendSimpleEmail(
                        email,
                        "Hráč schválen",
                        "Hráč " + player.getFullName() + " byl schválen administrátorem."
                );
            }

            case PLAYER_REJECTED -> {
                emailService.sendSimpleEmail(
                        email,
                        "Hráč zamítnut",
                        "Hráč " + player.getFullName() + " byl zamítnut administrátorem."
                );
            }

            // USER_UPDATED můžeš použít jinde (např. v UserService),
            // tady ho zatím ignorujeme nebo případně logujeme:
            case USER_UPDATED -> {
                emailService.sendSimpleEmail(
                        email,
                        "Účet byl aktualizován",
                        "Údaje vašeho účtu byly aktualizovány."
                );
            }

            // ostatní typy jsou spíš match-related a smysl dávají přes SMS,
            // email pro ně teď neřešíme
            default -> {
                log.debug("Typ {} nemá definovanou email notifikaci, nic se neposílá", type);
            }
        }
    }

    // ----------------------------------------------------
    // SMS
    // ----------------------------------------------------
    private void sendSms(PlayerEntity player,
                         NotificationType type,
                         Object context) {

        String phone = player.getPhoneNumber();
        if (phone == null || phone.isBlank()) {
            log.debug("Player {} nemá telefon – SMS se nepošle", player.getId());
            return;
        }

        switch (type) {

            // registrace / odhlášení / omluva – používají MatchRegistrationEntity
            case PLAYER_REGISTERED,
                 PLAYER_UNREGISTERED,
                 PLAYER_EXCUSED -> {

                if (!(context instanceof MatchRegistrationEntity registration)) {
                    log.warn("NotificationType {} očekává MatchRegistrationEntity v context, ale dostal {}", type,
                            (context != null ? context.getClass().getName() : "null"));
                    return;
                }

                String msg = smsMessageBuilder.buildMessageRegistration(registration);
                smsService.sendSms(phone, msg);
            }

            // rezervace – můžeš udělat jednoduchou SMS nebo případně taky použít builder
            case PLAYER_RESERVED -> {
                if (context instanceof MatchRegistrationEntity registration) {
                    // pokud chceš, můžeš klidně použít stejnou / upravenou zprávu
                    String msg = smsMessageBuilder.buildMessageRegistration(registration);
                    smsService.sendSms(phone, msg);
                } else {
                    String msg = "app_hokej - hráč " + player.getFullName() +
                            " je nyní v režimu NÁHRADNÍKA na zápas.";
                    smsService.sendSms(phone, msg);
                }
            }

            // USER_UPDATED a „čistě hráčské“ změny přes SMS obvykle neposíláme
            case PLAYER_CREATED,
                 PLAYER_UPDATED,
                 PLAYER_APPROVED,
                 PLAYER_REJECTED,
                 USER_UPDATED -> {
                log.debug("Typ {} nemá definovanou SMS notifikaci, nic se neposílá", type);
            }

            default -> {
                log.debug("Neznámý NotificationType {} – SMS se neposílá", type);
            }
        }
    }
}
