package cz.phsoft.hokej.registration.services;

import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.registration.repositories.MatchRegistrationHistoryRepository;
import cz.phsoft.hokej.match.repositories.MatchRepository;
import cz.phsoft.hokej.match.exceptions.MatchNotFoundException;
import cz.phsoft.hokej.registration.dto.MatchRegistrationHistoryDTO;
import cz.phsoft.hokej.registration.mappers.MatchRegistrationHistoryMapper;
import cz.phsoft.hokej.player.services.CurrentPlayerService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementace služby pro práci s historickými záznamy registrací hráčů k zápasům.
 *
 * Tato třída zajišťuje načítání auditních záznamů registrací z databáze
 * a jejich převod do přenosových objektů DTO. Odpovědností je ověření existence
 * zápasu, provedení dotazů nad historickými daty a mapování výsledků do podoby
 * vhodné pro controller vrstvu a frontend.
 *
 * Třída neprovádí žádné změny stavu systému. Používá se jako read-only vrstva
 * nad auditními daty a nenahrazuje hlavní logiku registrací.
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
     * Načítá historii registrací aktuálně přihlášeného hráče pro daný zápas.
     *
     * Nejprve se ověřuje, že zápas s daným identifikátorem existuje.
     * Následně se pomocí CurrentPlayerService zajistí, že je k dispozici
     * aktuální hráč, a získá se jeho identifikátor. Poté se načtou auditní
     * záznamy pro kombinaci daného zápasu a aktuálního hráče a výsledky
     * se převedou do DTO objektů.
     *
     * @param matchId identifikátor zápasu
     * @return seznam historických záznamů registrace aktuálního hráče k zápasu
     * @throws MatchNotFoundException pokud zápas s daným identifikátorem neexistuje
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
     * Načítá historii registrací zadaného hráče pro daný zápas.
     *
     * Metoda se používá zejména pro administrativní a auditní účely,
     * kde se nepracuje s kontextem aktuálního hráče, ale s hráčem určeným
     * parametrem. Nejprve se ověřuje existence zápasu a následně se načtou
     * odpovídající historické záznamy pro zadaného hráče. Výsledky se převádějí
     * do přenosových objektů DTO.
     *
     * @param matchId identifikátor zápasu
     * @param playerId identifikátor hráče
     * @return seznam historických záznamů registrace hráče k zápasu
     * @throws MatchNotFoundException pokud zápas s daným identifikátorem neexistuje
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