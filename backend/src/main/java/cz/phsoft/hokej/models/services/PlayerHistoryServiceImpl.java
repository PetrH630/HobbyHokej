package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.repositories.PlayerHistoryRepository;
import cz.phsoft.hokej.models.dto.PlayerHistoryDTO;
import cz.phsoft.hokej.models.mappers.PlayerHistoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementace servisní vrstvy pro práci s historií hráčů.
 *
 * Neprovádí žádné zápisy do databáze – historické záznamy
 * jsou vytvářeny databázovými triggery.
 */
@Service
public class PlayerHistoryServiceImpl
        implements PlayerHistoryService {

    private final PlayerHistoryRepository repository;
    private final PlayerHistoryMapper mapper;

    public PlayerHistoryServiceImpl(
            PlayerHistoryRepository repository,
            PlayerHistoryMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<PlayerHistoryDTO> getHistoryForPlayer(Long playerId) {
        return mapper.toDTOList(
                repository.findByPlayerIdOrderByChangedAtDesc(playerId)
        );
    }
}
