package cz.phsoft.hokej.player.dto;

import cz.phsoft.hokej.match.enums.MatchResult;
import cz.phsoft.hokej.player.enums.Team;

/**
 * Datový přenosový objekt reprezentující výsledek jednoho zápasu
 * pro potřeby statistik hráče.
 *
 * Objekt se používá jako položka kolekce v PlayerStatsDTO
 * a obsahuje výsledek zápasu, finální skóre a tým hráče v daném zápase.
 */
public class PlayerMatchResultDTO {

    /**
     * Identifikátor zápasu.
     */
    private Long matchId;

    /**
     * Tým, ve kterém byl hráč v daném zápase registrován.
     */
    private Team playerTeam;

    /**
     * Výsledek zápasu vyhodnocený z doménového objektu MatchScore.
     */
    private MatchResult result;

    /**
     * Skóre týmu DARK.
     */
    private Integer scoreDark;

    /**
     * Skóre týmu LIGHT.
     */
    private Integer scoreLight;

    /**
     * Příznak, že hráčův tým v zápase vyhrál.
     *
     * Hodnota je true pouze v případě, že hráč nebyl v remíze
     * a vítězný tým odpovídá playerTeam.
     */
    private boolean playerWon;

    /**
     * Příznak, že zápas skončil remízou.
     *
     * Hodnota je true v případě, že výsledek zápasu je DRAW.
     */
    private boolean draw;

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }

    public Team getPlayerTeam() { return playerTeam; }
    public void setPlayerTeam(Team playerTeam) { this.playerTeam = playerTeam; }

    public MatchResult getResult() { return result; }
    public void setResult(MatchResult result) { this.result = result; }

    public Integer getScoreDark() { return scoreDark; }
    public void setScoreDark(Integer scoreDark) { this.scoreDark = scoreDark; }

    public Integer getScoreLight() { return scoreLight; }
    public void setScoreLight(Integer scoreLight) { this.scoreLight = scoreLight; }

    public boolean isPlayerWon() { return playerWon; }
    public void setPlayerWon(boolean playerWon) { this.playerWon = playerWon; }

    public boolean isDraw() { return draw; }
    public void setDraw(boolean draw) { this.draw = draw; }
}