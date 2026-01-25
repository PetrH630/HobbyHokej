package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.enums.Team;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.requests.MatchRegistrationRequest;

import java.util.List;

public interface MatchRegistrationService {

    // 🔥 Vrací DTO místo entity
    MatchRegistrationDTO upsertRegistration(
            Long playerId,
            MatchRegistrationRequest request
    );

    List<MatchRegistrationDTO> getRegistrationsForMatch(Long matchId);

    List<MatchRegistrationDTO> getRegistrationsForMatches(List<Long> matchIds);

    List<MatchRegistrationDTO> getAllRegistrations();

    List<MatchRegistrationDTO> getRegistrationsForPlayer(Long playerId);

    List<PlayerDTO> getNoResponsePlayers(Long matchId);

    void recalcStatusesForMatch(Long matchId);

    MatchRegistrationDTO updateStatus(Long matchId, Long playerId, PlayerMatchStatus status);

    MatchRegistrationDTO markNoExcused(Long matchId, Long playerId, String adminNote);


}
