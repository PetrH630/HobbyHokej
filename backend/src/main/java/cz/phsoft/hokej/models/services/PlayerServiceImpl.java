package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.models.dto.mappers.PlayerMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import java.util.List;
import java.util.Optional;
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
        return playerRepository.findAll().stream()
                .map(playerMapper::toDTO)
                .toList();
    }

    @Override
    public PlayerDTO getPlayerById(Long id) {
        PlayerEntity player = playerRepository.findById(id)
                .orElseThrow(() -> new PlayerNotFoundException(id)); // místo RuntimeException
        return playerMapper.toDTO(player);
    }

    // --- TRANSACTIONAL pro zápis dat ---
    @Override
    @Transactional
    public PlayerDTO createPlayer(PlayerDTO dto) {
        checkDuplicateNameSurname(dto.getName(), dto.getSurname(), null);

        PlayerEntity entity = playerMapper.toEntity(dto);
        PlayerEntity saved = playerRepository.save(entity);
        return playerMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public PlayerDTO updatePlayer(Long id, PlayerDTO dto) {
        PlayerEntity existing = findPlayerOrThrow(id);

        // pokud se jméno/příjmení mění, ověř duplicitu
        if (!existing.getName().equals(dto.getName()) || !existing.getSurname().equals(dto.getSurname())) {
            checkDuplicateNameSurname(dto.getName(), dto.getSurname(), id);
        }

        existing.setName(dto.getName());
        existing.setSurname(dto.getSurname());
        existing.setType(dto.getType());
        existing.setJerseyColor(dto.getJerseyColor());

        PlayerEntity saved = playerRepository.save(existing);
        return playerMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public void deletePlayer(Long id) {
        playerRepository.deleteById(id);
    }

    // --- privátní metoda pro kontrolu duplicity jména a příjmení ---
    private void checkDuplicateNameSurname(String name, String surname, Long ignoreId) {
        Optional<PlayerEntity> duplicateOpt = playerRepository.findByNameAndSurname(name, surname);

        if (duplicateOpt.isPresent()) {
            if (ignoreId == null || !duplicateOpt.get().getId().equals(ignoreId)) {
                throw new RuntimeException("Hráč se jménem " + name + " " + surname + " již existuje.");
            }
        }
        }


    private PlayerEntity findPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }
}