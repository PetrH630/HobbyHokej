package cz.phsoft.hokej.data.entities;

import jakarta.persistence.Embeddable;

// TODO - ASI SMAZAT PO OTESTOVÁNÍ Z SETTINGS

/**
 * Vnořitelný objekt reprezentující základní nastavení notifikačních kanálů.
 *
 * Slouží k určení, zda jsou povoleny emailové a SMS notifikace.
 * Lze jej vkládat jako embeddable do různých entit podle potřeby.
 */
@Embeddable
public class NotificationSettings {

    /**
     * Příznak povolení emailových notifikací.
     */
    private boolean emailEnabled;

    /**
     * Příznak povolení SMS notifikací.
     */
    private boolean smsEnabled;

    public boolean isEmailEnabled() { return emailEnabled; }

    public void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }

    public boolean isSmsEnabled() { return smsEnabled; }

    public void setSmsEnabled(boolean smsEnabled) { this.smsEnabled = smsEnabled; }
}
