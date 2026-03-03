package cz.phsoft.hokej.player.dto;

import jakarta.validation.constraints.Pattern;

/**
 * DTO pro nastavení hráče na úrovni PlayerSettingsEntity.
 *
 * Slouží k přenosu preferencí hráče mezi backendem a frontendem,
 * zejména v oblasti kontaktních údajů, notifikačních kanálů,
 * typů notifikací a herních preferencí. DTO se používá jak
 * pro zobrazení nastavení, tak pro jejich aktualizaci.
 *
 * Neobsahuje žádnou business logiku a slouží výhradně
 * jako datový přenosový objekt mezi prezentační a servisní vrstvou.
 */
public class PlayerSettingsDTO {

    /**
     * Kontaktní e-mail hráče.
     */
    private String contactEmail;

    /**
     * Kontaktní telefon hráče v mezinárodním formátu.
     */
    @Pattern(
            regexp = "^\\+[1-9]\\d{1,14}$",
            message = "Telefon musí být v mezinárodním formátu, např. +420123456789"
    )
    private String contactPhone;

    /**
     * Určuje, zda jsou povoleny e-mailové notifikace.
     */
    private boolean emailEnabled;

    /**
     * Určuje, zda jsou povoleny SMS notifikace.
     */
    private boolean smsEnabled;

    /**
     * Určuje, zda má být hráč notifikován při registraci na zápas.
     */
    private boolean notifyOnRegistration;

    /**
     * Určuje, zda má být hráč notifikován při omluvě ze zápasu.
     */
    private boolean notifyOnExcuse;

    /**
     * Určuje, zda má být hráč notifikován při změně termínu nebo času zápasu.
     */
    private boolean notifyOnMatchChange;

    /**
     * Určuje, zda má být hráč notifikován při zrušení zápasu.
     */
    private boolean notifyOnMatchCancel;

    /**
     * Určuje, zda má být hráč notifikován při evidenci platby.
     */
    private boolean notifyOnPayment;

    /**
     * Určuje, zda mají být zasílány připomínky před zápasem.
     */
    private boolean notifyReminders;

    /**
     * Počet hodin před začátkem zápasu, kdy má být odeslána připomínka.
     */
    private Integer reminderHoursBefore;

    /**
     * Určuje, zda může být hráč automaticky přesunut
     * do druhého týmu při uvolnění místa.
     */
    private boolean possibleMoveToAnotherTeam;

    /**
     * Určuje, zda může být hráči automaticky změněna
     * herní pozice mezi obranou a útokem.
     */
    private boolean possibleChangePlayerPosition;

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    /**
     * Nastavuje kontaktní telefon hráče.
     *
     * Pokud je hodnota null nebo prázdná, ukládá se jako null.
     *
     * @param contactPhone kontaktní telefon hráče
     */
    public void setContactPhone(String contactPhone) {
        this.contactPhone = (contactPhone == null || contactPhone.isBlank())
                ? null
                : contactPhone;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public boolean isSmsEnabled() {
        return smsEnabled;
    }

    public void setSmsEnabled(boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
    }

    public boolean isNotifyOnRegistration() {
        return notifyOnRegistration;
    }

    public void setNotifyOnRegistration(boolean notifyOnRegistration) {
        this.notifyOnRegistration = notifyOnRegistration;
    }

    public boolean isNotifyOnExcuse() {
        return notifyOnExcuse;
    }

    public void setNotifyOnExcuse(boolean notifyOnExcuse) {
        this.notifyOnExcuse = notifyOnExcuse;
    }

    public boolean isNotifyOnMatchChange() {
        return notifyOnMatchChange;
    }

    public void setNotifyOnMatchChange(boolean notifyOnMatchChange) {
        this.notifyOnMatchChange = notifyOnMatchChange;
    }

    public boolean isNotifyOnMatchCancel() {
        return notifyOnMatchCancel;
    }

    public void setNotifyOnMatchCancel(boolean notifyOnMatchCancel) {
        this.notifyOnMatchCancel = notifyOnMatchCancel;
    }

    public boolean isNotifyOnPayment() {
        return notifyOnPayment;
    }

    public void setNotifyOnPayment(boolean notifyOnPayment) {
        this.notifyOnPayment = notifyOnPayment;
    }

    public boolean isNotifyReminders() {
        return notifyReminders;
    }

    public void setNotifyReminders(boolean notifyReminders) {
        this.notifyReminders = notifyReminders;
    }

    public Integer getReminderHoursBefore() {
        return reminderHoursBefore;
    }

    public void setReminderHoursBefore(Integer reminderHoursBefore) {
        this.reminderHoursBefore = reminderHoursBefore;
    }

    public boolean isPossibleMoveToAnotherTeam() {
        return possibleMoveToAnotherTeam;
    }

    public void setPossibleMoveToAnotherTeam(boolean possibleMoveToAnotherTeam) {
        this.possibleMoveToAnotherTeam = possibleMoveToAnotherTeam;
    }

    public boolean isPossibleChangePlayerPosition() {
        return possibleChangePlayerPosition;
    }

    public void setPossibleChangePlayerPosition(boolean possibleChangePlayerPosition) {
        this.possibleChangePlayerPosition = possibleChangePlayerPosition;
    }
}