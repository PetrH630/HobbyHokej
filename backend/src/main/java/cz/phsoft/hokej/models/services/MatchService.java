package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.models.dto.MatchDTO;
import cz.phsoft.hokej.models.dto.MatchDetailDTO;

import java.util.List;

public interface MatchService {
    List<MatchDTO> getAllMatches();
    List<MatchDTO> getUpcomingMatches();
    List<MatchDTO> getPastMatches();
    MatchDTO getNextMatch();
    MatchDTO getMatchById(Long id);
    MatchDTO createMatch(MatchDTO dto);
    MatchDTO updateMatch(Long id, MatchDTO dto);
    void deleteMatch(Long id);
    MatchDetailDTO getMatchDetail(Long id);
    List<MatchEntity> getAvailableMatchesForPlayer(Long playerId);
}
