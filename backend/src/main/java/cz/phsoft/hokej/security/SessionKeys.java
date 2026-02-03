package cz.phsoft.hokej.security;

/**
 * Centrální definice klíčů používaných v HTTP session.
 *
 * Slouží jako jednotné místo pro názvy session atributů,
 * aby se zabránilo duplicitám, překlepům a nekonzistenci
 * napříč aplikací.
 *
 * Třída je určena výhradně jako držák konstant
 * a neobsahuje žádnou logiku.
 */
public final class SessionKeys {

    /**
     * Klíč session atributu pro ID aktuálně zvoleného hráče.
     *
     * Hodnota představuje identifikátor hráče,
     * se kterým přihlášený uživatel právě pracuje.
     */
    public static final String CURRENT_PLAYER_ID = "CURRENT_PLAYER_ID";

    private SessionKeys() {
        // Utility třída, instanci nelze vytvořit
    }
}
