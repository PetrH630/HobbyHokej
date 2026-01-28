package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.repositories.MatchRegistrationHistoryRepository;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.exceptions.MatchNotFoundException;
import cz.phsoft.hokej.models.dto.MatchRegistrationHistoryDTO;
import cz.phsoft.hokej.models.dto.mappers.MatchRegistrationHistoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MatchRegistrationHistoryServiceImpl implements MatchRegistrationHistoryService{

    private final MatchRegistrationHistoryRepository historyRepository;
    private final MatchRegistrationHistoryMapper historyMapper;
    private final CurrentPlayerService currentPlayerService;
    private final MatchRepository matchRepository;

    public MatchRegistrationHistoryServiceImpl(
            MatchRegistrationHistoryRepository historyRepository,
            MatchRegistrationHistoryMapper historyMapper,
            CurrentPlayerService currentPlayerService,
            MatchRepository matchRepository
    ) {
        this.historyRepository = historyRepository;
        this.historyMapper = historyMapper;
        this.currentPlayerService = currentPlayerService;
        this.matchRepository = matchRepository;
    }

    /**
     * Vrátí historii registrací přihlášeného hráče pro daný zápas.
     */
    public List<MatchRegistrationHistoryDTO> getHistoryForCurrentPlayerAndMatch(Long matchId) {
        // ověření, že zápas existuje
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));

        // zjištění currentPlayer (předpokládám, že službu už máš)
        //PlayerEntity currentPlayer = currentPlayerService.getCurrentPlayerOrThrow();
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();


        // dotaz do historie
        var history = historyRepository
                .findByMatchIdAndPlayerIdOrderByChangedAtDesc(match.getId(), currentPlayerId);

        return historyMapper.toDTOList(history);
    }

    /**
     * Použitelná i pro admin účely – např. v jiném endpointu.
     */
    public List<MatchRegistrationHistoryDTO> getHistoryForPlayerAndMatch(Long matchId, Long playerId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));

        var history = historyRepository
                .findByMatchIdAndPlayerIdOrderByChangedAtDesc(match.getId(), playerId);

        return historyMapper.toDTOList(history);
    }
}
