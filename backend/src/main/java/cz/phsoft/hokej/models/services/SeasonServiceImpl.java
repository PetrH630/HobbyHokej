package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.SeasonEntity;
import cz.phsoft.hokej.data.repositories.SeasonRepository;
import cz.phsoft.hokej.exceptions.InvalidSeasonPeriodDateException;
import cz.phsoft.hokej.exceptions.SeasonNotFoundException;
import cz.phsoft.hokej.exceptions.SeasonPeriodOverlapException;
import cz.phsoft.hokej.models.dto.SeasonDTO;
import cz.phsoft.hokej.models.dto.mappers.SeasonMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class SeasonServiceImpl implements SeasonService {

    private final SeasonRepository seasonRepository;
    private final SeasonMapper mapper;

    public SeasonServiceImpl(SeasonRepository seasonRepository, SeasonMapper mapper) {
        this.seasonRepository = seasonRepository;
        this.mapper = mapper;
    }

    // ======================
    // CREATE
    // ======================
    @Override
    @Transactional
    public SeasonDTO createSeason(SeasonDTO seasonDTO) {
        // u create nemáme ID
        validateDates(seasonDTO, null);

        SeasonEntity entity = mapper.toEntity(seasonDTO);
        SeasonEntity saved = seasonRepository.save(entity);
        return mapper.toDTO(saved);
    }

    // ======================
    // UPDATE
    // ======================
    @Override
    @Transactional
    public SeasonDTO updateSeason(Long id, SeasonDTO seasonDTO) {
        // 1) najít existující sezónu
        SeasonEntity existing = seasonRepository.findById(id)
                .orElseThrow(() -> new SeasonNotFoundException(
                        "BE - Sezóna s ID " + id + " nebyla nalezena."
                ));

        // 2) validace dat s ignorováním této sezóny
        validateDates(seasonDTO, id);

        // 3) promítnout změny a uložit
        mapper.updateEntityFromDTO(seasonDTO, existing);
        SeasonEntity saved = seasonRepository.save(existing);

        return mapper.toDTO(saved);
    }

    // ======================
    // AKTIVNÍ SEZÓNA
    // ======================
    @Override
    public SeasonEntity getActiveSeason() {
        return seasonRepository.findByActiveTrue()
                .orElseThrow(() -> new SeasonNotFoundException(
                        "BE - Není nastavena žádná aktivní sezóna."
                ));
    }

    // Pokud chceš raději DTO:
    // @Override
    // public SeasonDTO getActiveSeason() {
    //     SeasonEntity entity = seasonRepository.findByActiveTrue()
    //             .orElseThrow(() -> new SeasonNotFoundException(
    //                     "BE - Není nastavena žádná aktivní sezóna."
    //             ));
    //     return mapper.toDTO(entity);
    // }

    // ======================
    // SEZNAM VŠECH SEZÓN
    // ======================
    @Override
    public List<SeasonDTO> getAllSeasons() {
        return seasonRepository.findAllByOrderByStartDateAsc()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    // ======================
    // NASTAVENÍ AKTIVNÍ SEZÓNY
    // ======================
    @Override
    @Transactional
    public void setActiveSeason(Long seasonId) {
        // 1) ověř, že existuje
        SeasonEntity toActivate = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new SeasonNotFoundException(
                        "BE - Sezóna s ID " + seasonId + " nebyla nalezena."
                ));

        // 2) načíst všechny sezóny a přepnout příznak
        List<SeasonEntity> all = seasonRepository.findAll();
        for (SeasonEntity season : all) {
            season.setActive(season.getId().equals(seasonId));
        }

        seasonRepository.saveAll(all);
    }

    // ======================
    // PRIVÁTNÍ VALIDACE DAT
    // ======================
    /**
     * Validace období sezóny + kontrola překryvu.
     *
     * @param seasonDTO        data sezóny
     * @param currentSeasonId  ID sezóny při updatu (pro create null)
     */
    private void validateDates(SeasonDTO seasonDTO, Long currentSeasonId) {
        LocalDate start = seasonDTO.getStartDate();
        LocalDate end = seasonDTO.getEndDate();

        if (start == null || end == null) {
            throw new InvalidSeasonPeriodDateException("BE - Datum od a do nesmí být null.");
        }

        if (!start.isBefore(end)) {
            throw new InvalidSeasonPeriodDateException("BE - Datum 'od' musí být před 'do'.");
        }

        boolean overlaps;

        if (currentSeasonId == null) {
            // CREATE – překryv s jakoukoli existující sezónou
            overlaps = seasonRepository
                    .existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(end, start);
        } else {
            // UPDATE – překryv s jinou sezónou (id != currentSeasonId)
            overlaps = seasonRepository
                    .existsByStartDateLessThanEqualAndEndDateGreaterThanEqualAndIdNot(
                            end,
                            start,
                            currentSeasonId
                    );
        }

        if (overlaps) {
            throw new SeasonPeriodOverlapException("BE - Sezóna se překrývá s existující sezónou.");
        }
    }
}
