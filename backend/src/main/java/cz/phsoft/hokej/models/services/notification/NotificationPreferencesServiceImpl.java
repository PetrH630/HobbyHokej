package cz.phsoft.hokej.models.services.notification;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.AppUserSettingsEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.entities.PlayerSettingsEntity;
import cz.phsoft.hokej.data.enums.GlobalNotificationLevel;
import cz.phsoft.hokej.data.enums.NotificationCategory;
import cz.phsoft.hokej.data.enums.NotificationType;
import cz.phsoft.hokej.models.services.NotificationDecision;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Implementace NotificationPreferencesService.
 *
 * Služba vyhodnocuje:
 * - nastavení hráče (PlayerSettingsEntity),
 * - nastavení uživatele (AppUserSettingsEntity),
 * - globální úroveň notifikací uživatele (GlobalNotificationLevel),
 * - konkrétní typ notifikace (NotificationType).
 *
 * Výsledkem je NotificationDecision, které definuje,
 * komu a jakými kanály bude notifikace doručena.
 */
@Service
public class NotificationPreferencesServiceImpl implements NotificationPreferencesService {

    @Override
    public NotificationDecision evaluate(PlayerEntity player,
                                         NotificationType type) {

        NotificationDecision decision = new NotificationDecision();

        if (player == null || type == null) {
            return decision;
        }

        PlayerSettingsEntity playerSettings = player.getSettings();
        AppUserEntity user = player.getUser();
        AppUserSettingsEntity userSettings = (user != null ? user.getSettings() : null);

        // Zdrojové kontakty

        // Email hráče – preferuje se PlayerSettings.contactEmail,
        // případně se použije email uživatele, pokud je to vhodné.
        String playerEmail = null;
        if (playerSettings != null && StringUtils.hasText(playerSettings.getContactEmail())) {
            playerEmail = playerSettings.getContactEmail();
        } else if (user != null && StringUtils.hasText(user.getEmail())) {
            // Fallback: pokud hráč nemá vlastní e-mail, lze použít e-mail uživatele.
            playerEmail = user.getEmail();
        }

        // Email uživatele (účtu).
        String userEmail = (user != null ? user.getEmail() : null);

        // Telefon hráče – preferuje se PlayerSettings.contactPhone,
        // fallback je případný phoneNumber na PlayerEntity.
        String playerPhone = null;
        if (playerSettings != null && StringUtils.hasText(playerSettings.getContactPhone())) {
            playerPhone = playerSettings.getContactPhone();
        } else if (StringUtils.hasText(player.getPhoneNumber())) {
            playerPhone = player.getPhoneNumber();
        }

        // Globální nastavení uživatele

        GlobalNotificationLevel globalLevel =
                (userSettings != null && userSettings.getGlobalNotificationLevel() != null)
                        ? userSettings.getGlobalNotificationLevel()
                        : GlobalNotificationLevel.ALL;

        // Zda globální úroveň vůbec povoluje tento konkrétní NotificationType.
        boolean userGlobalAllowsThisType = isGloballyEnabledForType(type, globalLevel);

        boolean copyAllToUserEmail =
                userSettings == null || userSettings.isCopyAllPlayerNotificationsToUserEmail();

        boolean includePlayersWithOwnEmail =
                userSettings != null && userSettings.isReceiveNotificationsForPlayersWithOwnEmail();

        // Nastavení hráče – povolené kanály

        boolean emailChannelEnabled = (playerSettings == null) || playerSettings.isEmailEnabled();
        boolean smsChannelEnabled = (playerSettings != null) && playerSettings.isSmsEnabled();

        // Zda je typ notifikace povolen pro hráče.
        boolean typeEnabledForPlayer = isTypeEnabledForPlayer(type, playerSettings);

        // Rozhodování podle kategorie notifikace

        NotificationCategory category = type.getCategory();

        switch (category) {

            // Systémové typy – primárně směřují na účet (user.email).
            case SYSTEM -> {
                if (user != null
                        && StringUtils.hasText(userEmail)
                        && userGlobalAllowsThisType) {

                    decision.setSendEmailToUser(true);
                    decision.setUserEmail(userEmail);
                }
            }

            // Registrace, omluvy, zápasové informace.
            case REGISTRATION, MATCH_INFO -> {

                // E-mail hráči.
                if (emailChannelEnabled
                        && typeEnabledForPlayer
                        && StringUtils.hasText(playerEmail)) {

                    decision.setSendEmailToPlayer(true);
                    decision.setPlayerEmail(playerEmail);
                }

                // SMS hráči.
                if (smsChannelEnabled
                        && typeEnabledForPlayer
                        && StringUtils.hasText(playerPhone)) {

                    decision.setSendSmsToPlayer(true);
                    decision.setPlayerPhone(playerPhone);
                }

                // E-mail uživateli jako kopie.
                if (user != null
                        && StringUtils.hasText(userEmail)
                        && userGlobalAllowsThisType
                        && copyAllToUserEmail
                        && (includePlayersWithOwnEmail || !hasOwnPlayerEmail(playerSettings))) {

                    // Uživatel dostane kopii, pokud:
                    // - chce kopie (copyAllToUserEmail),
                    // - globální úroveň mu tento typ neblokuje,
                    // - a buď hráč nemá vlastní e-mail,
                    //   nebo uživatel výslovně chce kopie i pro hráče s vlastním e-mailem.
                    decision.setSendEmailToUser(true);
                    decision.setUserEmail(userEmail);
                }
            }

            // Ostatní kategorie se explicitně nezpracovávají.
            default -> {
                // Nezpracovaná kategorie – raději neposílat nic.
            }
        }

        return decision;
    }

