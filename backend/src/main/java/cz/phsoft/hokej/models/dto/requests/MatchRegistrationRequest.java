package cz.phsoft.hokej.models.dto.requests;

import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.Team;
import jakarta.validation.constraints.*;


public class MatchRegistrationRequest {
    @NotNull
    @Positive
    private Long matchId;

    @NotNull
    @Positive
    private Long playerId;

    private Team team;
    private ExcuseReason excuseReason;
    private String excuseNote;
    private String adminNote;
    private boolean unregister;

    public Long getMatchId() { return matchId; }
    public Long getPlayerId() { return playerId; }
    public Team getTeam() { return team; }
    public ExcuseReason getExcuseReason() { return excuseReason; }
    public String getExcuseNote() { return excuseNote; }
    public String getAdminNote() { return adminNote; }
    public boolean isUnregister() { return unregister; }
}
