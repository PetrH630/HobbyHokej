package cz.phsoft.hokej.data.entities;

import jakarta.persistence.*;

/**
 * Entita uchovávající nastavení konkrétního hráče.
 *
 * Odděluje:
 * - identitu hráče (PlayerEntity: jméno, tým, status...)
 * - jeho notifikační preference a kontaktní údaje (PlayerSettingsEntity).
 */
@Entity
@Table(name = "player_settings")
public class PlayerSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Jeden záznam PlayerSettingsEntity pro jednoho hráče.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private PlayerEntity player;

    // =========================================
    // KONTAKT
    // =========================================

    /**
     * Volitelný email hráče.
     *
     * Pokud je null/prázdný, používá se email uživatele (AppUser.email).
     * Toto pole tedy představuje "override", nikoliv povinný údaj.
     */
    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    /**
     * Volitelné telefonní číslo hráče pro SMS.
     *
     * Pokud je null/prázdné, můžeš se rozhodnout:
     * - buď SMS neposílat,
     * - nebo použít telefon z jiného zdroje (pokud ho máš).
     */
    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    // =========================================
    // KANÁLY (EMAIL / SMS)
    // =========================================

    /**
     * Má hráč dostávat emailové notifikace?
     *
     * Pozn.: i když je TRUE, je potřeba mít k dispozici
     * efektivní email (contactEmail nebo email účtu).
     */
    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = true;

    /**
     * Má hráč dostávat SMS notifikace?
     *
     * Opět je nutné mít vyplněný kontakt (contactPhone),
     * jinak se SMS nebude posílat.
     */
    @Column(name = "sms_enabled", nullable = false)
    private boolean smsEnabled = false;

    // =========================================
    // TYPY NOTIFIKACÍ
    // =========================================

    /**
     * Chce být hráč informován o své registraci / odhlášení na zápas?
     */
    @Column(name = "notify_on_registration", nullable = false)
    private boolean notifyOnRegistration = true;

    /**
     * Chce být hráč informován o změně svého statusu na omluveného (EXCUSED)?
     */
    @Column(name = "notify_on_excuse", nullable = false)
    private boolean notifyOnExcuse = true;

    /**
     * Chce být hráč informován o změnách zápasu
     * (čas, místo, popis...)?
     */
    @Column(name = "notify_on_match_change", nullable = false)
    private boolean notifyOnMatchChange = true;

    /**
     * Chce být hráč informován o zrušení zápasu?
     */
    @Column(name = "notify_on_match_cancel", nullable = false)
    private boolean notifyOnMatchCancel = true;

    /**
     * Chce hráč notifikace o platbách / dluzích / vyúčtování?
     * (zatím do budoucna, můžeš začít i s FALSE a používat později).
     */
    @Column(name = "notify_on_payment", nullable = false)
    private boolean notifyOnPayment = false;

    // =========================================
    // PŘIPOMÍNKY PŘED ZÁPASEM
    // =========================================

    /**
     * Má hráč dostávat připomínky před zápasem?
     */
    @Column(name = "notify_reminders", nullable = false)
    private boolean notifyReminders = true;

    /**
     * Kolik hodin před začátkem zápasu poslat připomínku.
     * Např. 24 = den předem.
     *
     * Pokud je null, můžeš v logice použít default (např. 24 h).
     */
    @Column(name = "reminder_hours_before")
    private Integer reminderHoursBefore = 24;

    // =========================================
    // GETTERY / SETTERY
    // =========================================

    public Long getId() {
        return id;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(PlayerEntity player) {
        this.player = player;
    }

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
