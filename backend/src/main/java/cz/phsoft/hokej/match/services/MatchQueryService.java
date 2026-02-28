package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.dto.MatchDTO;
import cz.phsoft.hokej.match.dto.MatchDetailDTO;
import cz.phsoft.hokej.match.dto.MatchOverviewDTO;

import java.util.List;

/**
 * Service vrstva pro čtecí operace nad zápasy.
 *
 * Poskytuje metody pro načítání seznamů zápasů, detailu zápasu
 * a přehledů zápasů pro konkrétního hráče. Neprovádí žádné změny
 * stavu v databázi ani neodesílá notifikace.
 */
public interface MatchQueryService {

    List<MatchDTO> getAllMatches();

    List<MatchDTO> getUpcomingMatches();

    List<MatchDTO> getPastMatches();

    MatchDTO getNextMatch();

    MatchDTO getMatchById(Long id);

    MatchDetailDTO getMatchDetail(Long id);

    List<MatchDTO> getAvailableMatchesForPlayer(Long playerId);

    Long getPlayerIdByEmail(String email);

    List<MatchOverviewDTO> getUpcomingMatchesOverviewForPlayer(Long playerId);

    List<MatchDTO> getUpcomingMatchesForPlayer(Long playerId);

    List<MatchOverviewDTO> getAllPassedMatchesForPlayer(Long playerId);
}