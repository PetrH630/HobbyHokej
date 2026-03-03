package cz.phsoft.hokej.notifications.services;

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

    // EMAIL – UŽIVATEL (AppUser)

    /**
     * Určuje, zda má být odeslán email uživateli (AppUser).
     *
     * Hodnota true znamená, že notifikace má být
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

    // EMAIL – HRÁČ

    /**
     * Určuje, zda má být odeslán email přímo hráči.
     *
     * Email se odesílá na kontakt definovaný v nastavení hráče,
     * typicky PlayerSettings.contactEmail.
     */
    private boolean sendEmailToPlayer;

    /**
     * Emailová adresa hráče.
     *
     * Hodnota obvykle pochází z PlayerSettings.contactEmail,
     * případně může být použita náhradní hodnota
     * podle logiky vyhodnocení.
     */
    private String playerEmail;

    // SMS – HRÁČ

    /**
     * Určuje, zda má být odeslána SMS hráči.
     *
     * Hodnota true znamená, že hráč má povolené
     * SMS notifikace a je k dispozici platné telefonní číslo.
     */
    private boolean sendSmsToPlayer;

    /**
     * Telefonní číslo hráče.
     *
     * Hodnota obvykle pochází z PlayerSettings.contactPhone,
     * případně se použije náhradní hodnota uložená přímo u hráče.
     */
    private String playerPhone;

    // GETTERY / SETTERY

    /**
     * Zjistí, zda má být odeslán email uživateli (AppUser).
     *
     * @return true, pokud se má odeslat email uživateli, jinak false
     */
    public boolean isSendEmailToUser() {
        return sendEmailToUser;
    }

    /**
     * Nastaví, zda má být odeslán email uživateli (AppUser).
     *
     * @param sendEmailToUser hodnota určující, zda se má email odeslat
     */
    public void setSendEmailToUser(boolean sendEmailToUser) {
        this.sendEmailToUser = sendEmailToUser;
    }

    /**
     * Vrátí emailovou adresu uživatele.
     *
     * @return emailová adresa uživatele, na kterou se má notifikace odeslat
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Nastaví emailovou adresu uživatele.
     *
     * @param userEmail emailová adresa, která se použije pro odeslání notifikace
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Zjistí, zda má být odeslán email přímo hráči.
     *
     * @return true, pokud se má odeslat email hráči, jinak false
     */
    public boolean isSendEmailToPlayer() {
        return sendEmailToPlayer;
    }

    /**
     * Nastaví, zda má být odeslán email přímo hráči.
     *
     * @param sendEmailToPlayer hodnota určující, zda se má email odeslat hráči
     */
    public void setSendEmailToPlayer(boolean sendEmailToPlayer) {
        this.sendEmailToPlayer = sendEmailToPlayer;
    }

    /**
     * Vrátí emailovou adresu hráče.
     *
     * @return emailová adresa hráče použitá pro odeslání notifikace
     */
    public String getPlayerEmail() {
        return playerEmail;
    }

    /**
     * Nastaví emailovou adresu hráče.
     *
     * @param playerEmail emailová adresa hráče, na kterou se má notifikace odeslat
     */
    public void setPlayerEmail(String playerEmail) {
        this.playerEmail = playerEmail;
    }

    /**
     * Zjistí, zda má být odeslána SMS hráči.
     *
     * @return true, pokud se má odeslat SMS hráči, jinak false
     */
    public boolean isSendSmsToPlayer() {
        return sendSmsToPlayer;
    }

    /**
     * Nastaví, zda má být odeslána SMS hráči.
     *
     * @param sendSmsToPlayer hodnota určující, zda se má SMS odeslat
     */
    public void setSendSmsToPlayer(boolean sendSmsToPlayer) {
        this.sendSmsToPlayer = sendSmsToPlayer;
    }

    /**
     * Vrátí telefonní číslo hráče.
     *
     * @return telefonní číslo hráče, na které má být SMS odeslána
     */
    public String getPlayerPhone() {
        return playerPhone;
    }

    /**
     * Nastaví telefonní číslo hráče.
     *
     * @param playerPhone telefonní číslo, které se použije pro odeslání SMS
     */
    public void setPlayerPhone(String playerPhone) {
        this.playerPhone = playerPhone;
    }
}