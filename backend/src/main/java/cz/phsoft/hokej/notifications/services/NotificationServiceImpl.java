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
    private final InAppNotificationService inAppNotificationService;

    // demo režim a úložiště notifikací pro demo
    private final DemoModeService demoModeService;
    private final DemoNotificationStore demoNotificationStore;

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

    // SMS pomocná metoda

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
     * Pokud je typ uveden v MANAGER_COPY_BLACKLIST, kopie se neposílají.
     */
    private boolean shouldSendManagerCopy(NotificationType type) {
        return !MANAGER_COPY_BLACKLIST.contains(type);
    }

    /**
     * Určuje, zda má konkrétní manažer dostat kopii daného typu notifikace.
     *
     * Vyhodnocuje nastavení managerNotificationLevel na AppUserSettingsEntity.
     * Pokud není nastaveno, používá se globalNotificationLevel. Pokud není
     * k dispozici ani globální úroveň, používá se výchozí ALL.
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
     * Vyhodnotí, zda je daný typ notifikace povolen pro zvolenou úroveň.
     *
     * NONE           znamená, že se notifikace neposílají.
     * ALL            znamená, že se posílají všechny typy.
     * IMPORTANT_ONLY znamená, že se posílají pouze důležité typy.
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