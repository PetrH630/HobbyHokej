package cz.phsoft.hokej.models.services;

/**
 * Výsledek vyhodnocení notifikačních preferencí.
 *
 * Třída říká:
 * - zda poslat email uživateli (AppUser),
 * - zda poslat email hráči (pokud má vlastní email),
 * - zda poslat SMS hráči,
 * - jaké konkrétní kontakty použít.
 */
public class NotificationDecision {

    // ===== EMAIL =====

    /**
     * Poslat email uživateli (AppUser)?
     */
    private boolean sendEmailToUser;

    /**
     * Email uživatele (AppUser.email).
     */
    private String userEmail;

    /**
     * fullname uživatele (AppUser.email).
     */
    private String fullname;
    /**
     * Poslat email hráči na jeho kontakt (PlayerSettings.contactEmail)?
     */
    private boolean sendEmailToPlayer;

    /**
     * Email hráče (PlayerSettings.contactEmail nebo fallback).
     */
    private String playerEmail;

    // ===== SMS =====

    /**
     * Poslat SMS hráči?
     */
    private boolean sendSmsToPlayer;

    /**
     * Telefon hráče (PlayerSettings.contactPhone nebo fallback).
     */
    private String playerPhone;

    // ===== GETTERY / SETTERY =====

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
