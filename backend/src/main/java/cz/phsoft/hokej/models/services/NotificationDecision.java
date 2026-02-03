package cz.phsoft.hokej.models.services;

/**
 * Datový objekt reprezentující výsledek vyhodnocení
 * notifikačních preferencí pro konkrétní událost.
 *
 * Slouží jako přenosový objekt mezi logikou vyhodnocení
 * notifikací a konkrétními kanály odesílání (email, SMS).
 *
 * Objekt jednoznačně určuje:
 * - zda má být odeslán email uživateli (AppUser),
 * - zda má být odeslán email hráči na jeho vlastní kontakt,
 * - zda má být odeslána SMS hráči,
 * - jaké konkrétní kontaktní údaje se mají použít.
 *
 * Třída neřeší:
 * - samotné odesílání notifikací,
 * - sestavení obsahu zpráv,
 * - validaci kontaktních údajů.
 *
 * Používá se typicky v NotificationService
 * jako výsledek rozhodovací logiky.
 */
public class NotificationDecision {

    // ==================================================
    // EMAIL – UŽIVATEL (AppUser)
    // ==================================================

    /**
     * Určuje, zda má být odeslán email uživateli (AppUser).
     *
     * Hodnota {@code true} znamená, že notifikace má být
     * odeslána na email navázaný na uživatelský účet.
     */
    private boolean sendEmailToUser;

    /**
     * Emailová adresa uživatele (AppUser.email),
     * na kterou má být případná notifikace odeslána.
     */
    private String userEmail;

    /**
     * Celé jméno uživatele.
     *
     * Používá se zejména pro personalizaci textu
     * emailové zprávy.
     */
    private String fullname;

    // ==================================================
    // EMAIL – HRÁČ
    // ==================================================

    /**
     * Určuje, zda má být odeslán email přímo hráči.
     *
     * Email se odesílá na kontakt definovaný v nastavení hráče,
     * typicky {@code PlayerSettings.contactEmail}.
     */
    private boolean sendEmailToPlayer;

    /**
     * Emailová adresa hráče.
     *
     * Hodnota obvykle pochází z {@code PlayerSettings.contactEmail},
     * případně může být použita náhradní (fallback) hodnota
     * podle logiky vyhodnocení.
     */
    private String playerEmail;

    // ==================================================
    // SMS – HRÁČ
    // ==================================================

    /**
     * Určuje, zda má být odeslána SMS hráči.
     *
     * Hodnota {@code true} znamená, že hráč má povolené
     * SMS notifikace a je k dispozici platné telefonní číslo.
     */
    private boolean sendSmsToPlayer;

    /**
     * Telefonní číslo hráče.
     *
     * Hodnota obvykle pochází z {@code PlayerSettings.contactPhone},
     * případně se použije fallback hodnota uložená přímo u hráče.
     */
    private String playerPhone;

    // ==================================================
    // GETTERY / SETTERY
    // ==================================================

    public boolean isSendEmailToUser() {
        return sendEmailToUser;
    }

    public void setSendEmailToUser(boolean sendEmailToUser) {
        this.sendEmailToUser = sendEmailToUser;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public boolean isSendEmailToPlayer() {
        return sendEmailToPlayer;
    }

    public void setSendEmailToPlayer(boolean sendEmailToPlayer) {
        this.sendEmailToPlayer = sendEmailToPlayer;
    }

    public String getPlayerEmail() {
        return playerEmail;
    }

    public void setPlayerEmail(String playerEmail) {
        this.playerEmail = playerEmail;
    }

    public boolean isSendSmsToPlayer() {
        return sendSmsToPlayer;
    }

    public void setSendSmsToPlayer(boolean sendSmsToPlayer) {
        this.sendSmsToPlayer = sendSmsToPlayer;
    }

    public String getPlayerPhone() {
        return playerPhone;
    }

    public void setPlayerPhone(String playerPhone) {
        this.playerPhone = playerPhone;
    }
}
