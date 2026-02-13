package cz.phsoft.hokej.models.dto;

/**
 * DTO reprezentující informaci o aktuálním režimu zastoupení.
 *
 * Obsahuje příznak, zda je aktivní impersonace, a případně
 * identifikátor a jméno zastupovaného hráče.
 */
public class ImpersonationInfoDTO {

    private boolean impersonating;
    private Long playerId;
    private String playerName;

    public ImpersonationInfoDTO(boolean impersonating, Long playerId, String playerName) {
        this.impersonating = impersonating;
        this.playerId = playerId;
        this.playerName = playerName;
    }

    public boolean isImpersonating() {
        return impersonating;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }
}
