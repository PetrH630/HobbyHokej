package cz.phsoft.hokej.data.entities;

import jakarta.persistence.Embeddable;
// TODO - ASI SMAZAT PO OTESTOVÁNÍ Z SETTINGS
/**
 * Vnořitelný objekt reprezentující nastavení notifikací uživatele / hráče.
 *
 * Slouží k určení, jakými kanály si uživatel přeje
 * přijímat systémové notifikace.
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

    // gettery / settery

    public boolean isEmailEnabled() { return emailEnabled; }
    public void setEmailEnabled(boolean emailEnabled) { this.emailEnabled = emailEnabled; }

    public boolean isSmsEnabled() { return smsEnabled; }
    public void setSmsEnabled(boolean smsEnabled) { this.smsEnabled = smsEnabled; }
}
