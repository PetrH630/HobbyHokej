package cz.phsoft.hokej.user.entities;

import cz.phsoft.hokej.user.enums.PlayerSelectionMode;
import cz.phsoft.hokej.notifications.enums.GlobalNotificationLevel;
import cz.phsoft.hokej.user.enums.LandingPage;

import jakarta.persistence.*;

import java.time.LocalTime;

/**
 * Entita uchovávající nastavení uživatelského účtu.
 *
 * Odděluje identitu uživatele (AppUserEntity) od jeho preferencí
 * a chování v systému (AppUserSettingsEntity). Slouží zejména
 * pro nastavení výběru hráče, globální úrovně notifikací a
 * preferencí souvisejících s uživatelským rozhraním.
 */
@Entity
@Table(name = "app_user_settings")
public class AppUserSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Uživatel, ke kterému tato nastavení patří.
     * Pro jednoho uživatele existuje právě jeden záznam nastavení.
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
     * Určuje, kolik notifikací bude uživatel dostávat
     * bez ohledu na nastavení konkrétních hráčů.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "global_notification_level", nullable = false, length = 50)
    private GlobalNotificationLevel globalNotificationLevel = GlobalNotificationLevel.ALL;

    /**
     * Úroveň notifikací, které má uživatel dostávat v roli manažera.
     *
     * Hodnota se používá při rozhodování, zda mají být zasílány
     * kopie notifikací na e-mail uživatele, pokud má zároveň
     * roli manažera. Nastavení ovlivňuje pouze manažerské kopie,
     * nikoliv notifikace určené přímo tomuto uživateli nebo hráčům.
     *
     * Pokud je hodnota null, používá se hodnota globalNotificationLevel
     * jako výchozí chování.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "manager_notification_level", length = 50)
    private GlobalNotificationLevel managerNotificationLevel;

    /**
     * Určuje, zda má uživatel dostávat kopie všech notifikací,
     * které chodí jeho hráčům.
     *
     * Příklad:
     * true: rodič chce mít přehled o všem, co se děje u dětí.
     * false: spoléhá se pouze na notifikace hráčů.
     */
    @Column(name = "copy_all_player_notifications_to_user_email", nullable = false)
    private boolean copyAllPlayerNotificationsToUserEmail = true;

    /**
     * Určuje, zda má uživatel dostávat notifikace i za hráče,
     * kteří mají vlastní email (contactEmail v PlayerSettings).
     *
     * Příklad:
     * false: pokud má hráč vlastní email, chodí notifikace pouze jemu.
     * true: uživatel dostává kopie notifikací i v tomto případě.
     */
    @Column(name = "receive_notifications_for_players_with_own_email", nullable = false)
    private boolean receiveNotificationsForPlayersWithOwnEmail = false;

    /**
     * Určuje, zda má být používán denní souhrn (digest) místo
     * jednotlivých notifikací během dne.
     */
    @Column(name = "email_digest_enabled", nullable = false)
    private boolean emailDigestEnabled = false;

    /**
     * Čas, kdy má chodit souhrnný email, pokud je digest zapnutý.
     */
    @Column(name = "email_digest_time")
    private LocalTime emailDigestTime;

    /**
     * Preferovaný jazyk uživatelského rozhraní.
     * Například "cs" nebo "en".
     */
    @Column(name = "ui_language", length = 10)
    private String uiLanguage = "cs";

    /**
     * Časová zóna uživatele.
     * Například "Europe/Prague".
     */
    @Column(name = "timezone", length = 50)
    private String timezone = "Europe/Prague";

    /**
     * Výchozí obrazovka po přihlášení.
     *
     * V databázi je uložena jako řetězcová hodnota ENUM (VARCHAR),
     * v aplikační logice se používá enum LandingPage.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "default_landing_page", nullable = false, length = 50)
    private LandingPage defaultLandingPage = LandingPage.DASHBOARD;

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

    public GlobalNotificationLevel getManagerNotificationLevel() {
        return managerNotificationLevel;
    }

    public void setManagerNotificationLevel(GlobalNotificationLevel managerNotificationLevel) {
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

    /**
     * Vrací výchozí landing page jako String (pro DTO / MapStruct).
     */
    public LandingPage getDefaultLandingPage() {
        return defaultLandingPage;
    }

    /**
     * Nastavuje výchozí landing page ze Stringu (pro DTO / MapStruct).
     */
    public void setDefaultLandingPage(LandingPage defaultLandingPage) {
        this.defaultLandingPage = defaultLandingPage;
    }
}