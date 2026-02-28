package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.player.dto.PlayerDTO;
import cz.phsoft.hokej.shared.dto.SuccessResponseDTO;

public interface PlayerCommandService {

    PlayerDTO createPlayer(PlayerDTO dto);

    PlayerDTO createPlayerForUser(PlayerDTO dto, String userEmail);

    PlayerDTO updatePlayer(Long id, PlayerDTO dto);

    SuccessResponseDTO deletePlayer(Long id);

    SuccessResponseDTO approvePlayer(Long id);

    SuccessResponseDTO rejectPlayer(Long id);

    void changePlayerUser(Long id, Long newUserId);

    SuccessResponseDTO setCurrentPlayerForUser(String userEmail, Long playerId);

    SuccessResponseDTO autoSelectCurrentPlayerForUser(String userEmail);
}