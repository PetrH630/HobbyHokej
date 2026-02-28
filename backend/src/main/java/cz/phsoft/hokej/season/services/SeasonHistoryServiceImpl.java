package cz.phsoft.hokej.season.services;

import cz.phsoft.hokej.season.repositories.SeasonHistoryRepository;
import cz.phsoft.hokej.season.dto.SeasonHistoryDTO;
import cz.phsoft.hokej.season.mappers.SeasonHistoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementace service vrstvy používaná pro poskytování historických dat sezóny.
 *
 * Třída zajišťuje načtení záznamů historie sezóny z repository vrstvy a jejich převod do DTO.
 * Načítání dat se deleguje do {@link SeasonHistoryRepository} a mapování entit na DTO se deleguje
 * do {@link SeasonHistoryMapper}.
 *
 * Implementace neposkytuje zápisovou logiku a slouží pouze pro čtení a prezentaci historie sezóny
 * vyšším vrstvám aplikace, typicky controller vrstvě.
 */
@Service
public class SeasonHistoryServiceImpl implements SeasonHistoryService {

    private final SeasonHistoryRepository repository;
    private final SeasonHistoryMapper mapper;

    public SeasonHistoryServiceImpl(
            SeasonHistoryRepository repository,
            SeasonHistoryMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Vrací historická data pro zadanou sezónu seřazená od nejnovější změny.
     *
     * Data se načítají z repository vrstvy filtrováním podle identifikátoru sezóny.
     * Výsledek se převádí do {@link SeasonHistoryDTO} pomocí mapper vrstvy.
     *
     * @param seasonId Identifikátor sezóny, pro kterou se historie načítá.
     * @return Seznam historických záznamů sezóny ve formě DTO seřazený sestupně podle času změny.
     */
    @Override
    public List<SeasonHistoryDTO> getHistoryForSeason(Long seasonId) {
        return mapper.toDTOList(
                repository.findBySeasonIdOrderByChangedAtDesc(seasonId)
        );
    }
}
