package cz.phsoft.hokej.match.enums;

public enum MatchMode {
    THREE_ON_THREE_NO_GOALIE(3, false),  //  3 na 3 bez brankáře - 12
    THREE_ON_THREE_WITH_GOALIE(3, true),  //  3 na 3 s brankářem - 14
    FOUR_ON_FOUR_NO_GOALIE(4, false), // 4 na 4 bez brankáře - 16
    FOUR_ON_FOUR_WITH_GOALIE(4, true),  // 4 na 4 s brankářem - 18
    FIVE_ON_FIVE_NO_GOALIE(5, false), // 5 na 5 bez brankáře - 20
    FIVE_ON_FIVE_WITH_GOALIE(5, true), // 5 na 5 s brankářem - 22
    SIX_ON_SIX_NO_GOALIE(6, false); // 6 na 6 bez brankáře - 24

    /**
     * Počet hráčů v poli na ledě na jeden tým.
     */
    private final int skatersPerTeam;

    /**
     * Indikuje, zda je součástí sestavy brankář.
     */
    private final boolean goalieIncluded;

    MatchMode(int skatersPerTeam, boolean goalieIncluded) {
        this.skatersPerTeam = skatersPerTeam;
        this.goalieIncluded = goalieIncluded;
    }

    /**
     * Vrací počet hráčů v poli na ledě.
     */
    public int getSkatersPerTeam() {
        return skatersPerTeam;
    }

    /**
     * Vrací informaci, zda je zahrnut brankář.
     */
    public boolean isGoalieIncluded() {
        return goalieIncluded;
    }

    /**
     * Vrací maximální počet hráčů na jeden tým.
     * Hráči v poli mají střídání (2x),
     * brankář se nestřídá.
     */
    public int getPlayersPerTeam() {
        return (skatersPerTeam * 2) + (goalieIncluded ? 1 : 0);
    }

    /**
     * Vrací celkový počet hráčů v zápase (oba týmy).
     */
    public int getTotalPlayers() {
        return getPlayersPerTeam() * 2;
    }
}