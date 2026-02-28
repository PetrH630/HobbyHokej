package cz.phsoft.hokej.registration.dto;

import cz.phsoft.hokej.match.enums.MatchMode;
import cz.phsoft.hokej.player.enums.Team;

import java.util.List;

/**
 * DTO pro přehled pozic a kapacity na ledě v rámci jednoho zápasu
 * pro konkrétní tým.
 *
 * Obsahuje identifikaci zápasu, použitý MatchMode, maximální počet hráčů,
 * vybraný tým a seznam pozic s kapacitou, obsazeností a volnými sloty
 * pro tento tým.
 */
public class MatchTeamPositionOverviewDTO {

    private Long matchId;
    private MatchMode matchMode;
    private Integer maxPlayers;
    private Team team;
    private List<MatchTeamPositionSlotDTO> positionSlots;

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public MatchMode getMatchMode() {
        return matchMode;
    }

    public void setMatchMode(MatchMode matchMode) {
        this.matchMode = matchMode;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public List<MatchTeamPositionSlotDTO> getPositionSlots() {
        return positionSlots;
    }

    public void setPositionSlots(List<MatchTeamPositionSlotDTO> positionSlots) {
        this.positionSlots = positionSlots;
    }
}