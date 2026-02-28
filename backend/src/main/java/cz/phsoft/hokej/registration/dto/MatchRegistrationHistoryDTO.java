package cz.phsoft.hokej.registration.dto;

import cz.phsoft.hokej.registration.enums.ExcuseReason;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.Team;

import java.time.LocalDateTime;

/**
 * DTO reprezentující historický záznam o registraci hráče k zápasu.
 *
 * Slouží pro auditní a přehledové účely. Obsahuje informace o tom,
 * jak se registrace v čase měnila, včetně původního časového razítka,
 * akce, autora změny a stavů registrace.
 *
 * Datový model odpovídá záznamu v tabulce historie registrací.
 */
public class MatchRegistrationHistoryDTO {

    private Long id;
    private String action;
    private String adminNote;
    private LocalDateTime changedAt;
    private String createdBy;
    private String excuseNote;
    private ExcuseReason excuseReason;
    private Long matchId;
    private Long matchRegistrationId;
    private LocalDateTime originalTimestamp;
    private Long playerId;
    private PlayerMatchStatus status;
    private Team team;

    /**
     * Pozice hráče v tomto konkrétním zápase v okamžiku změny.
     */
    private PlayerPosition positionInMatch;

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
}