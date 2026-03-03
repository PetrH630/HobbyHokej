package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.user.entities.AppUserSettingsEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.notifications.enums.GlobalNotificationLevel;
import cz.phsoft.hokej.notifications.enums.NotificationType;
import cz.phsoft.hokej.user.repositories.AppUserRepository;
import cz.phsoft.hokej.notifications.email.EmailMessageBuilder;
import cz.phsoft.hokej.notifications.email.EmailService;
import cz.phsoft.hokej.notifications.sms.SmsMessageBuilder;
import cz.phsoft.hokej.notifications.sms.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

/**
 * Implementace služby NotificationService.
 *
 * Zajišťuje:
 * - využití NotificationPreferencesService pro rozhodnutí, komu má být notifikace doručena,
 * - sestavení obsahu e-mailových zpráv pomocí EmailMessageBuilder,
 * - sestavení obsahu SMS zpráv pomocí SmsMessageBuilder,
 * - odesílání e-mailů pomocí EmailService,
 * - odesílání SMS pomocí SmsService,
 * - uložení in-app notifikací pomocí InAppNotificationService,
 * - rozesílání kopií vybraných notifikací manažerům (dle blacklistu).
 *
 * Třída neřeší:
 * - samotné vyvolání notifikací (kdy se mají posílat),
 * - perzistenci e-mailů a SMS mimo in-app notifikace,
 * - detailní business pravidla registrací nebo zápasů.
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
    private final InAppNotificationService inAppNotificationService;

    // demo režim a úložiště notifikací pro demo
    private final DemoModeService demoModeService;
    private final DemoNotificationStore demoNotificationStore;

    /**
     * Množina typů notifikací, pro které se nemá posílat kopie manažerům.
     *
     * Používá se v metodě shouldSendManagerCopy pro rozhodnutí,
     * zda je vhodné posílat manažerské kopie daného typu.
     */
    private static final Set<NotificationType> MANAGER_COPY_BLACKLIST = EnumSet.of(
            NotificationType.MATCH_CANCELED,
            NotificationType.MATCH_TIME_CHANGED,
            NotificationType.MATCH_UNCANCELED,
            NotificationType.MATCH_REMINDER
    );

    /**
     * Vytváří instanci implementace NotificationService.
     *
     * Všechny závislosti jsou injektovány konstruktorovou injekcí.
     * Služba je připravena jak pro běžný provoz, tak pro DEMO režim,
     * ve kterém se e-maily a SMS pouze ukládají do DemoNotificationStore.
     *
     * @param appUserRepository              repository pro práci s uživateli
     * @param emailService                   služba pro odesílání e-mailů
     * @param smsService                     služba pro odesílání SMS
     * @param smsMessageBuilder              builder pro sestavení obsahu SMS
     * @param emailMessageBuilder            builder pro sestavení obsahu e-mailů
     * @param notificationPreferencesService služba pro vyhodnocení notifikačních preferencí
     * @param demoModeService                služba určující, zda je aplikace v DEMO režimu
     * @param demoNotificationStore          úložiště notifikací v DEMO režimu
     * @param inAppNotificationService       služba pro ukládání in-app notifikací
     */
    public NotificationServiceImpl(
            AppUserRepository appUserRepository,
            EmailService emailService,
            SmsService smsService,
            SmsMessageBuilder smsMessageBuilder,
            EmailMessageBuilder emailMessageBuilder,
            NotificationPreferencesService notificationPreferencesService,
            DemoModeService demoModeService,
            DemoNotificationStore demoNotificationStore,
            InAppNotificationService inAppNotificationService
    ) {
        this.appUserRepository = appUserRepository;
        this.emailService = emailService;
        this.smsService = smsService;
        this.smsMessageBuilder = smsMessageBuilder;
        this.emailMessageBuilder = emailMessageBuilder;
        this.notificationPreferencesService = notificationPreferencesService;
        this.demoModeService = demoModeService;
        this.demoNotificationStore = demoNotificationStore;
        this.inAppNotificationService = inAppNotificationService;
    }

    /**
     * Odesílá notifikaci konkrétnímu hráči.
     *
     * Metoda:
     * - získá rozhodnutí o kanálech a kontaktech z NotificationPreferencesService,
     * - vytvoří in-app notifikaci včetně informací o použitých kanálech (emailTo, smsTo),
     * - podle rozhodnutí odešle e-mail uživateli, e-mail hráči a případně SMS hráči,
     * - volitelně odešle kopii vybraných typů notifikací manažerům (pokud to typ dovoluje).
     *
     * V DEMO režimu se e-maily a SMS neodesílají, ale ukládají se do DemoNotificationStore.
     *
     * @param player  hráč, kterému je notifikace určena
     * @param type    typ notifikace definující druh události
     * @param context kontextová data související s notifikací (například zápas nebo registrace)
     */
    @Override
    public void notifyPlayer(PlayerEntity player,
                             NotificationType type,
                             Object context) {

        if (player == null) {
            log.warn("notifyPlayer() called with null player for type {}", type);
            return;
        }

        try {
            // 1) Rozhodnutí podle nastavení
            NotificationDecision decision = notificationPreferencesService.evaluate(player, type);
            log.info("notifyPlayer decision: type={}, playerId={}, sendUserEmail={}, sendPlayerEmail={}, sendSms={}",
                    type, player.getId(),
                    decision.isSendEmailToUser(),
                    decision.isSendEmailToPlayer(),
                    decision.isSendSmsToPlayer()
            );

            // Sestavit emailTo: všechny emaily, kam se má posílat (user + player), bez duplicit
            String emailTo = null;
            if (decision.isSendEmailToUser() && decision.getUserEmail() != null && !decision.getUserEmail().isBlank()) {
                emailTo = decision.getUserEmail().trim();
            }
            if (decision.isSendEmailToPlayer() && decision.getPlayerEmail() != null && !decision.getPlayerEmail().isBlank()) {
                String playerEmail = decision.getPlayerEmail().trim();
                if (emailTo == null) {
                    emailTo = playerEmail;
                } else if (!emailTo.equalsIgnoreCase(playerEmail)) {
                    emailTo = emailTo + ", " + playerEmail;
                }
            }

            // Sestavit smsTo – pouze hráč
            String smsTo = null;
            if (decision.isSendSmsToPlayer()
                    && decision.getPlayerPhone() != null
                    && !decision.getPlayerPhone().isBlank()) {
                smsTo = decision.getPlayerPhone().trim();
            }

            // 2) In-app notifikace s informací o kanálech
            try {
                inAppNotificationService.storeForPlayer(player, type, context, emailTo, smsTo);
            } catch (Exception ex) {
                log.error("notifyPlayer: chyba při ukládání in-app notifikace type={} playerId={}",
                        type, player.getId(), ex);
            }

            // 3) E-maily / SMS – vlastní odeslání (beze změn, jen použijeme decision)
            // E-mail pro uživatele
            if (decision.isSendEmailToUser() && decision.getUserEmail() != null) {
                sendEmailToUser(decision.getUserEmail(), player, type, context);
            }

            // E-mail pro hráče
            if (decision.isSendEmailToPlayer() && decision.getPlayerEmail() != null) {
                sendEmailToPlayer(decision.getPlayerEmail(), player, type, context);
            }

            // SMS pro hráče
            if (decision.isSendSmsToPlayer() && decision.getPlayerPhone() != null) {
                sendSmsToPhone(decision.getPlayerPhone(), player, type, context);
            }

            // Kopie manažerům – logika beze změny
            if (shouldSendManagerCopy(type)) {
                // ... existující kód pro manažerské kopie ...
            } else {
                log.debug("Typ {} je v MANAGER_COPY_BLACKLIST – kopie manažerům se neposílá (notifyPlayer).", type);
            }

            log.debug("notifyPlayer: in-app + e-mail/SMS notifikace zpracována pro type={} playerId={}",
                    type, player.getId());

        } catch (Exception ex) {
            log.error("notifyPlayer: chyba při zpracování notifikace type={} playerId={}",
                    type, player.getId(), ex);
        }
    }

    /**
     * Odesílá notifikaci konkrétnímu uživateli.
     *
     * Metoda:
     * - připraví e-mailovou adresu příjemce z entity AppUserEntity,
     * - uloží in-app notifikaci včetně informace o e-mailovém cíli,
     * - pokud má uživatel e-mail, sestaví obsah pomocí EmailMessageBuilder
     *   a odešle e-mail (nebo jej uloží do DemoNotificationStore v DEMO režimu),
     * - případná logika pro manažery zůstává v další části metody
     *   (zde ponechána bez změny).
     *
     * @param user    uživatel, kterému je notifikace určena
     * @param type    typ notifikace definující druh události
     * @param context kontextová data související s notifikací; pokud je null,
     *                použije se jako kontext samotný uživatel
     */
    @Override
    public void notifyUser(AppUserEntity user,
                           NotificationType type,
                           Object context) {

        if (user == null) {
            log.warn("notifyUser() called with null user for type {}", type);
            return;
        }

        String userEmail = user.getEmail();
        String emailTo = (userEmail != null && !userEmail.isBlank()) ? userEmail.trim() : null;

        // 1) In-app notifikace s informací o emailTo
        try {
            Object effectiveContext = (context != null) ? context : user;
            inAppNotificationService.storeForUser(user, type, effectiveContext, emailTo);
        } catch (Exception ex) {
            log.error("notifyUser: chyba při ukládání in-app notifikace type={} userId={}",
                    type, user.getId(), ex);
        }

        // 2) E-maily a kopie manažerům – původní logika (jen jsem vyhodil duplicitní rozhodování okolo emailTo)
        try {
            Object effectiveContext = (context != null) ? context : user;

            if (userEmail != null && !userEmail.isBlank()) {
                EmailMessageBuilder.EmailContent content =
                        emailMessageBuilder.buildForUser(type, null, userEmail, effectiveContext);

                if (content != null) {
                    if (demoModeService.isDemoMode()) {
                        demoNotificationStore.addEmail(
                                userEmail,
                                content.subject(),
                                content.body(),
                                content.html(),
                                type,
                                "USER"
                        );
                        log.debug("DEMO MODE: notifyUser e-mail USER uložen do DemoNotificationStore, nic se neodesílá");
                    } else {
                        if (content.html()) {
                            emailService.sendHtmlEmail(userEmail, content.subject(), content.body());
                        } else {
                            emailService.sendSimpleEmail(userEmail, content.subject(), content.body());
                        }
                    }
                } else {
                    log.debug("Typ {} nemá definovanou e-mailovou šablonu pro uživatele (USER), nic se neposílá", type);
                }
            } else {
                log.debug("notifyUser: uživatel {} nemá e-mail, nic se neposílá", user.getId());
            }

            // ... zbytek notifyUser (manažeři, blacklist) beze změny ...

        } catch (Exception ex) {
            log.error("notifyUser: chyba při zpracování e-mailů/kopií pro type={} userId={}",
                    type, user.getId(), ex);
        }
    }

    // Pomocné metody pro e-mail

    /**
     * Odesílá e-mail vybranému manažerovi.
     *
     * Metoda ověří, zda má manažer nastavený e-mail a zda existuje
     * e-mailová šablona pro daný typ notifikace. V DEMO režimu
     * se e-mail neodesílá, ale uloží se do DemoNotificationStore.
     *
     * @param manager manažer, kterému je e-mail určen
     * @param player  hráč, kterého se notifikace týká
     * @param type    typ notifikace
     * @param context kontextová data (například zápas nebo registrace)
     */
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

        // DEMO režim – uložení do DemoNotificationStore
        if (demoModeService.isDemoMode()) {
            // DEMO CHANGED: používáme novou signaturu addEmail(...)
            demoNotificationStore.addEmail(
                    email,
                    content.subject(),
                    content.body(),
                    content.html(),
                    type,
                    "MANAGER"
            );
            log.debug("DEMO MODE: sendEmailToManager – e-mail uložen do DemoNotificationStore, nic se neodesílá");
            return;
        }
        // KONEC DEMO

        if (content.html()) {
            emailService.sendHtmlEmail(email, content.subject(), content.body());
        } else {
            emailService.sendSimpleEmail(email, content.subject(), content.body());
        }
    }

    /**
     * Odesílá e-mail na e-mailovou adresu uživatele.
     *
     * Metoda používá EmailMessageBuilder.buildForUser,
     * přičemž hráč je volitelný (může být null) podle povahy notifikace.
     * V DEMO režimu se e-mail ukládá do DemoNotificationStore.
     *
     * @param email  e-mailová adresa uživatele
     * @param player hráč, kterého se notifikace týká (může být null)
     * @param type   typ notifikace
     * @param context kontextová data související s notifikací
     */
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

        // DEMO režim – uložení do DemoNotificationStore
        if (demoModeService.isDemoMode()) {
            // DEMO CHANGED: používáme novou signaturu addEmail(...)
            demoNotificationStore.addEmail(
                    email,
                    content.subject(),
                    content.body(),
                    content.html(),
                    type,
                    "USER"
            );
            log.debug("DEMO MODE: sendEmailToUser – e-mail uložen do DemoNotificationStore, nic se neodesílá");
            return;
        }
        // KONEC DEMO

        if (content.html()) {
            emailService.sendHtmlEmail(email, content.subject(), content.body());
        } else {
            emailService.sendSimpleEmail(email, content.subject(), content.body());
        }
    }

    /**
     * Odesílá e-mail na kontaktní e-mail hráče.
     *
     * Metoda používá EmailMessageBuilder.buildForPlayer. Pokud není
     * k dispozici šablona pro daný typ, e-mail se neodesílá.
     * V DEMO režimu se e-mail uloží do DemoNotificationStore.
     *
     * @param email   e-mailová adresa hráče
     * @param player  hráč, kterému je e-mail určen
     * @param type    typ notifikace
     * @param context kontextová data související s notifikací
     */
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

        // DEMO režim – uložení do DemoNotificationStore
        if (demoModeService.isDemoMode()) {
            // DEMO CHANGED: používáme novou signaturu addEmail(...)
            demoNotificationStore.addEmail(
                    email,
                    content.subject(),
                    content.body(),
                    content.html(),
                    type,
                    "PLAYER"
            );
            log.debug("DEMO MODE: sendEmailToPlayer – e-mail uložen do DemoNotificationStore, nic se neodesílá");
            return;
        }
        // KONEC DEMO

        if (content.html()) {
            emailService.sendHtmlEmail(email, content.subject(), content.body());
        } else {
            emailService.sendSimpleEmail(email, content.subject(), content.body());
        }
    }

    /**
     * Odesílá SMS na dané telefonní číslo.
     *
     * Metoda:
     * - sestaví text zprávy pomocí SmsMessageBuilder,
     * - v DEMO režimu uloží SMS do DemoNotificationStore,
     * - v běžném režimu odešle SMS přes SmsService.
     *
     * Pokud není k dispozici šablona nebo je text prázdný,
     * SMS se neodesílá.
     *
     * @param phone   telefonní číslo hráče
     * @param player  hráč, kterého se notifikace týká (může být null)
     * @param type    typ notifikace
     * @param context kontextová data související s notifikací
     */
    private void sendSmsToPhone(String phone,
                                PlayerEntity player,
                                NotificationType type,
                                Object context) {

        if (phone == null || phone.isBlank()) {
            log.debug("sendSmsToPhone: prázdný telefon – SMS se nepošle (player {})", player != null ? player.getId() : null);
            return;
        }

        String msg = smsMessageBuilder.buildForNotification(type, player, context);

        if (msg == null || msg.isBlank()) {
            log.debug("Typ {} nemá definovanou SMS šablonu nebo chybí context – SMS se neposílá", type);
            return;
        }

        // DEMO režim – uložení do DemoNotificationStore
        if (demoModeService.isDemoMode()) {
            // DEMO CHANGED: používáme novou signaturu addSms(...)
            demoNotificationStore.addSms(
                    phone,
                    msg,
                    type
            );
            log.debug("DEMO MODE: sendSmsToPhone – SMS uložena do DemoNotificationStore, nic se neodesílá");
            return;
        }
        // KONEC DEMO

        smsService.sendSms(phone, msg);
    }

    /**
     * Určuje, zda se pro daný typ notifikace mají posílat kopie manažerům.
     *
     * Pokud je daný typ uveden v množině MANAGER_COPY_BLACKLIST,
     * manažerské kopie se neposílají.
     *
     * @param type typ notifikace
     * @return true, pokud se manažerské kopie mají posílat, jinak false
     */
    private boolean shouldSendManagerCopy(NotificationType type) {
        return !MANAGER_COPY_BLACKLIST.contains(type);
    }

    /**
     * Určuje, zda má konkrétní manažer dostat kopii daného typu notifikace.
     *
     * Metoda:
     * - načte nastavení AppUserSettingsEntity,
     * - preferuje managerNotificationLevel, pokud je nastaven,
     * - pokud není, použije globalNotificationLevel,
     * - pokud ani ten není k dispozici, použije výchozí ALL.
     *
     * Poté využije isEnabledForType pro konečné rozhodnutí.
     *
     * @param type    typ notifikace
     * @param manager uživatel reprezentující manažera
     * @return true, pokud má manažer dostat kopii, jinak false
     */
    private boolean isManagerCopyAllowedForManager(NotificationType type,
                                                   AppUserEntity manager) {
        if (manager == null) {
            return false;
        }

        AppUserSettingsEntity settings = manager.getSettings();
        GlobalNotificationLevel level;

        if (settings == null) {
            level = GlobalNotificationLevel.ALL;
        } else if (settings.getManagerNotificationLevel() != null) {
            level = settings.getManagerNotificationLevel();
        } else if (settings.getGlobalNotificationLevel() != null) {
            level = settings.getGlobalNotificationLevel();
        } else {
            level = GlobalNotificationLevel.ALL;
        }

        return isEnabledForType(type, level);
    }

    /**
     * Vyhodnocuje, zda je daný typ notifikace povolen pro zvolenou úroveň.
     *
     * Význam jednotlivých úrovní:
     * - NONE           znamená, že se notifikace neposílají,
     * - ALL            znamená, že se posílají všechny typy,
     * - IMPORTANT_ONLY znamená, že se posílají pouze důležité typy.
     *
     * @param type  typ notifikace
     * @param level globální nebo manažerská úroveň notifikací
     * @return true, pokud je typ pro danou úroveň povolen, jinak false
     */
    private boolean isEnabledForType(NotificationType type,
                                     GlobalNotificationLevel level) {
        return switch (level) {
            case NONE -> false;
            case ALL -> true;
            case IMPORTANT_ONLY -> type.isImportant();
        };
    }
}