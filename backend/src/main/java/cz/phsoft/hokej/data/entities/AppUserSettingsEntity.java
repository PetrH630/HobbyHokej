package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.PlayerSelectionMode;
import cz.phsoft.hokej.data.enums.GlobalNotificationLevel;
import jakarta.persistence.*;

import java.time.LocalTime;

/**
 * Entita uchovávající nastavení uživatelského účtu (AppUserEntity).
 *
 * Odděluje:
 * - identitu uživatele (AppUserEntity: login, heslo, role...)
 * - jeho preference a chování v systému (AppUserSettingsEntity).
 */
@Entity
@Table(name = "app_user_settings")
public class AppUserSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Jeden záznam AppUserSettingsEntity pro jednoho uživatele.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUserEntity user;

    /**
     * Způsob automatického výběru hráče po přihlášení
     * nebo při auto-select logice.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "player_selection_mode", nullable = false, length = 50)
    private PlayerSelectionMode playerSelectionMode = PlayerSelectionMode.FIRST_PLAYER;

    /**
     * Globální úroveň notifikací pro uživatele.
     * Určuje, kolik notifikací bude dostávat on sám,
     * nezávisle na hráčích.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "global_notification_level", nullable = false, length = 50)
    private GlobalNotificationLevel globalNotificationLevel = GlobalNotificationLevel.ALL;

    /**
     * Má uživatel dostávat kopie všech notifikací,
     * které chodí jeho hráčům?
     *
     * Příklad:
     * - TRUE: rodič chce mít přehled o všem, co se děje u dětí
     * - FALSE: spoléhá se jen na notifikace hráčů.
     */
    @Column(name = "copy_all_player_notifications_to_user_email", nullable = false)
    private boolean copyAllPlayerNotificationsToUserEmail = true;

    /**
     * Má uživatel dostávat notifikace i za hráče,
     * kteří mají vlastní email (contactEmail v PlayerSettings)?
     *
     * Příklad:
     * - FALSE: když má dítě vlastní email, chodí notifikace jen jemu
     * - TRUE: rodič chce kopii, i když má hráč svůj email.
     */
    @Column(name = "receive_notifications_for_players_with_own_email", nullable = false)
    private boolean receiveNotificationsForPlayersWithOwnEmail = false;

    /**
     * Má uživatel preferovat denní souhrn (digest)
     * místo jednotlivých notifikací během dne?
     */
    @Column(name = "email_digest_enabled", nullable = false)
    private boolean emailDigestEnabled = false;

    /**
     * Čas, kdy má chodit souhrnný email, pokud je digest zapnutý.
     * Např. 20:00.
     */
    @Column(name = "email_digest_time")
    private LocalTime emailDigestTime;

    /**
     * Preferovaný jazyk uživatelského rozhraní.
     * Např. "cs", "en".
     */
    @Column(name = "ui_language", length = 10)
    private String uiLanguage = "cs";

    /**
     * Časová zóna uživatele, např. "Europe/Prague".
     */
    @Column(name = "timezone", length = 50)
    private String timezone = "Europe/Prague";

    /**
     * Výchozí obrazovka po přihlášení.
     * Může být např. "DASHBOARD", "MATCHES", "PLAYERS".
     *
     * Z praktických důvodů ji necháme jako String,
     * enum můžeme doplnit později, pokud bude potřeba.
     */
    @Column(name = "default_landing_page", length = 50)
    private String defaultLandingPage = "DASHBOARD";

    // =========================================
    // Gettery a settery
    // =========================================

    public Long getId() {
        return id;
    }

    public AppUserEntity getUser() {
        return user;
    }

    public void setUser(AppUserEntity user) {
        this.user = user;
    }

    public PlayerSelectionMode getPlayerSelectionMode() {
        return playerSelectionMode;
    }

    public void setPlayerSelectionMode(PlayerSelectionMode playerSelectionMode) {
        this.playerSelectionMode = playerSelectionMode;
    }

    public GlobalNotificationLevel getGlobalNotificationLevel() {
        return globalNotificationLevel;
    }

    public void setGlobalNotificationLevel(GlobalNotificationLevel globalNotificationLevel) {
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

    public LocalTime getEmailDigestTime() {
        return emailDigestTime;
    }

    public void setEmailDigestTime(LocalTime emailDigestTime) {
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
