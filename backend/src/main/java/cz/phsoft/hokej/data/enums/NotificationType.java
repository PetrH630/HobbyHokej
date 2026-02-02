package cz.phsoft.hokej.data.enums;

/**
 * Konkrétní typy notifikací používaných v systému.
 *
 * Každý typ notifikace je zařazen do kategorie
 * NotificationCategory a má příznak důležitosti.
 * Tyto informace se používají při rozhodování,
 * komu a jak budou notifikace doručovány.
 */
public enum NotificationType {

    // REGISTRATION

    /**
     * Vytvoření nové registrace hráče na zápas.
     */
    MATCH_REGISTRATION_CREATED(NotificationCategory.REGISTRATION, true),

    /**
     * Aktualizace existující registrace.
     */
    MATCH_REGISTRATION_UPDATED(NotificationCategory.REGISTRATION, true),

    /**
     * Zrušení registrace hráče na zápas.
     */
    MATCH_REGISTRATION_CANCELED(NotificationCategory.REGISTRATION, true),

    /**
     * Přesunutí hráče do stavu rezervace (náhradník).
     */
    MATCH_REGISTRATION_RESERVED(NotificationCategory.REGISTRATION, true),

    /**
     * Registrace hráče jako náhradníka s neurčitou účastí.
     */
    MATCH_REGISTRATION_SUBSTITUTE(NotificationCategory.REGISTRATION, false),

    /**
     * Informace o posunu hráče z čekací listiny výše.
     */
    MATCH_WAITING_LIST_MOVED_UP(NotificationCategory.REGISTRATION, true),

    /**
     * Informace o tom, že hráč dosud nereagoval.
     */
    MATCH_REGISTRATION_NO_RESPONSE(NotificationCategory.REGISTRATION, false),

    /**
     * Hráč byl omluven z účasti na zápase.
     */
    PLAYER_EXCUSED(NotificationCategory.REGISTRATION, true),

    /**
     * Hráč se neomluvil, ačkoliv byl přihlášen.
     */
    PLAYER_NO_EXCUSED(NotificationCategory.REGISTRATION, true),

    // MATCH_INFO

    /**
     * Připomínka nadcházejícího zápasu.
     */
    MATCH_REMINDER(NotificationCategory.MATCH_INFO, true),

    /**
     * Zrušení zápasu.
     */
    MATCH_CANCELED(NotificationCategory.MATCH_INFO, true),

    /**
     * Znovu aktivovaný zápas po předchozím zrušení.
     */
    MATCH_UNCANCELED(NotificationCategory.MATCH_INFO, true),

    /**
     * Změna času nebo dalších detailů zápasu.
     */
    MATCH_TIME_CHANGED(NotificationCategory.MATCH_INFO, true),

    // SYSTEM – hráčské/uživatelské události

    /**
     * Vytvoření nového hráče.
     */
    PLAYER_CREATED(NotificationCategory.SYSTEM, true),

    /**
     * Aktualizace údajů o hráči.
     */
    PLAYER_UPDATED(NotificationCategory.SYSTEM, false),

    /**
     * Schválení hráče administrátorem.
     */
    PLAYER_APPROVED(NotificationCategory.SYSTEM, true),

    /**
     * Zamítnutí hráče administrátorem.
     */
    PLAYER_REJECTED(NotificationCategory.SYSTEM, true),

    /**
     * Smazání hráče.
     */
    PLAYER_DELETED(NotificationCategory.SYSTEM, true),

    /**
     * Změna přiřazení hráče k jinému uživatelskému účtu.
     */
    PLAYER_CHANGE_USER(NotificationCategory.SYSTEM, true),

    // USER

    /**
     * Vytvoření nového uživatelského účtu.
     */
    USER_CREATED(NotificationCategory.SYSTEM, true),

    /**
     * Aktivace uživatelského účtu.
     */
    USER_ACTIVATED(NotificationCategory.SYSTEM, true),

    /**
     * Deaktivace uživatelského účtu.
     */
    USER_DEACTIVATED(NotificationCategory.SYSTEM, true),

    /**
     * Aktualizace údajů o uživateli.
     */
    USER_UPDATED(NotificationCategory.SYSTEM, true),

    // SYSTEM – SECURITY

    /**
     * Reset hesla uživatele.
     */
    PASSWORD_RESET(NotificationCategory.SYSTEM, true),

    /**
     * Změna hesla uživatelem.
     */
    USER_CHANGE_PASSWORD(NotificationCategory.SYSTEM, true),

    /**
     * Požadavek na reset zapomenutého hesla.
     */
    FORGOTTEN_PASSWORD_RESET_REQUEST(NotificationCategory.SYSTEM, true),

    /**
     * Dokončení procesu resetu zapomenutého hesla.
     */
    FORGOTTEN_PASSWORD_RESET_COMPLETED(NotificationCategory.SYSTEM, true),

    /**
     * Bezpečnostní upozornění (podezřelá aktivita a podobně).
     */
    SECURITY_ALERT(NotificationCategory.SYSTEM, true);

    /**
     * Kategorie notifikace (registrace, informace o zápase, systém).
     */
    private final NotificationCategory category;

    /**
     * Příznak důležitosti notifikace.
     *
     * Hodnota se používá například při filtrování podle
     * GlobalNotificationLevel.
     */
    private final boolean important;

    NotificationType(NotificationCategory category, boolean important) {
        this.category = category;
        this.important = important;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public boolean isImportant() {
        return important;
    }
}
