package cz.phsoft.hokej.security;

/**
 * Centrální definice klíčů používaných v HTTP session.
 *
 * Třída slouží jako jednotné místo pro názvy session atributů,
 * aby se zabránilo duplicitám, překlepům a nekonzistenci
 * napříč aplikací.
 *
 * Neobsahuje žádnou aplikační logiku a je určena výhradně
 * jako držák konstant.
 */
public final class SessionKeys {

    /**
     * Klíč session atributu pro identifikátor aktuálně zvoleného hráče.
     *
     * Hodnota představuje ID hráče, se kterým přihlášený uživatel
     * právě pracuje v rámci aktuální session.
     */
    public static final String CURRENT_PLAYER_ID = "CURRENT_PLAYER_ID";

    /**
     * Soukromý konstruktor brání vytvoření instance třídy.
     *
     * Třída je navržena jako utilitní a obsahuje pouze konstanty.
     */
    private SessionKeys() {
        // Utility třída, instanci nelze vytvořit
    }
}