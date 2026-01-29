package cz.phsoft.hokej.models.dto;

/**
 * DTO pro nastavení uživatele (AppUserSettingsEntity).
 *
 * Slouží pro přenos dat mezi backendem a frontendem.
 */
public class AppUserSettingsDTO {

    // ---- výběr hráče ----

    /**
     * Způsob automatického výběru hráče.
     *
     * Hodnoty odpovídají enumu PlayerSelectionMode:
     * - FIRST_PLAYER
     * - ALWAYS_CHOOSE
     */
    private String playerSelectionMode;

    // ---- globální notifikace ----

    /**
     * Globální úroveň notifikací pro uživatele.
     *
     * Hodnoty odpovídají enumu GlobalNotificationLevel:
     * - ALL
     * - IMPORTANT_ONLY
     * - NONE
     */
    private String globalNotificationLevel;

    /**
     * Má uživatel dostávat kopie všech notifikací
     * svých hráčů na svůj email?
     */
    private boolean copyAllPlayerNotificationsToUserEmail;

    /**
     * Má uživatel dostávat notifikace i za hráče,
     * kteří mají vlastní email?
     */
    private boolean receiveNotificationsForPlayersWithOwnEmail;

    /**
     * Má uživatel preferovat denní souhrn (digest)
     * místo jednotlivých notifikací?
     */
    private boolean emailDigestEnabled;

    /**
     * Čas, kdy má chodit souhrnný email (pokud je digest aktivní).
     * Reprezentovaný jako String, např. "20:00".
     */
    private String emailDigestTime;

    // ---- UX / UI ----

    /**
     * Preferovaný jazyk UI (např. "cs", "en").
     */
    private String uiLanguage;

    /**
     * Časová zóna (např. "Europe/Prague").
     */
    private String timezone;

    /**
     * Výchozí obrazovka po přihlášení.
     * Např. "DASHBOARD", "MATCHES", "PLAYERS".
     */
    private String defaultLandingPage;

    // ===== Gettery / settery =====

    public String getPlayerSelectionMode() {
        return playerSelectionMode;
    }

    public void setPlayerSelectionMode(String playerSelectionMode) {
        this.playerSelectionMode = playerSelectionMode;
    }

    public String getGlobalNotificationLevel() {
        return globalNotificationLevel;
    }

    public void setGlobalNotificationLevel(String globalNotificationLevel) {
        this.globalNotificationLevel = globalNotificationLevel;
    }

    public boolean isCopyAllPlayerNotificationsToUserEmail() {
        return copyAllPlayerNotificationsToUserEmail;
    }

    public void setCopyAllPlayerNotificationsToUserEmail(boolean copyAllPlayerNotificationsToUserEmail) {
        this.copyAllPlayerNotificationsToUserEmail = copyAllPlayerNotificationsToUserEmail;
    }

    public boolean isReceiveNotificationsForPlayersWithOwnEmail() {
        return receiveNotificationsForPlayersWithOwnEmail;
    }

    public void setReceiveNotificationsForPlayersWithOwnEmail(boolean receiveNotificationsForPlayersWithOwnEmail) {
        this.receiveNotificationsForPlayersWithOwnEmail = receiveNotificationsForPlayersWithOwnEmail;
    }

    public boolean isEmailDigestEnabled() {
        return emailDigestEnabled;
    }

    public void setEmailDigestEnabled(boolean emailDigestEnabled) {
        this.emailDigestEnabled = emailDigestEnabled;
    }

    public String getEmailDigestTime() {
        return emailDigestTime;
    }

    public void setEmailDigestTime(String emailDigestTime) {
        this.emailDigestTime = emailDigestTime;
    }

    public String getUiLanguage() {
        return uiLanguage;
    }

    public void setUiLanguage(String uiLanguage) {
        this.uiLanguage = uiLanguage;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getDefaultLandingPage() {
        return defaultLandingPage;
    }

    public void setDefaultLandingPage(String defaultLandingPage) {
        this.defaultLandingPage = defaultLandingPage;
    }
}
