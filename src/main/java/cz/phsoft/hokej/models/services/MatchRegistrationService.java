package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;

import java.util.List;

public interface MatchRegistrationService {

    // Přihlásit hráče
    MatchRegistrationEntity registerPlayer(Long matchId, Long playerId);

    // Odhlásit hráče
    MatchRegistrationEntity unregisterPlayer(Long matchId, Long playerId, String note, String reason);

    // Omluvit hráče s důvodem
    MatchRegistrationEntity excusePlayer(Long matchId, Long playerId, String note, String reason);

    // Získat poslední status hráče u zápasu
    MatchRegistrationEntity getLastStatus(Long matchId, Long playerId);

    List<MatchRegistrationEntity> getLastStatusesForMatch(Long matchId);

    // Seznam všech registrací pro zápas
    List<MatchRegistrationEntity> getRegistrationsForMatch(Long matchId);

    List<MatchRegistrationEntity> getAllRegistrations();

    List<MatchRegistrationEntity> getRegistrationsForPlayer(Long playerId);

    // Získat hráče co se vůbec nevyjádřil
    List<PlayerEntity> getNoResponsePlayers(Long matchId);

    void recalcStatusesForMatch(Long matchId);

}
