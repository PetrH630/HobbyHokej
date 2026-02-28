package cz.phsoft.hokej.notifications.enums;

/**
 * Základní kategorizace notifikací v systému.
 *
 * Kategorie umožňuje jednodušší filtrování a rozhodování
 * nad typy notifikací, například pro nastavení preferencí
 * nebo logování.
 */
public enum NotificationCategory {

    /**
     * Notifikace související s registracemi hráčů.
     *
     * Jedná se například o přihlášení, odhlášení, omluvy
     * nebo čekací listinu.
     */
    REGISTRATION,   // přihlášení / odhlášení / omluvy / čekací listina

    /**
     * Notifikace informující o změnách kolem zápasu.
     *
     * Typicky jde o změny času, zrušení nebo důležité
     * organizační informace.
     */
    MATCH_INFO,     // změna času, zrušení, info k zápasu

    /**
     * Systémové nebo bezpečnostní notifikace.
     *
     * Patří sem například reset hesla, bezpečnostní upozornění
     * a další události spojené s účtem.
     */
    SYSTEM          // věci typu „reset hesla“, bezpečnost atd.
}
