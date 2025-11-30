package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.models.services.PlayerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public List<PlayerEntity> getAllPlayers() {
        return playerRepository.findAll();
    }

    @Override
    public PlayerEntity getPlayerById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hráč nenalezen: " + id));
    }

    @Override
    public PlayerEntity createPlayer(PlayerEntity player) {
        return playerRepository.save(player);
    }

    @Override
    public PlayerEntity updatePlayer(Long id, PlayerEntity newData) {
        PlayerEntity existing = getPlayerById(id);
        existing.setName(newData.getName());
        existing.setSurname(newData.getSurname());
        return playerRepository.save(existing);
    }

    @Override
    public void deletePlayer(Long id) {
        playerRepository.deleteById(id);
    }
}
