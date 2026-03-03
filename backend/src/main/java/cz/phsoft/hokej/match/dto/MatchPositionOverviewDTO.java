package cz.phsoft.hokej.match.dto;

import cz.phsoft.hokej.match.enums.MatchMode;

import java.util.List;

/**
 * DTO reprezentující přehled pozic a jejich kapacity v rámci jednoho zápasu.
 *
 * Objekt obsahuje identifikaci zápasu, použitý MatchMode,
 * maximální počet hráčů a seznam pozic s kapacitou
 * a aktuální obsazeností pro oba týmy.
 *
 * DTO je vytvářeno službou MatchPositionService
 * a slouží jako výstupní model pro frontend.
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