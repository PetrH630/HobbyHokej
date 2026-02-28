package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.player.dto.PlayerDTO;

import java.util.List;

public interface PlayerQueryService {

    List<PlayerDTO> getAllPlayers();

    PlayerDTO getPlayerById(Long id);

    List<PlayerDTO> getPlayersByUser(String email);
}