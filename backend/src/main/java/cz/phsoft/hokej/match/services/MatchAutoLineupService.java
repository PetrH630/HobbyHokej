package cz.phsoft.hokej.match.services;

public interface MatchAutoLineupService {

    /**
     * Automaticky přeskupí pozice hráčů v zápase tak, aby byly
     * co nejlépe obsazeny všechny posty první lajny pro oba týmy.
     *
     * Používá:
     * - konfiguraci pozic z MatchModeLayoutUtil,
     * - maxPlayers (slotsPerTeam = maxPlayers / 2),
     * - current REGISTERED hráče v obou týmech,
     * - preference hráčů (primary/secondary position),
     * - timestamp registrace (nejmladší = nejpozději registrovaný).
     *
     * Nemění tým (DARK/LIGHT), jen pozici v rámci týmu.
     */
    void autoArrangeStartingLineup(Long matchId);
}