package cz.phsoft.hokej.user.enums;

/**
 * Enum reprezentující výchozí obrazovku po přihlášení uživatele.
 *
 * Hodnota se používá v nastavení uživatele (AppUserSettingsEntity)
 * a určuje, na jakou sekci aplikace má být uživatel po přihlášení
 * přesměrován.
 */
public enum LandingPage {

    /**
     * Přehledová obrazovka (dashboard) s úvodními informacemi.
     */
    DASHBOARD,

    /**
     * Sekce s přehledem hráčů.
     */
    PLAYERS,

    /**
     * Sekce s přehledem zápasů.
     */
    MATCHES
}