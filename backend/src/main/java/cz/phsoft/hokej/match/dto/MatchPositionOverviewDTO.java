package cz.phsoft.hokej.match.dto;

import cz.phsoft.hokej.match.enums.MatchMode;
import cz.phsoft.hokej.match.services.MatchPositionService;

import java.util.List;

/**
 * DTO pro přehled pozic a kapacity na ledě v rámci jednoho zápasu.
 *
 * Obsahuje základní identifikaci zápasu, použitý MatchMode, maximální
 * počet hráčů a seznam pozic s kapacitou a obsazeností pro týmy DARK
 * a LIGHT.
 *
 * DTO se používá jako výstup z {@link MatchPositionService}
 * a může se přímo mapovat na odpověď pro frontend.
 */
public class MatchPositionOverviewDTO {

    private Long matchId;
    private MatchMode matchMode;
    private Integer maxPlayers;
    private List<MatchPositionSlotDTO> positionSlots;

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

    public List<MatchPositionSlotDTO> getPositionSlots() {
        return positionSlots;
    }

    public void setPositionSlots(List<MatchPositionSlotDTO> positionSlots) {
        this.positionSlots = positionSlots;
    }
}