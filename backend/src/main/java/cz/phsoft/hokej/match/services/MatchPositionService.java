package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.dto.MatchPositionOverviewDTO;
import cz.phsoft.hokej.match.dto.MatchTeamPositionOverviewDTO;
import cz.phsoft.hokej.player.enums.Team;

/**
 * Servisní rozhraní pro přehled obsazenosti pozic v zápase.
 *
 * Třída poskytuje čtecí operace nad rozložením pozic na ledě.
 * Kombinuje konfiguraci zápasu s aktuálními registracemi hráčů
 * a vrací agregovaný přehled kapacity a obsazenosti.
 */
public interface MatchPositionService {

    /**
     * Vrací přehled kapacity a obsazenosti pozic pro oba týmy.
     *
     * @param matchId identifikátor zápasu
     * @return agregovaný přehled pozic pro oba týmy
     */
    MatchPositionOverviewDTO getPositionOverviewForMatch(Long matchId);

    /**
     * Vrací přehled kapacity a obsazenosti pozic pro konkrétní tým.
     *
     * @param matchId identifikátor zápasu
     * @param team tým, pro který se přehled sestavuje
     * @return přehled pozic pro daný tým
     */
    MatchTeamPositionOverviewDTO getPositionOverviewForMatchAndTeam(Long matchId, Team team);
}