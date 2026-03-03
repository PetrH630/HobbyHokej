package cz.phsoft.hokej.notifications.enums;

/**
 * Kategorie notifikací v systému.
 *
 * Používá se pro filtrování, nastavení preferencí
 * a rozhodování o způsobu doručení.
 */
public enum NotificationCategory {

    /**
     * Notifikace související s registracemi hráčů.
     */
    REGISTRATION,

    /**
     * Notifikace týkající se informací o zápase.
     */
    MATCH_INFO,

    /**
     * Systémové nebo bezpečnostní notifikace.
     */
    SYSTEM
}