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
 * Vyhodnocuje:
 * - PlayerSettingsEntity (emailEnabled, smsEnabled, notifyOn..., kategorie)
 * - AppUserSettingsEntity (globalNotificationLevel, kopie emailů, chování pro hráče s vlastním emailem...)
 */
@Service
public class NotificationPreferencesServiceImpl implements NotificationPreferencesService {


    public NotificationDecision evaluate(PlayerEntity player,
                                         NotificationType type) {

        NotificationDecision decision = new NotificationDecision();

        if (player == null || type == null) {
            return decision;
        }

        PlayerSettingsEntity playerSettings = player.getSettings();
        AppUserEntity user = player.getUser();
        AppUserSettingsEntity userSettings = (user != null ? user.getSettings() : null);

        // ===== ZDROJE KONTAKTŮ =====

        // email hráče – preferuj PlayerSettings.contactEmail,
        // případně fallback na email uživatele, pokud chceme
        String playerEmail = null;
        if (playerSettings != null && StringUtils.hasText(playerSettings.getContactEmail())) {
            playerEmail = playerSettings.getContactEmail();
        } else if (user != null && StringUtils.hasText(user.getEmail())) {
            // fallback: pokud nemá vlastní email, můžeme použít email uživatele
            playerEmail = user.getEmail();
        }

        // email uživatele (účtu)
        String userEmail = (user != null ? user.getEmail() : null);

        // telefon hráče – preferuj PlayerSettings.contactPhone,
        // fallback na případný player.getPhoneNumber()
        String playerPhone = null;
        if (playerSettings != null && StringUtils.hasText(playerSettings.getContactPhone())) {
            playerPhone = playerSettings.getContactPhone();
        } else if (StringUtils.hasText(player.getPhoneNumber())) {
            playerPhone = player.getPhoneNumber();
        }



        // ===== GLOBALNÍ NASTAVENÍ USERA =====

        GlobalNotificationLevel globalLevel =
                (userSettings != null && userSettings.getGlobalNotificationLevel() != null)
                        ? userSettings.getGlobalNotificationLevel()
                        : GlobalNotificationLevel.ALL;

        // zda globální level vůbec povoluje tento konkrétní NotificationType
        boolean userGlobalAllowsThisType = isGloballyEnabledForType(type, globalLevel);

        boolean copyAllToUserEmail =
                userSettings == null || userSettings.isCopyAllPlayerNotificationsToUserEmail();

        boolean includePlayersWithOwnEmail =
                userSettings != null && userSettings.isReceiveNotificationsForPlayersWithOwnEmail();

        // ===== PLAYER SETTINGS – KANÁLY + KATEGORIE =====

        boolean emailChannelEnabled = (playerSettings == null) || playerSettings.isEmailEnabled();
        boolean smsChannelEnabled = (playerSettings != null) && playerSettings.isSmsEnabled();

        // kategorie povolená pro hráče?
        boolean categoryEnabledForPlayer = isCategoryEnabledForPlayer(type, playerSettings);

        // ===== ROZHODOVÁNÍ DLE TYPE / CATEGORY =====

        NotificationCategory category = type.getCategory();

        switch (category) {

            // ----------------------------------
            // SYSTÉMOVÉ TYPY – jdou primárně na účet (user.email)
            // (PLAYER_CREATED, PLAYER_UPDATED, PLAYER_APPROVED, PLAYER_REJECTED, USER_UPDATED, ...)
            // ----------------------------------
            case SYSTEM -> {
                if (user != null
                        && StringUtils.hasText(userEmail)
                        && userGlobalAllowsThisType) {
                    decision.setSendEmailToUser(true);
                    decision.setUserEmail(userEmail);
                }
                // momentálně neposíláme nic hráči; pokud bys chtěl, můžeš doplnit:
                // if (emailChannelEnabled && categoryEnabledForPlayer && StringUtils.hasText(playerEmail)) ...
            }

            // ----------------------------------
            // REGISTRACE / OMLUVY / ZÁPASOVÉ INFO
            // (PLAYER_REGISTERED, PLAYER_UNREGISTERED, PLAYER_RESERVED, PLAYER_EXCUSED, ...)
            // ----------------------------------
            case REGISTRATION, EXCUSE, MATCH_INFO -> {

                // EMAIL – hráč
                if (emailChannelEnabled
                        && categoryEnabledForPlayer
                        && StringUtils.hasText(playerEmail)) {

                    decision.setSendEmailToPlayer(true);
                    decision.setPlayerEmail(playerEmail);
                }

                // SMS – hráč
                if (smsChannelEnabled
                        && categoryEnabledForPlayer
                        && StringUtils.hasText(playerPhone)) {

                    decision.setSendSmsToPlayer(true);
                    decision.setPlayerPhone(playerPhone);
                }

                // EMAIL – uživatel (kopie)
                if (user != null
                        && StringUtils.hasText(userEmail)
                        && userGlobalAllowsThisType
                        && copyAllToUserEmail
                        && (includePlayersWithOwnEmail || !hasOwnPlayerEmail(playerSettings))) {

                    // Uživatel dostane kopii, pokud:
                    // - chce kopie (copyAllToUserEmail),
                    // - global level mu tento typ neblokuje,
                    // - a buď hráč nemá vlastní email (contactEmail),
                    //   nebo user výslovně chce kopie i pro hráče s vlastním emailem.
                    decision.setSendEmailToUser(true);
                    decision.setUserEmail(userEmail);
                }
            }

            // ----------------------------------
            // OSTATNÍ – raději explicitně nic
            // ----------------------------------
            default -> {
                // nezpracovaná kategorie – raději neposílat nic
            }
        }

        return decision;
    }

    /**
     * Určuje, zda globální nastavení uživatele povoluje daný NotificationType.
     *
     * NONE           -> nikdy nic
     * ALL            -> vždy
     * IMPORTANT_ONLY -> jen pokud je typ označen jako "important"
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
     * Kategorie notifikací povolená pro daného hráče?
     *
     * Pokud playerSettings == null -> bereme default: vše povoleno.
     * (chování stejné jako dřív, kdy jsi u null settings bral vše jako TRUE).
     */
    private boolean isCategoryEnabledForPlayer(NotificationType type,
                                               PlayerSettingsEntity playerSettings) {

        if (playerSettings == null) {
            // defaultní chování – hráč nemá nastavení, vše povoleno
            return true;
        }

        return switch (type.getCategory()) {
            case REGISTRATION -> playerSettings.isRegistrationNotificationsEnabled();
            case EXCUSE       -> playerSettings.isExcuseNotificationsEnabled();
            case MATCH_INFO   -> playerSettings.isMatchInfoNotificationsEnabled();
            case SYSTEM       -> playerSettings.isSystemNotificationsEnabled();
        };
    }

    /**
     * Má hráč vlastní email v PlayerSettings (contactEmail)?
     * Používá se při rozhodování, zda posílat kopii na user.email.
     */
    private boolean hasOwnPlayerEmail(PlayerSettingsEntity playerSettings) {
        return playerSettings != null && StringUtils.hasText(playerSettings.getContactEmail());
    }
}
