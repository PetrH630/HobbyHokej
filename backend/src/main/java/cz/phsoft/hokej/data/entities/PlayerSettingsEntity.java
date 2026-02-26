package cz.phsoft.hokej.data.entities;

import jakarta.persistence.*;

/**
 * Entita uchovávající nastavení konkrétního hráče.
 *
 * Odděluje identitu hráče (PlayerEntity) od jeho kontaktních údajů
 * a detailních notifikačních preferencí. Nastavení se využívá
 * při rozhodování o tom, zda a jak budou hráči doručovány notifikace.
 */
@Entity
@Table(name = "player_settings")
public class PlayerSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Hráč, ke kterému toto nastavení patří.
     * Pro jednoho hráče existuje právě jeden záznam nastavení.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private PlayerEntity player;

    /**
     * Volitelný email hráče.
     * Pokud není vyplněn, může se použít email uživatele (AppUserEntity).
     */
    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    /**
     * Volitelné telefonní číslo hráče pro SMS notifikace.
     */
    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    /**
     * Příznak, zda má hráč dostávat emailové notifikace.
     */
    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = true;

    /**
     * Příznak, zda má hráč dostávat SMS notifikace.
     */
    @Column(name = "sms_enabled", nullable = false)
    private boolean smsEnabled = false;

    /**
     * Příznak, zda chce hráč notifikace o registraci a odhlášení.
     */
    @Column(name = "notify_on_registration", nullable = false)
    private boolean notifyOnRegistration = true;

    /**
     * Příznak, zda chce hráč notifikace o omluvách.
     */
    @Column(name = "notify_on_excuse", nullable = false)
    private boolean notifyOnExcuse = true;

    /**
     * Příznak, zda chce hráč notifikace o změnách zápasu.
     */
    @Column(name = "notify_on_match_change", nullable = false)
    private boolean notifyOnMatchChange = true;

    /**
     * Příznak, zda chce hráč notifikace o zrušení zápasu.
     */
    @Column(name = "notify_on_match_cancel", nullable = false)
    private boolean notifyOnMatchCancel = true;

    /**
     * Příznak, zda chce hráč notifikace o platbách nebo vyúčtování.
     */
    @Column(name = "notify_on_payment", nullable = false)
    private boolean notifyOnPayment = false;

    /**
     * Příznak, zda má hráč dostávat připomínky před zápasem.
     */
    @Column(name = "notify_reminders", nullable = false)
    private boolean notifyReminders = true;

    /**
     * Počet hodin před začátkem zápasu, kdy má být odeslána připomínka.
     * Například hodnota 24 znamená připomínku den předem.
     */
    @Column(name = "reminder_hours_before")
    private Integer reminderHoursBefore = 24;

    /**
     * Příznak, zda může být hráč automaticky přesunut
     * do druhého týmu při uvolnění místa.
     *
     * Používá se při výběru náhradníka z RESERVED
     * pro obsazení volného slotu v opačném týmu.
     */
    @Column(name = "possible_move_to_another_team", nullable = false)
    private boolean possibleMoveToAnotherTeam = false;

    /**
     * Příznak, zda může být hráči automaticky změněna
     * herní pozice mezi obranou a útokem.
     *
     * Používá se při výběru náhradníka z RESERVED
     * pro obsazení volného slotu na jiné než preferované
     * pozici. Na pozici brankáře se tímto příznakem
     * přesun nepovoluje.
     */
    @Column(name = "possible_change_player_position", nullable = false)
    private boolean possibleChangePlayerPosition = false;

    /**
     * Určuje, zda jsou povoleny notifikace týkající se registrací.
     */
    @Transient
    public boolean isRegistrationNotificationsEnabled() {
        return notifyOnRegistration;
    }

    /**
     * Určuje, zda jsou povoleny notifikace týkající se omluv.
     */
    @Transient
    public boolean isExcuseNotificationsEnabled() {
        return notifyOnExcuse;
    }

    /**
     * Určuje, zda jsou povoleny notifikace týkající se informací o zápase,
     * například změn a připomínek.
     */
    @Transient
    public boolean isMatchInfoNotificationsEnabled() {
        return notifyOnMatchChange
                || notifyOnMatchCancel
                || notifyReminders;
    }

    /**
     * Určuje, zda jsou povoleny systémové notifikace,
     * například o platbách.
     */
    @Transient
    public boolean isSystemNotificationsEnabled() {
        return notifyOnPayment;
    }

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