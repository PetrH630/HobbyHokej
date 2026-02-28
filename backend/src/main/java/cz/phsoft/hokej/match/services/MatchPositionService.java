package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.util.MatchModeLayoutUtil;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.match.dto.MatchPositionOverviewDTO;
import cz.phsoft.hokej.registration.dto.MatchTeamPositionOverviewDTO;
import cz.phsoft.hokej.registration.services.MatchRegistrationService;

/**
 * Service vrstva pro přehled obsazenosti pozic v zápase.
 *
 * Třída poskytuje čtecí operace nad rozložením pozic na ledě pro daný zápas.
 * Vyhodnocuje kapacitu pozic podle MatchMode a maxPlayers a kombinuje ji
 * s aktuální obsazeností podle registrací hráčů.
 *
 * Pro čtení registrací se používá {@link MatchRegistrationService}.
 * Pro získání konfigurace pozic a jejich kapacity se používá
 * {@link MatchModeLayoutUtil}.
 */
public interface MatchPositionService {

    /**
     * Sestavuje přehled kapacity a obsazenosti pozic pro konkrétní zápas
     * pro oba týmy.
     *
     * @param matchId Identifikátor zápasu.
     * @return Přehled pozic a jejich obsazenosti ve formě DTO.
     */
    MatchPositionOverviewDTO getPositionOverviewForMatch(Long matchId);

    /**
     * Sestavuje přehled kapacity a obsazenosti pozic pro konkrétní zápas
     * a konkrétní tým.
     *
     * Kapacita pro jednotlivé pozice na tým se odvodí pomocí
     * {@link MatchModeLayoutUtil}. Následně se načtou registrace hráčů
     * a spočítá se obsazenost pozic pouze pro zvolený tým.
     *
     * @param matchId Identifikátor zápasu.
     * @param team    Tým, pro který se přehled sestavuje.
     * @return Přehled pozic a jejich obsazenosti pro daný tým.
     */
    MatchTeamPositionOverviewDTO getPositionOverviewForMatchAndTeam(Long matchId, Team team);
}