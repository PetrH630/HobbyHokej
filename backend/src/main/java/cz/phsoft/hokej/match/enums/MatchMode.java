package cz.phsoft.hokej.match.enums;

/**
 * Výčtový typ reprezentující herní režim zápasu.
 *
 * Definuje počet hráčů v poli na jeden tým
 * a informaci, zda je součástí sestavy brankář.
 *
 * Enum zároveň obsahuje odvozenou doménovou logiku
 * pro výpočet maximální kapacity hráčů.
 */
public enum MatchMode {

    THREE_ON_THREE_NO_GOALIE(3, false),
    THREE_ON_THREE_WITH_GOALIE(3, true),
    FOUR_ON_FOUR_NO_GOALIE(4, false),
    FOUR_ON_FOUR_WITH_GOALIE(4, true),
    FIVE_ON_FIVE_NO_GOALIE(5, false),
    FIVE_ON_FIVE_WITH_GOALIE(5, true),
    SIX_ON_SIX_NO_GOALIE(6, false);

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

    public int getSkatersPerTeam() {
        return skatersPerTeam;
    }

    public boolean isGoalieIncluded() {
        return goalieIncluded;
    }

    /**
     * Vypočítá maximální počet hráčů na jeden tým.
     *
     * Hráči v poli se počítají s dvojnásobnou kapacitou
     * z důvodu střídání. Brankář se nestřídá.
     *
     * @return maximální počet hráčů na jeden tým
     */
    public int getPlayersPerTeam() {
        return (skatersPerTeam * 2) + (goalieIncluded ? 1 : 0);
    }

    /**
     * Vypočítá celkový maximální počet hráčů v zápase.
     *
     * @return maximální počet hráčů pro oba týmy dohromady
     */
    public int getTotalPlayers() {
        return getPlayersPerTeam() * 2;
    }
}