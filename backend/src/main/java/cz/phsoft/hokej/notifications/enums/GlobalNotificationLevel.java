package cz.phsoft.hokej.notifications.enums;

/**
 * Globální úroveň notifikací pro uživatele.
 *
 * Určuje, v jakém rozsahu budou uživateli doručovány notifikace
 * napříč všemi jeho hráči, bez ohledu na dílčí nastavení
 * jednotlivých hráčů.
 */
public enum GlobalNotificationLevel {

    /**
     * Všechny běžné notifikace.
     *
     * Zahrnují například registrace, omluvy, změny zápasů,
     * zrušení, připomínky a další standardní události.
     */
    ALL,

    /**
     * Pouze důležité události.
     *
     * Typicky se jedná o zrušení zápasu, změnu času nebo místa
     * zápasu a další kritické informace.
     */
    IMPORTANT_ONLY,

    /**
     * Uživatel nechce žádné notifikace pro sebe.
     *
     * Nastavení hráčů zůstává zachováno a může být použito
     * pro doručování notifikací přímo hráčům.
     */
    NONE
}
