package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import java.util.List;


public interface PlayerService {
    List<PlayerEntity> getAllPlayers();
    PlayerEntity getPlayerById(Long id);
    PlayerEntity createPlayer(PlayerEntity player);
    PlayerEntity updatePlayer(Long id, PlayerEntity player);
    void deletePlayer(Long id);
}
