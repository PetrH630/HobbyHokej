package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.repositories.MatchRegistrationHistoryRepository;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.exceptions.MatchNotFoundException;
import cz.phsoft.hokej.models.dto.MatchRegistrationHistoryDTO;
import cz.phsoft.hokej.models.mappers.MatchRegistrationHistoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementace rozhraní MatchRegistrationHistoryService.
 *
 * Třída zajišťuje načítání historických záznamů registrací hráčů
 * k zápasům z databáze a jejich převod do DTO. Odpovědností je
 * ověření existence zápasu, provedení dotazu do historie a mapování
 * výsledků do podoby vhodné pro controller a frontend.
 *
 * Třída neprovádí žádné změny stavu systému. Slouží jako read-only
 * vrstva nad auditními daty a nenahrazuje hlavní logiku registrací.
 */
@Service
public class MatchRegistrationHistoryServiceImpl implements MatchRegistrationHistoryService {

    /**
     * Repository pro čtení historických záznamů registrací.
     */
    private final MatchRegistrationHistoryRepository historyRepository;

    /**
     * Mapper pro převod historických entit do DTO.
     */
    private final MatchRegistrationHistoryMapper historyMapper;

    /**
     * Service pro práci s aktuálně zvoleným hráčem.
     *
     * Používá se při načítání historie pro přihlášeného hráče.
     */
    private final CurrentPlayerService currentPlayerService;

    /**
     * Repository pro práci se zápasy.
     *
     * Používá se k ověření, že požadovaný zápas existuje.
     */
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
     * Načte historii registrací aktuálně přihlášeného hráče pro daný zápas.
     *
     * Nejprve se ověří, že zápas existuje. Poté se ověří,
     * že je nastaven aktuální hráč, a získá se jeho identifikátor.
     * Následně se načtou auditní záznamy pro kombinaci daného zápasu
     * a aktuálního hráče. Výsledky jsou mapovány do DTO.
     *
     * @param matchId ID zápasu
     * @return seznam historických záznamů registrace aktuálního hráče k zápasu
     * @throws MatchNotFoundException pokud zápas s daným ID neexistuje
     */
    @Override
    public List<MatchRegistrationHistoryDTO> getHistoryForCurrentPlayerAndMatch(Long matchId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));

        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();

        var history = historyRepository
                .findByMatchIdAndPlayerIdOrderByChangedAtDesc(match.getId(), currentPlayerId);

        return historyMapper.toDTOList(history);
    }

    /**
     * Načte historii registrací zadaného hráče pro daný zápas.
     *
     * Metoda je vhodná pro administrativní a auditní endpointy,
     * kde se nepracuje s kontextem aktuálního hráče, ale s konkrétním
     * hráčem určeným parametrem. Nejprve se ověří existence zápasu,
     * poté se načtou odpovídající historické záznamy a převedou se do DTO.
     *
     * @param matchId  ID zápasu
     * @param playerId ID hráče
     * @return seznam historických záznamů registrace hráče k zápasu
     * @throws MatchNotFoundException pokud zápas s daným ID neexistuje
     */
    @Override
    public List<MatchRegistrationHistoryDTO> getHistoryForPlayerAndMatch(Long matchId, Long playerId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));

        var history = historyRepository
                .findByMatchIdAndPlayerIdOrderByChangedAtDesc(match.getId(), playerId);

        return historyMapper.toDTOList(history);
    }
}
