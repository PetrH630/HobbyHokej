package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.enums.Team;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entita reprezentující historický záznam změn registrace hráče k zápasu.
 *
 * Slouží k auditování změn registrací (vytvoření, úprava, zrušení)
 * a uchovává kompletní stav registrace v okamžiku změny.
 */
@Entity
@Table(name = "match_registration_history")
public class MatchRegistrationHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID původní registrace z hlavní tabulky.
     */
    @Column(name = "match_registration_id", nullable = false)
    private Long matchRegistrationId;

    /**
     * ID zápasu, ke kterému se historický záznam vztahuje.
     */
    @Column(name = "match_id", nullable = false)
    private Long matchId;

    /**
     * ID hráče, kterého se historický záznam týká.
     */
    @Column(name = "player_id", nullable = false)
    private Long playerId;

    /**
     * Stav registrace v okamžiku změny.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerMatchStatus status;

    /**
     * Důvod omluvy hráče (pokud byl stav EXCUSED).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "excuse_reason")
    private ExcuseReason excuseReason;

    /**
     * Textová poznámka k omluvě hráče.
     */
    @Column(name = "excuse_note")
    private String excuseNote;

    /**
     * Administrativní poznámka vztahující se k registraci.
     */
    @Column(name = "admin_note")
    private String adminNote;

    /**
     * Tým, do kterého byl hráč v daném okamžiku zařazen.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "team")
    private Team team;

    /**
     * Původní časové razítko registrace.
     */
    @Column(name = "original_timestamp", nullable = false)
    private LocalDateTime originalTimestamp;

    /**
     * Původ vytvoření registrace (např. "user", "system").
     */
    @Column(name = "created_by", nullable = false)
    private String createdBy;

    /**
     * Typ provedené operace nad registrací.
     *
     * Typicky:
     * - INSERT
     * - UPDATE
     * - DELETE
     */
    @Column(nullable = false)
    private String action;

    /**
     * Datum a čas provedení změny.
     */
    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public MatchRegistrationHistoryEntity() {
    }

    // gettery / settery

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMatchRegistrationId() { return matchRegistrationId; }
    public void setMatchRegistrationId(Long matchRegistrationId) { this.matchRegistrationId = matchRegistrationId; }

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public PlayerMatchStatus getStatus() { return status; }
    public void setStatus(PlayerMatchStatus status) { this.status = status; }

    public ExcuseReason getExcuseReason() { return excuseReason; }
    public void setExcuseReason(ExcuseReason excuseReason) { this.excuseReason = excuseReason; }

    public String getExcuseNote() { return excuseNote; }
    public void setExcuseNote(String excuseNote) { this.excuseNote = excuseNote; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }

    public LocalDateTime getOriginalTimestamp() { return originalTimestamp; }
    public void setOriginalTimestamp(LocalDateTime originalTimestamp) { this.originalTimestamp = originalTimestamp; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
