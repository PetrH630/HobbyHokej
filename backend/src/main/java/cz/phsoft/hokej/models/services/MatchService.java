package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.models.dto.MatchDTO;
import cz.phsoft.hokej.models.dto.MatchDetailDTO;
import cz.phsoft.hokej.models.dto.MatchOverviewDTO;
import cz.phsoft.hokej.models.dto.SuccessResponseDTO;

import java.util.List;

public interface MatchService {
    List<MatchDTO> getAllMatches();
    List<MatchDTO> getUpcomingMatches();
    List<MatchDTO> getPastMatches();
    MatchDTO getNextMatch();
    MatchDTO getMatchById(Long id);
    MatchDTO createMatch(MatchDTO dto);
    MatchDTO updateMatch(Long id, MatchDTO dto);
    SuccessResponseDTO deleteMatch(Long id);
    MatchDetailDTO getMatchDetail(Long id);
    List<MatchDTO> getAvailableMatchesForPlayer(Long playerId);
    List<MatchDTO> getUpcomingMatchesForPlayer(Long playerId);
    Long getPlayerIdByEmail(String email);
    List<MatchOverviewDTO> getUpcomingMatchesOverviewForPlayer(Long playerId);


}
