package cz.phsoft.hokej.notifications.enums;

/**
 * Kanály, kterými mohou být doručovány notifikace.
 *
 * Enum se používá při rozhodování, zda se má notifikace poslat
 * e-mailem, SMS nebo více kanály.
 */
public enum NotificationChannel {

    /**
     * Emailový kanál.
     */
    EMAIL,

    /**
     * SMS kanál.
     */
    SMS
}
