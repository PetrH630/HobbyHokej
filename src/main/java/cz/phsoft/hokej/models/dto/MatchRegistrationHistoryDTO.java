package cz.phsoft.hokej.models.dto;

import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class MatchRegistrationHistoryDTO {
    private Long id; // volitelné, při GET

    @NotNull(message = "ID zápasu je povinné.")
    @Positive(message = "ID zápasu musí být kladné.")
    private Long matchId;

    @NotNull(message = "ID hráče je povinné.")
    @Positive(message = "ID hráče musí být kladné.")
    private Long playerId;

    private PlayerMatchStatus status;
    private ExcuseReason excuseReason; // pouze pokud status = EXCUSED
    private String excuseNote;

    private LocalDateTime createdAt;

    private String createdBy; // "user" nebo "system"

    @NotNull
    private String changedBy; // "user" nebo "system"

    private LocalDateTime changedAt;

    public MatchRegistrationHistoryDTO() {}

    // Gettery a settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}

