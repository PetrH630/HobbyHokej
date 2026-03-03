package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.player.repositories.PlayerHistoryRepository;
import cz.phsoft.hokej.player.dto.PlayerHistoryDTO;
import cz.phsoft.hokej.player.mappers.PlayerHistoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementace servisní vrstvy pro práci s historií hráčů.
 *
 * Třída zajišťuje čtení auditních záznamů změn hráčů.
 * Neprovádí žádné zápisy do databáze, protože historické
 * záznamy jsou vytvářeny na úrovni databáze pomocí triggerů.
 *
 * Odpovědnost:
 * - načtení historických záznamů z repository vrstvy,
 * - převod entit na DTO pomocí mapperu,
 * - předání výsledku vyšší aplikační vrstvě.
 */
@Service
public class PlayerHistoryServiceImpl
        implements PlayerHistoryService {

    private final PlayerHistoryRepository repository;
    private final PlayerHistoryMapper mapper;

    /**
     * Vytvoří instanci servisní třídy pro práci s historií hráčů.
     *
     * Závislosti jsou injektovány konstruktorově.
     *
     * @param repository repository zajišťující přístup k historickým záznamům
     * @param mapper mapper zajišťující převod entity na DTO
     */
    public PlayerHistoryServiceImpl(
            PlayerHistoryRepository repository,
            PlayerHistoryMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Vrátí historii změn daného hráče.
     *
     * Metoda deleguje načtení historických záznamů
     * na repository vrstvu a následně provede jejich
     * převod na DTO reprezentaci pomocí mapperu.
     *
     * Záznamy jsou vráceny v pořadí od nejnovější změny
     * po nejstarší.
     *
     * @param playerId identifikátor hráče
     * @return seznam historických záznamů ve formě PlayerHistoryDTO
     */
    @Override
    public List<PlayerHistoryDTO> getHistoryForPlayer(Long playerId) {
        return mapper.toDTOList(
                repository.findByPlayerIdOrderByChangedAtDesc(playerId)
        );
    }
}