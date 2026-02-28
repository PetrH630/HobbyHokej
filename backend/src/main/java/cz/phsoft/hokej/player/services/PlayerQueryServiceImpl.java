package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.player.dto.PlayerDTO;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.player.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.player.mappers.PlayerMapper;
import cz.phsoft.hokej.player.repositories.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Query služba pro čtecí operace nad hráči.
 *
 * Odpovědnosti:
 * - poskytování seznamu všech hráčů,
 * - načtení hráče podle ID,
 * - načtení hráčů podle uživatele.
 *
 * Neprovádí žádné změny stavu systému ani neodesílá notifikace.
 */
@Service
public class PlayerQueryServiceImpl implements PlayerQueryService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;

    public PlayerQueryServiceImpl(PlayerRepository playerRepository,
                                  PlayerMapper playerMapper) {
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
                .orElseThrow(() -> new PlayerNotFoundException(id));
        return playerMapper.toDTO(player);
    }

    @Override
    public List<PlayerDTO> getPlayersByUser(String email) {
        return playerRepository.findByUser_EmailOrderByIdAsc(email).stream()
                .map(playerMapper::toDTO)
                .toList();
    }
}