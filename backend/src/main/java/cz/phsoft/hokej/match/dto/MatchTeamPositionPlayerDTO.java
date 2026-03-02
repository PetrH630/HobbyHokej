package cz.phsoft.hokej.match.dto;

/**
 * DTO pro základní identifikaci hráče na konkrétní pozici v zápase.
 *
 * Používá se v rámci přehledu pozic pro tým.
 */
public class MatchTeamPositionPlayerDTO {

    private Long playerId;
    private String playerName;

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}