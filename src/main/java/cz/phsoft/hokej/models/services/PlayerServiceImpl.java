package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.data.enums.Role;
import cz.phsoft.hokej.data.enums.PlayerType;
import org.springframework.stereotype.Service;

import java.util.List;

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
                .orElseThrow(() -> new RuntimeException("Player not found: " + id));
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
        existing.setEmail(newData.getEmail());
        existing.setPhone(newData.getPhone());
        existing.setType(newData.getType());
        existing.setRole(newData.getRole());

        if (newData.getPlayerPassword() != null && !newData.getPlayerPassword().isBlank()) {
            existing.setPlayerPassword(newData.getPlayerPassword());
        }

        return playerRepository.save(existing);
    }

    @Override
    public void deletePlayer(Long id) {
        playerRepository.deleteById(id);
    }
}