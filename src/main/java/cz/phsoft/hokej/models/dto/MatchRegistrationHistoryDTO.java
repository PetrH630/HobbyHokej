package cz.phsoft.hokej.models.dto;

import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.enums.JerseyColor;

import java.time.LocalDateTime;

public class MatchRegistrationHistoryDTO {

    private Long id;
    private Long matchRegistrationId;
    private Long matchId;
    private Long playerId;

    private PlayerMatchStatus status;
    private ExcuseReason excuseReason;
    private String excuseNote;
    private String adminNote;
    private JerseyColor jerseyColor;

    private LocalDateTime originalTimestamp;
    private String createdBy;

    private String action;       // INSERT / UPDATE / DELETE
    private LocalDateTime changedAt;

    public MatchRegistrationHistoryDTO() {
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
