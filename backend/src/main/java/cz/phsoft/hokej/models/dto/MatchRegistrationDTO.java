package cz.phsoft.hokej.models.dto;

import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.JerseyColor;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class MatchRegistrationDTO {
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
    private String adminNote;
    private JerseyColor jerseyColor;

    @NotNull
    private String createdBy; // "user" nebo "system"

    private PlayerDTO playerDTO;

    public MatchRegistrationDTO() {}

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

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public JerseyColor getJerseyColor() { return jerseyColor; }
    public void setJerseyColor(JerseyColor jerseyColor) { this.jerseyColor = jerseyColor; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}