    /**
     * Určuje, zda globální nastavení uživatele povoluje daný NotificationType.
     *
     * NONE           -> nepovoluje žádné notifikace
     * ALL            -> povoluje všechny notifikace
     * IMPORTANT_ONLY -> povoluje pouze typy označené jako důležité
     */
    private boolean isGloballyEnabledForType(NotificationType type,
                                             GlobalNotificationLevel level) {

        return switch (level) {
            case NONE -> false;
            case ALL -> true;
            case IMPORTANT_ONLY -> type.isImportant();
        };
    }

    /**
     * Zjistí, zda má hráč vlastní e-mail v PlayerSettings (contactEmail).
     *
     * Používá se při rozhodování, zda posílat kopii na e-mail uživatele.
     */
    private boolean hasOwnPlayerEmail(PlayerSettingsEntity playerSettings) {
        return playerSettings != null && StringUtils.hasText(playerSettings.getContactEmail());
    }

    /**
     * Zjistí, zda je konkrétní typ notifikace povolen pro daného hráče.
     *
     * Pokud playerSettings == null:
     * - většina typů je povolena (zpětná kompatibilita),
     * - MATCH_REMINDER je výchozím chováním vypnutý
     *   (odpovídá defaultu notifyReminders = false).
     */
    private boolean isTypeEnabledForPlayer(NotificationType type,
                                           PlayerSettingsEntity playerSettings) {

        if (playerSettings == null) {
            return switch (type) {
                case MATCH_REMINDER -> false; // bez explicitního nastavení neposílat
                default -> true;
            };
        }

        return switch (type) {
            // REGISTRATION
            case MATCH_REGISTRATION_CREATED,
                 MATCH_REGISTRATION_UPDATED,
                 MATCH_REGISTRATION_CANCELED,
                 MATCH_REGISTRATION_RESERVED,
                 MATCH_REGISTRATION_SUBSTITUTE,
                 MATCH_WAITING_LIST_MOVED_UP,
                 MATCH_REGISTRATION_NO_RESPONSE,
                 PLAYER_EXCUSED,
                 PLAYER_NO_EXCUSED -> playerSettings.isRegistrationNotificationsEnabled();

            // MATCH_INFO
            case MATCH_REMINDER -> playerSettings.isNotifyReminders();
            case MATCH_CANCELED -> playerSettings.isNotifyOnMatchCancel();
            case MATCH_UNCANCELED,
                 MATCH_TIME_CHANGED -> playerSettings.isNotifyOnMatchChange();

            // SYSTEM (a ostatní, které nejsou výše výslovně vyjmenované)
            default -> playerSettings.isSystemNotificationsEnabled();
        };
    }
}