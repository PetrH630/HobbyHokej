package cz.phsoft.hokej.match.dto;

/**
 * DTO reprezentující základní identifikaci hráče
 * obsazujícího konkrétní pozici v zápase.
 *
 * Objekt se používá jako součást přehledu pozic
 * v rámci jednoho týmu a obsahuje pouze
 * identifikační údaje potřebné pro zobrazení
 * ve frontendovém rozhraní.
 *
 * DTO neobsahuje žádnou business logiku.
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