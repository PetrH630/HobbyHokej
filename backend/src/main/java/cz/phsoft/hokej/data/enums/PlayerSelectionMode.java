package cz.phsoft.hokej.data.enums;

/**
 * Způsob automatického výběru hráče pro uživatele.
 *
 * Použije se při přihlášení uživatele nebo při
 * volání auto-select logiky.
 */
public enum PlayerSelectionMode {
    /**
     * Automaticky zvolí prvního hráče uživatele
     * seřazeného podle ID (nejstarší hráč).
     */
    FIRST_PLAYER,
    /**
     * Po přihlášení se žádný hráč automaticky nevybere.
     * Uživateli se na FE nabídne výběr hráče ručně.
     */
    ALWAYS_CHOOSE
}
