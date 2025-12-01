package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import java.util.List;

public interface MatchRegistrationService {

    // Přihlásit hráče
    MatchRegistrationEntity registerPlayer(Long matchId, Long playerId);

    // Odhlásit hráče
    MatchRegistrationEntity unregisterPlayer(Long matchId, Long playerId);

    // Omluvit hráče s důvodem
    MatchRegistrationEntity excusePlayer(Long matchId, Long playerId, String note, String reason);

    // Získat poslední status hráče u zápasu
    MatchRegistrationEntity getLastStatus(Long matchId, Long playerId);

    // Seznam všech registrací pro zápas
    List<MatchRegistrationEntity> getRegistrationsForMatch(Long matchId);

    List<MatchRegistrationEntity> getAllRegistrations();

    List<MatchRegistrationEntity> getRegistrationsForPlayer(Long playerId);
}
