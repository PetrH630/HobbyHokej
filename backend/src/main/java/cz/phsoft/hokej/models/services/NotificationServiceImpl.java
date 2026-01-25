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

/**
 * Centrální služba pro odesílání notifikací hráčům (email + SMS).
 *
 * Odpovědnosti:
 * <ul>
 *     <li>rozhodnout, zda se má notifikace poslat (podle preferencí hráče),</li>
 *     <li>zvolit kanál (email / SMS / oba),</li>
 *     <li>delegovat konkrétní odeslání na {@link EmailService} / {@link SmsService}.</li>
 * </ul>
 *
 * Tato třída:
 * <ul>
 *     <li>NEřeší byznys logiku – kdy a proč se notifikace spouští, to řeší volající service,</li>
 *     <li>NEřeší oprávnění ani autentizaci,</li>
 *     <li>pouze přijímá typ notifikace a kontext a podle toho pošle zprávu.</li>
 * </ul>
 *
 * Princip:
 * <pre>
 *  nějaká service → notifyPlayer(player, type, context)
 *      → NotificationServiceImpl:
 *          - přečte NotificationSettings hráče,
 *          - rozhodne email/SMS,
 *          - zavolá EmailService / SmsService.
 * </pre>
 *
 * Parametr {@code context}:
 * <ul>
 *     <li>slouží k předání doménových dat (např. {@link MatchRegistrationEntity}),</li>
 *     <li>díky tomu zůstává API jednoduché a rozšiřitelné.</li>
 * </ul>
 */
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

    /**
     * Hlavní vstupní bod pro notifikace.
     *
     * @param player  hráč, kterému má být notifikace poslána
     * @param type    typ notifikace ({@link NotificationType})
     * @param context kontextová data (např. {@link MatchRegistrationEntity}),
     *                může být {@code null} – záleží na typu notifikace
     */
    @Override
    public void notifyPlayer(PlayerEntity player,
                             NotificationType type,
                             Object context) {

        // bezpečnostní kontrola – nemělo by se stávat
        if (player == null) {
            log.warn("notifyPlayer() called with null player for type {}", type);
            return;
        }

        // preference notifikací hráče
        NotificationSettings settings = player.getNotificationSettings();
        if (settings == null) {
            // hráč nemá nastavené preference → nic neposíláme
            log.debug(
                    "Player {} nemá NotificationSettings – žádné notifikace se neposílají",
                    player.getId()
            );
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

    /**
     * Odeslání emailové notifikace podle typu.
     * <p>
     * Email se používá především pro:
     * <ul>
     *     <li>změny účtu,</li>
     *     <li>změny stavu hráče (schválení / zamítnutí / úpravy údajů).</li>
     * </ul>
     *
     * Notifikace spojené se zápasem (registrace, omluva, rezervace)
     * jsou primárně řešeny přes SMS.
     */
    private void sendEmail(PlayerEntity player,
                           NotificationType type,
                           Object context) {

        if (player.getUser() == null || player.getUser().getEmail() == null) {
            log.debug(
                    "Player {} nemá email v uživateli – email se nepošle",
                    player.getId()
            );
            return;
        }

        String email = player.getUser().getEmail();

        switch (type) {
            case PLAYER_CREATED -> emailService.sendSimpleEmail(
                    email,
                    "Hráč vytvořen",
                    "Hráč " + player.getFullName() + " byl úspěšně vytvořen."
            );

            case PLAYER_UPDATED -> emailService.sendSimpleEmail(
                    email,
                    "Hráč upraven",
                    "Údaje hráče " + player.getFullName() + " byly aktualizovány."
            );

            case PLAYER_APPROVED -> emailService.sendSimpleEmail(
                    email,
                    "Hráč schválen",
                    "Hráč " + player.getFullName() + " byl schválen administrátorem."
            );

            case PLAYER_REJECTED -> emailService.sendSimpleEmail(
                    email,
                    "Hráč zamítnut",
                    "Hráč " + player.getFullName() + " byl zamítnut administrátorem."
            );

            case USER_UPDATED -> emailService.sendSimpleEmail(
                    email,
                    "Účet byl aktualizován",
                    "Údaje vašeho účtu byly aktualizovány."
            );

            // ostatní typy notifikací emailem neposíláme
            default -> log.debug(
                    "Typ {} nemá definovanou email notifikaci, nic se neposílá",
                    type
            );
        }
    }

    // ----------------------------------------------------
    // SMS
    // ----------------------------------------------------

    /**
     * Odeslání SMS notifikace podle typu.
     * <p>
     * SMS se používá hlavně pro:
     * <ul>
     *     <li>registrace na zápasy,</li>
     *     <li>odhlášení,</li>
     *     <li>omluvy,</li>
     *     <li>rezervace (náhradníci).</li>
     * </ul>
     *
     * Pro většinu těchto notifikací se očekává
     * {@link MatchRegistrationEntity} v parametru {@code context},
     * protože obsahuje detailní informace o zápasu a registraci.
     */
    private void sendSms(PlayerEntity player,
                         NotificationType type,
                         Object context) {

        String phone = player.getPhoneNumber();
        if (phone == null || phone.isBlank()) {
            log.debug("Player {} nemá telefon – SMS se nepošle", player.getId());
            return;
        }

        switch (type) {

            // registrace / odhlášení / omluva – vyžadují MatchRegistrationEntity
            case PLAYER_REGISTERED,
                 PLAYER_UNREGISTERED,
                 PLAYER_EXCUSED -> {

                if (!(context instanceof MatchRegistrationEntity registration)) {
                    log.warn(
                            "NotificationType {} očekává MatchRegistrationEntity v context, ale dostal {}",
                            type,
                            (context != null ? context.getClass().getName() : "null")
                    );
                    return;
                }

                String msg = smsMessageBuilder.buildMessageRegistration(registration);
                smsService.sendSms(phone, msg);
            }

            // rezervace hráče (náhradník)
            case PLAYER_RESERVED -> {
                if (context instanceof MatchRegistrationEntity registration) {
                    String msg = smsMessageBuilder.buildMessageRegistration(registration);
                    smsService.sendSms(phone, msg);
                } else {
                    // Fallback zpráva, pokud kontext není k dispozici
                    String msg = "app_hokej - hráč " + player.getFullName()
                            + " je nyní v režimu NÁHRADNÍKA na zápas.";
                    smsService.sendSms(phone, msg);
                }
            }

            // tyto typy přes SMS neposíláme – jsou čistě emailové
            case PLAYER_CREATED,
                 PLAYER_UPDATED,
                 PLAYER_APPROVED,
                 PLAYER_REJECTED,
                 USER_UPDATED -> log.debug(
                    "Typ {} nemá definovanou SMS notifikaci, nic se neposílá",
                    type
            );

            // neznámý / nový typ – raději neposílat
            default -> log.debug(
                    "Neznámý NotificationType {} – SMS se neposílá",
                    type
            );
        }
    }
}
