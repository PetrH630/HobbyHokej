package cz.phsoft.hokej.models.services;
import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
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
    private final NotificationPreferencesService notificationPreferencesService;

    public NotificationServiceImpl(
            EmailService emailService,
            SmsService smsService,
            SmsMessageBuilder smsMessageBuilder,
            NotificationPreferencesService notificationPreferencesService
    ) {
        this.emailService = emailService;
        this.smsService = smsService;
        this.smsMessageBuilder = smsMessageBuilder;
        this.notificationPreferencesService = notificationPreferencesService;
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

        // PŮVODNÍ LOGIKA PŘES NotificationSettings – NYNÍ NAHRAZENA PREFERENCES SERVICE
//        NotificationSettings settings = player.getNotificationSettings();
//        if (settings == null) {
//            // hráč nemá nastavené preference → nic neposíláme
//            log.debug(
//                    "Player {} nemá NotificationSettings – žádné notifikace se neposílají",
//                    player.getId()
//            );
//            return;
//        }
//
//        // ================= EMAIL =================
//        if (settings.isEmailEnabled() && player.getUser() != null) {
//            sendEmail(player, type, context);
//        }
//
//        // ================= SMS =================
//        if (settings.isSmsEnabled() && player.getPhoneNumber() != null) {
//            sendSms(player, type, context);
//        }

        // NOVÁ LOGIKA – PREFERENCES PŘES AppUserSettings + PlayerSettingsEntity
        NotificationDecision decision = notificationPreferencesService.evaluate(player, type);

        // EMAILY
        // USER
        if (decision.isSendEmailToUser() && decision.getUserEmail() != null) {
            sendEmailToUser(decision.getUserEmail(), player, type, context);
        }
        // PLAYER
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

    /**
     * Odeslání emailu uživateli (AppUser) – typicky na email účtu.
     */
    private void sendEmailToUser(String email,
                                 PlayerEntity player,
                                 NotificationType type,
                                 Object context) {

        if (email == null || email.isBlank()) {
            log.debug("sendEmailToUser: prázdný email, nic se neposílá");
            return;
        }

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
            // účetní / bezpečnostní věci – můžeš doplnit vlastní texty
            case PASSWORD_RESET -> emailService.sendSimpleEmail(
                    email,
                    "Reset hesla",
                    "Byl proveden reset vašeho hesla. Pokud jste o něj nežádal(a), kontaktujte podporu."
            );

            case SECURITY_ALERT -> emailService.sendSimpleEmail(
                    email,
                    "Bezpečnostní upozornění",
                    "Byla zaznamenána neobvyklá aktivita na vašem účtu."
            );


            // další typy – podle potřeby
            default -> log.debug(
                    "Typ {} nemá definovanou email notifikaci pro uživatele, nic se neposílá",
                    type
            );
        }
    }

    /**
     * Odeslání emailu hráči (na jeho kontakt z PlayerSettings).
     *
     * Zatím může používat stejné texty jako pro uživatele,
     * nebo je můžeš časem odlišit.
     */
    private void sendEmailToPlayer(String email,
                                   PlayerEntity player,
                                   NotificationType type,
                                   Object context) {

        if (email == null || email.isBlank()) {
            log.debug("sendEmailToPlayer: prázdný email, nic se neposílá");
            return;
        }

        // Pro začátek můžeme použít stejné šablony jako pro uživatele:
        sendEmailToUser(email, player, type, context);


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
    private void sendSmsToPhone(String phone,
                                PlayerEntity player,
                                NotificationType type,
                                Object context) {

        if (phone == null || phone.isBlank()) {
            log.debug("sendSmsToPhone: prázdný telefon – SMS se nepošle (player {})", player.getId());
            return;
        }

        switch (type) {

            // Registrace / odhlášení / přesun ve frontě / omluvy
            case MATCH_REGISTRATION_CREATED,
                 MATCH_REGISTRATION_UPDATED,
                 MATCH_REGISTRATION_CANCELED,
                 MATCH_REGISTRATION_RESERVED,
                 MATCH_WAITING_LIST_MOVED_UP,
                 PLAYER_EXCUSED,
                 PLAYER_NO_EXCUSED -> {

                if (!(context instanceof MatchRegistrationEntity registration)) {
                    log.warn(
                            "NotificationType {} očekává MatchRegistrationEntity v context, ale dostal {}",
                            type,
                            (context != null ? context.getClass().getName() : "null")
                    );
                    return;
                }

                // Můžeš podle typu zvolit různé buildery, zatím necháme jeden:
                String msg = smsMessageBuilder.buildMessageRegistration(registration);
                smsService.sendSms(phone, msg);
            }

            // Obecné info / připomínky k zápasu
            case MATCH_REMINDER,
                 MATCH_CANCELED,
                 MATCH_TIME_CHANGED -> {

                if (!(context instanceof MatchEntity match)) {
                    log.warn(
                            "NotificationType {} očekává MatchEntity v context, ale dostal {}",
                            type,
                            (context != null ? context.getClass().getName() : "null")
                    );
                    return;
                }

                String msg = smsMessageBuilder.buildMessageMatchInfo(type, match);
                smsService.sendSms(phone, msg);
            }

            // tyto typy přes SMS neposíláme – jsou čistě emailové
            case PLAYER_CREATED,
                 PLAYER_UPDATED,
                 PLAYER_APPROVED,
                 PLAYER_REJECTED,
                 USER_UPDATED,
                 PASSWORD_RESET,
                 SECURITY_ALERT -> log.debug(
                    "Typ {} nemá definovanou SMS notifikaci, nic se neposílá",
                    type
            );

            default -> log.debug(
                    "Neznámý NotificationType {} – SMS se neposílá",
                    type
            );
        }
    }


}
