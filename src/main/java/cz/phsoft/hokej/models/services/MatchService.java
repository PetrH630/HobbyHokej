package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.models.dto.MatchDTO;

public interface MatchService {

    MatchDTO prihlasitHrace(Long matchId, Long playerId);

    MatchDTO odhlasitHrace(Long matchId, Long playerId);
}