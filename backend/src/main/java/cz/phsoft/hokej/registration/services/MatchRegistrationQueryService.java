package cz.phsoft.hokej.registration.services;

import cz.phsoft.hokej.registration.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.player.dto.PlayerDTO;

import java.util.List;

/**
 * Service vrstva pro čtecí operace nad registracemi hráčů.
 *
 * Poskytuje přehledy registrací a hráčů podle zápasů a sezóny.
 */
public interface MatchRegistrationQueryService {

    List<MatchRegistrationDTO> getRegistrationsForMatch(Long matchId);

    List<MatchRegistrationDTO> getRegistrationsForMatches(List<Long> matchIds);

    List<MatchRegistrationDTO> getAllRegistrations();

    List<MatchRegistrationDTO> getRegistrationsForPlayer(Long playerId);

    List<PlayerDTO> getNoResponsePlayers(Long matchId);
}