package cz.phsoft.hokej.models.dto;

/**
 * DTO pro nastavení uživatele na úrovni aplikačního účtu.
 *
 * Reprezentuje datový model entity AppUserSettingsEntity na úrovni API.
 * Slouží pro přenos preferencí uživatele mezi backendem a frontendem,
 * zejména v oblasti výběru hráče, globálních notifikací a uživatelského
 * rozhraní.
 */
public class AppUserSettingsDTO {

    // výběr hráče

    /**
     * Způsob automatického výběru hráče po přihlášení.
     *
     * Hodnoty odpovídají enumu PlayerSelectionMode,
     * například FIRST_PLAYER nebo ALWAYS_CHOOSE.
     */
    private String playerSelectionMode;

    // globální notifikace

    /**
     * Globální úroveň notifikací pro uživatele.
     *
     * Hodnoty odpovídají enumu GlobalNotificationLevel,
     * například ALL, IMPORTANT_ONLY nebo NONE.
     */
    private String globalNotificationLevel;

    /**
     * Úroveň notifikací, které má uživatel dostávat v roli manažera.
     *
     * Hodnoty odpovídají enumu GlobalNotificationLevel.
     * Určuje, jaké kopie notifikací budou zasílány na e-mail
     * uživatele jako manažera. Používá se pro filtrování kopií
     * zpráv určených manažerům.
     *
     * Pokud je tato hodnota na backendu null, používá se hodnota
     * globalNotificationLevel jako výchozí.
     */
    private String managerNotificationLevel;

    /**
     * Určuje, zda má uživatel dostávat kopie všech notifikací
     * svých hráčů na svůj email.
     */
    private boolean copyAllPlayerNotificationsToUserEmail;

    /**
     * Určuje, zda má uživatel dostávat notifikace i za hráče,
     * kteří mají vlastní email.
     */
    private boolean receiveNotificationsForPlayersWithOwnEmail;

    /**
     * Určuje, zda je aktivní denní souhrn e-mailů místo
     * jednotlivých notifikací.
     */
    private boolean emailDigestEnabled;

    /**
     * Čas pro odeslání souhrnného e-mailu, pokud je denní digest aktivní.
     *
     * Hodnota se zadává jako řetězec, například "20:00".
     */
    private String emailDigestTime;

    // UX / UI

    /**
     * Preferovaný jazyk uživatelského rozhraní,
     * například "cs" nebo "en".
     */
    private String uiLanguage;

    /**
     * Časová zóna uživatele, například "Europe/Prague".
     */
    private String timezone;

    /**
     * Výchozí obrazovka po přihlášení.
     *
     * Typicky odpovídá názvu sekce na frontendu,
     * například "DASHBOARD", "MATCHES" nebo "PLAYERS".
     */
    private String defaultLandingPage;

    // gettery / settery

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

    public String getManagerNotificationLevel() {
        return managerNotificationLevel;
    }

    public void setManagerNotificationLevel(String managerNotificationLevel) {
        this.managerNotificationLevel = managerNotificationLevel;
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
