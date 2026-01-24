package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.SuccessResponseDTO;

import java.util.List;


public interface PlayerService {
    List<PlayerDTO> getAllPlayers();
    PlayerDTO getPlayerById(Long id);
    PlayerDTO createPlayer(PlayerDTO player);
    PlayerDTO createPlayerForUser(PlayerDTO dto, String userEmail);
    PlayerDTO updatePlayer(Long id, PlayerDTO player);
    SuccessResponseDTO deletePlayer(Long id);
    List<PlayerDTO> getPlayersByUser(String email);
    SuccessResponseDTO approvePlayer (Long id);
    SuccessResponseDTO rejectPlayer (Long id);
    SuccessResponseDTO setCurrentPlayerForUser(String userEmail, Long playerId);
    SuccessResponseDTO autoSelectCurrentPlayerForUser(String userEmail);

}
