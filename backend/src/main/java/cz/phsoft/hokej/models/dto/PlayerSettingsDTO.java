package cz.phsoft.hokej.models.dto;

/**
 * DTO pro nastavení hráče na úrovni PlayerSettingsEntity.
 *
 * Slouží k přenosu preferencí hráče mezi backendem a frontendem,
 * zejména v oblasti kontaktních údajů a notifikačních kanálů.
 * DTO se používá jak pro zobrazení nastavení, tak pro jejich aktualizaci.
 */
public class PlayerSettingsDTO {

    // kontaktní údaje

    private String contactEmail;
    private String contactPhone;

    // kanály

    private boolean emailEnabled;
    private boolean smsEnabled;

    // typy notifikací

    private boolean notifyOnRegistration;
    private boolean notifyOnExcuse;
    private boolean notifyOnMatchChange;
    private boolean notifyOnMatchCancel;
    private boolean notifyOnPayment;

    // připomínky

    private boolean notifyReminders;
    private Integer reminderHoursBefore;

    // gettery / settery

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
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
}
