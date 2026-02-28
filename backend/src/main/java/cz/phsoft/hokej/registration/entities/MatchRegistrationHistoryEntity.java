package cz.phsoft.hokej.registration.entities;

import cz.phsoft.hokej.registration.enums.ExcuseReason;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.Team;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entita reprezentující historický záznam změn registrace hráče k zápasu.
 *
 * Slouží k auditování změn registrací, včetně vytvoření, úprav a zrušení.
 * Uchovává stav registrace v okamžiku změny a základní údaje o původu
 * provedené operace.
 */
@Entity
@Table(name = "match_registration_history")
public class MatchRegistrationHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Typ provedené operace nad registrací.
     * Typicky hodnoty INSERT, UPDATE nebo DELETE.
     */
    @Column(name = "action", nullable = false)
    private String action;

    /**
     * Administrativní poznámka vztahující se k registraci.
     */
    @Column(name = "admin_note")
    private String adminNote;

    /**
     * Datum a čas provedení změny.
     */
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    /**
     * Původ vytvoření nebo změny registrace.
     * Typicky hodnoty jako "user" nebo "system".
     */
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    /**
     * Textová poznámka k omluvě hráče.
     */
    @Column(name = "excuse_note")
    private String excuseNote;

    /**
     * Důvod omluvy hráče, pokud byl stav omluvený.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "excuse_reason")
    private ExcuseReason excuseReason;

    /**
     * ID zápasu, ke kterému se historický záznam vztahuje.
     */
    @Column(name = "match_id", nullable = false)
    private Long matchId;

    /**
     * ID původní registrace z hlavní tabulky registrací.
     */
    @Column(name = "match_registration_id", nullable = false)
    private Long matchRegistrationId;

    /**
     * Původní časové razítko registrace.
     */
    @Column(name = "original_timestamp", nullable = false)
    private LocalDateTime originalTimestamp;

    /**
     * ID hráče, kterého se historický záznam týká.
     */
    @Column(name = "player_id", nullable = false)
    private Long playerId;

    /**
     * Stav registrace v okamžiku změny.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PlayerMatchStatus status;

    /**
     * Tým, do kterého byl hráč v daném okamžiku zařazen.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "team")
    private Team team;

    /**
     * Pozice hráče v tomto konkrétním zápase v okamžiku změny.
     *
     * Slouží k auditování přiřazení hráče na konkrétní pozice.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "position_in_match", length = 30)
    private PlayerPosition positionInMatch;

    /**
     * Příznak, že připomínka MATCH_REMINDER již byla
     * pro danou registraci odeslána v okamžiku této změny.
     *
     * Slouží k auditování chování plánovače připomínek.
     */
    @Column(name = "reminder_already_sent", nullable = false)
    private boolean reminderAlreadySent;

    public MatchRegistrationHistoryEntity() {
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }

    public void setAction(String action) { this.action = action; }

    public String getAdminNote() { return adminNote; }

    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public LocalDateTime getChangedAt() { return changedAt; }

    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }

    public String getCreatedBy() { return createdBy; }

    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getExcuseNote() { return excuseNote; }

    public void setExcuseNote(String excuseNote) { this.excuseNote = excuseNote; }

    public ExcuseReason getExcuseReason() { return excuseReason; }

    public void setExcuseReason(ExcuseReason excuseReason) { this.excuseReason = excuseReason; }

    public Long getMatchId() { return matchId; }

    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public Long getMatchRegistrationId() { return matchRegistrationId; }

    public void setMatchRegistrationId(Long matchRegistrationId) { this.matchRegistrationId = matchRegistrationId; }

    public LocalDateTime getOriginalTimestamp() { return originalTimestamp; }

    public void setOriginalTimestamp(LocalDateTime originalTimestamp) { this.originalTimestamp = originalTimestamp; }

    public Long getPlayerId() { return playerId; }

    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public PlayerMatchStatus getStatus() { return status; }

    public void setStatus(PlayerMatchStatus status) { this.status = status; }

    public Team getTeam() { return team; }

    public void setTeam(Team team) { this.team = team; }

    public PlayerPosition getPositionInMatch() {
        return positionInMatch;
    }

    public void setPositionInMatch(PlayerPosition positionInMatch) {
        this.positionInMatch = positionInMatch;
    }

    public boolean isReminderAlreadySent() {
        return reminderAlreadySent;
    }

    public void setReminderAlreadySent(boolean reminderAlreadySent) {
        this.reminderAlreadySent = reminderAlreadySent;
    }
}