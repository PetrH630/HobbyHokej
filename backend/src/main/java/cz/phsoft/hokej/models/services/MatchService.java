package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.models.dto.*;

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
    List<MatchOverviewDTO> getAllPassedMatchesForPlayer(Long playerId);
    MatchRegistrationDTO markNoExcused(Long matchId, Long playerId, String adminNote);

}
