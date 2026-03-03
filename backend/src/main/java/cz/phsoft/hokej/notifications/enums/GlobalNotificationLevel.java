package cz.phsoft.hokej.notifications.enums;

/**
 * Globální úroveň notifikací pro uživatele.
 *
 * Určuje rozsah notifikací doručovaných uživateli
 * bez ohledu na individuální nastavení jednotlivých hráčů.
 */
public enum GlobalNotificationLevel {

    /**
     * Všechny běžné notifikace.
     */
    ALL,

    /**
     * Pouze důležité události.
     */
    IMPORTANT_ONLY,

    /**
     * Žádné notifikace pro uživatele.
     */
    NONE
}