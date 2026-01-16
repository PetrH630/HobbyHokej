package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.Team;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.PlayerDTO;

import java.util.List;

public interface MatchRegistrationService {

    // 🔥 Vrací DTO místo entity
    MatchRegistrationDTO upsertRegistration(
            Long matchId,
            Long playerId,
            Team team,
            String adminNote,
            ExcuseReason excuseReason,
            String excuseNote,
            boolean unregister
    );

    List<MatchRegistrationDTO> getRegistrationsForMatch(Long matchId);

    List<MatchRegistrationDTO> getRegistrationsForMatches(List<Long> matchIds);

    List<MatchRegistrationDTO> getAllRegistrations();

    List<MatchRegistrationDTO> getRegistrationsForPlayer(Long playerId);

    List<PlayerDTO> getNoResponsePlayers(Long matchId);

    void recalcStatusesForMatch(Long matchId);
}
