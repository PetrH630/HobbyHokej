package cz.phsoft.hokej.data.entities;

import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.enums.JerseyColor;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_registration_history")
public class MatchRegistrationHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID z původní hlavní tabulky
    @Column(name = "match_registration_id", nullable = false)
    private Long matchRegistrationId;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerMatchStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "excuse_reason")
    private ExcuseReason excuseReason;

    @Column(name = "excuse_note")
    private String excuseNote;

    @Column(name = "admin_note")
    private String adminNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "jersey_color")
    private JerseyColor jerseyColor;

    @Column(name = "original_timestamp", nullable = false)
    private LocalDateTime originalTimestamp;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private String action; // INSERT / UPDATE / DELETE

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    public MatchRegistrationHistoryEntity() {
    }

    // Gettery a settery

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMatchRegistrationId() {
        return matchRegistrationId;
    }

    public void setMatchRegistrationId(Long matchRegistrationId) {
        this.matchRegistrationId = matchRegistrationId;
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public PlayerMatchStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerMatchStatus status) {
        this.status = status;
    }

    public ExcuseReason getExcuseReason() {
        return excuseReason;
    }

    public void setExcuseReason(ExcuseReason excuseReason) {
        this.excuseReason = excuseReason;
    }

    public String getExcuseNote() {
        return excuseNote;
    }

    public void setExcuseNote(String excuseNote) {
        this.excuseNote = excuseNote;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public JerseyColor getJerseyColor() {
        return jerseyColor;
    }

    public void setJerseyColor(JerseyColor jerseyColor) {
        this.jerseyColor = jerseyColor;
    }

    public LocalDateTime getOriginalTimestamp() {
        return originalTimestamp;
    }

    public void setOriginalTimestamp(LocalDateTime originalTimestamp) {
        this.originalTimestamp = originalTimestamp;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
