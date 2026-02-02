package cz.phsoft.hokej.data.enums;

/**
 * Způsob automatického výběru hráče pro uživatele.
 *
 * Používá se při přihlášení uživatele nebo při volání auto-select
 * logiky na backendu. Ovlivňuje, jaký hráč bude předvybrán v UI.
 */
public enum PlayerSelectionMode {

    /**
     * Automaticky se vybere první hráč uživatele podle ID.
     *
     * Typicky se jedná o nejstaršího (prvně založeného) hráče
     * daného uživatele.
     */
    FIRST_PLAYER,

    /**
     * Po přihlášení se žádný hráč automaticky nevybere.
     *
     * Uživatel si musí hráče zvolit ručně, obvykle z nabídky
     * na frontendu.
     */
    ALWAYS_CHOOSE
}
