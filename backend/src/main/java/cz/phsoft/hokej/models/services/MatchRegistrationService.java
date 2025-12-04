package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.JerseyColor;


import java.util.List;

public interface MatchRegistrationService {


    // Přihlásit hráče s volitelnou barvou dresu a poznámkou admina
    MatchRegistrationEntity registerPlayer(Long matchId, Long playerId, JerseyColor jerseyColor, String adminNote);

    // Odhlásit hráče
    MatchRegistrationEntity unregisterPlayer(Long matchId, Long playerId, String note, String reason);

    // Omluvit hráče s důvodem
    MatchRegistrationEntity excusePlayer(Long matchId, Long playerId, String note, String reason);

    // Seznam všech registrací pro zápas
    List<MatchRegistrationEntity> getRegistrationsForMatch(Long matchId);

    // Seznam všech registrací
    List<MatchRegistrationEntity> getAllRegistrations();

    // Seznam registrací pro konkrétního hráče
    List<MatchRegistrationEntity> getRegistrationsForPlayer(Long playerId);

    // Získat hráče, kteří se vůbec nevyjádřili k zápasu
    List<PlayerEntity> getNoResponsePlayers(Long matchId);

    // Přepočet statusů REGISTERED / RESERVED podle kapacity
    void recalcStatusesForMatch(Long matchId);


}





