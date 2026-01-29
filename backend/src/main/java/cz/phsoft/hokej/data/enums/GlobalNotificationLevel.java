package cz.phsoft.hokej.data.enums;

/**
 * Globální úroveň notifikací pro uživatele.
 *
 * Řeší, kolik toho chce uživatel dostávat
 * napříč všemi svými hráči.
 */
public enum GlobalNotificationLevel {
    /**
     * Všechny běžné notifikace (registrace, omluvy,
     * změny zápasů, zrušení, připomínky...).
     */
    ALL,
    /**
     * Pouze důležité události – typicky:
     * - zrušení zápasu,
     * - změna času/místa zápasu,
     * - kritické informace.
     */
    IMPORTANT_ONLY,
    /**
     * Uživatel nechce žádné notifikace pro sebe.
     * Hráči ale stále mohou mít své notifikace
     * dle vlastního nastavení.
     */
    NONE
}
