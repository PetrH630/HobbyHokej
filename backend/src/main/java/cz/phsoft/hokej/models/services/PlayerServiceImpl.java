package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.models.dto.mappers.PlayerMapper;
import org.springframework.stereotype.Service;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;


    public PlayerServiceImpl(PlayerRepository playerRepository, PlayerMapper playerMapper) {
        this.playerRepository = playerRepository;
        this.playerMapper = playerMapper;
    }

    @Override
    public List<PlayerDTO> getAllPlayers() {
        return playerRepository.findAll()
                .stream()
                .map(playerMapper::toDTO)
                .collect(Collectors.toList());

    }

    @Override
    public PlayerDTO getPlayerById(Long id) {
            PlayerEntity player = findPlayerOrThrow(id);
            return playerMapper.toDTO(player);

    }

    @Override
    public PlayerDTO createPlayer(PlayerDTO dto) {
        // kontrola duplicity
        if (playerRepository.existsByNameAndSurname(dto.getName(), dto.getSurname())) {
            throw new RuntimeException("Hráč se jménem " + dto.getName() + " " + dto.getSurname() + " již existuje.");
        }

        PlayerEntity entity = playerMapper.toEntity(dto);
        PlayerEntity saved = playerRepository.save(entity);
        return playerMapper.toDTO(saved);
    }

    @Override
    public PlayerDTO updatePlayer(Long id, PlayerDTO dto) {

        PlayerEntity existing = findPlayerOrThrow(id);

        // pokud se jméno/příjmení mění, ověř duplicitu
        if ((!existing.getName().equals(dto.getName())
                || !existing.getSurname().equals(dto.getSurname()))
                && playerRepository.existsByNameAndSurname(dto.getName(), dto.getSurname())) {

            throw new RuntimeException("Hráč se jménem " + dto.getName() + " " + dto.getSurname() + " již existuje.");
        }

        // aktualizace dat
        existing.setName(dto.getName());
        existing.setSurname(dto.getSurname());
        existing.setType(dto.getType());
        existing.setJerseyColor(dto.getJerseyColor());

        PlayerEntity saved = playerRepository.save(existing);
        return playerMapper.toDTO(saved);
    }

    @Override
    public void deletePlayer(Long id) {
        playerRepository.deleteById(id);
    }

    private PlayerEntity findPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found: " + playerId));
    }

}