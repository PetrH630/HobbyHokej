package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.SeasonEntity;
import cz.phsoft.hokej.data.repositories.SeasonRepository;
import cz.phsoft.hokej.exceptions.InvalidSeasonPeriodDateException;
import cz.phsoft.hokej.exceptions.InvalidSeasonStateException;
import cz.phsoft.hokej.exceptions.SeasonNotFoundException;
import cz.phsoft.hokej.exceptions.SeasonPeriodOverlapException;
import cz.phsoft.hokej.models.dto.SeasonDTO;
import cz.phsoft.hokej.models.mappers.SeasonMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service vrstva pro správu sezón ({@link SeasonEntity}).
 *
 * Zodpovídá za:
 * <ul>
 *     <li>vytvoření nové sezóny (včetně validace dat a překryvů),</li>
 *     <li>úpravu existující sezóny (včetně pravidel pro aktivní sezónu),</li>
 *     <li>získání aktuálně aktivní sezóny,</li>
 *     <li>návrat všech sezón v logickém pořadí,</li>
 *     <li>nastavení konkrétní sezóny jako aktivní (a deaktivaci ostatních).</li>
 * </ul>
 *
 * DŮLEŽITÉ INVARIANTY:
 * <ul>
 *     <li>datum začátku musí být před datem konce,</li>
 *     <li>nesmí existovat dvě sezóny, které se časově překrývají,</li>
 *     <li>systém by neměl zůstat bez aktivní sezóny,</li>
 *     <li>v jeden okamžik smí být aktivní právě jedna sezóna.</li>
 * </ul>
 *
 * Třída je čistě doménová:
 * <ul>
 *     <li>neřeší autentizaci/autorizaci (řeší controller + Security),</li>
 *     <li>neposílá notifikace,</li>
 *     <li>zajišťuje konzistentní stav kalendáře sezón pro zbytek aplikace.</li>
 * </ul>
 */
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

    /**
     * Vytvoří novou sezónu.
     * <p>
     * Kroky:
     * <ol>
     *     <li>zvaliduje datumové rozmezí a překryvy s jinými sezónami,</li>
     *     <li>uloží novou sezónu,</li>
     *     <li>zajistí, že v systému vždy existuje nějaká aktivní sezóna:
     *         <ul>
     *             <li>pokud {@code seasonDTO.isActive() == true} → nastaví ji jako jedinou aktivní,</li>
     *             <li>pokud je to úplně první sezóna v systému (activeCount == 0),
     *                 nastaví tuto novou sezónu jako aktivní i když DTO říká {@code active = false}.</li>
     *         </ul>
     *     </li>
     * </ol>
     *
     * @param seasonDTO vstupní data sezóny
     * @return vytvořená sezóna v podobě {@link SeasonDTO}
     *
     * @throws InvalidSeasonPeriodDateException pokud jsou neplatná data od/do
     * @throws SeasonPeriodOverlapException     pokud se sezóna překrývá s existující
     */
    @Override
    @Transactional
    public SeasonDTO createSeason(SeasonDTO seasonDTO) {
        // u create nemáme ID → předáváme null, validace bere v potaz všechny existující sezóny
        validateDates(seasonDTO, null);

        SeasonEntity entity = mapper.toEntity(seasonDTO);
        SeasonEntity saved = seasonRepository.save(entity);

        long activeCount = seasonRepository.countByActiveTrue();

        if (seasonDTO.isActive()) {
            // nová sezóna má být aktivní → uděláme z ní jedinou aktivní
            setOnlyActiveSeason(saved.getId());
        } else if (activeCount == 0) {
            // v systému není žádná aktivní sezóna → nesmíme zůstat bez aktivní
            // → automaticky tuto novou sezónu nastavíme jako jedinou aktivní
            setOnlyActiveSeason(saved.getId());
        }

        return mapper.toDTO(saved);
    }

    // ======================
    // UPDATE
    // ======================

    /**
     * Aktualizuje existující sezónu.
     * <p>
     * Kroky:
     * <ol>
     *     <li>ověří, že sezóna s daným ID existuje,</li>
     *     <li>zvaliduje datumy s ignorováním této jedné sezóny
     *         (aby se "nepřekrývala sama se sebou"),</li>
     *     <li>zkontroluje, že nelze deaktivovat „poslední“ aktivní sezónu v systému,</li>
     *     <li>promítne změny z DTO do entity a uloží je,</li>
     *     <li>pokud se sezóna z neaktivní stala aktivní, nastaví ji jako JEDINOU aktivní.</li>
     * </ol>
     *
     * @param id        ID upravované sezóny
     * @param seasonDTO nové hodnoty sezóny
     *
     * @throws SeasonNotFoundException      pokud sezóna s daným ID neexistuje
     * @throws InvalidSeasonPeriodDateException pokud jsou neplatná data od/do
     * @throws SeasonPeriodOverlapException pokud se sezóna překrývá s jinou
     * @throws InvalidSeasonStateException  pokud se pokoušíš deaktivovat jedinou aktivní sezónu
     */
    @Override
    @Transactional
    public SeasonDTO updateSeason(Long id, SeasonDTO seasonDTO) {
        // 1) najít existující sezónu
        SeasonEntity existing = seasonRepository.findById(id)
                .orElseThrow(() -> new SeasonNotFoundException(id));

        // 2) validace dat s ignorováním této sezóny (aby se nepočítala jako překryv sama se sebou)
        validateDates(seasonDTO, id);

        boolean wasActive = existing.isActive();
        boolean willBeActive = seasonDTO.isActive();

        // *** STRIKTNÍ REŽIM: nelze deaktivovat jedinou aktivní sezónu
        if (wasActive && !willBeActive) {
            long activeCount = seasonRepository.countByActiveTrue();
            if (activeCount <= 1) {
                throw new InvalidSeasonStateException(
                        "BE - Nelze deaktivovat jedinou aktivní sezónu. " +
                        "Nejprve nastavte jinou sezónu jako aktivní."
                );
            }
        }

        // 3) promítnout změny z DTO do entity a uložit
        mapper.updateEntityFromDTO(seasonDTO, existing);
        SeasonEntity saved = seasonRepository.save(existing);

        // pokud se sezóna z neaktivní stala aktivní → uděláme z ní jedinou aktivní
        if (!wasActive && saved.isActive()) {
            setOnlyActiveSeason(saved.getId());
        }

        return mapper.toDTO(saved);
    }

    // ======================
    // AKTIVNÍ SEZÓNA
    // ======================

    /**
     * Vrátí aktuálně aktivní sezónu ({@link SeasonEntity}).
     *
     * @return aktivní sezóna
     * @throws SeasonNotFoundException pokud není nastavena žádná aktivní sezóna
     */
    @Override
    public SeasonEntity getActiveSeason() {
        return seasonRepository.findByActiveTrue()
                .orElseThrow(() -> new SeasonNotFoundException(
                        "BE - Není nastavena žádná aktivní sezóna."
                ));
    }

    @Override
    public SeasonDTO getActiveSeasonOrNull() {
        return seasonRepository.findByActiveTrue()
                .map(mapper::toDTO)
                .orElse(null);
    }

    @Override
    public SeasonDTO getSeasonById(Long id) {
        SeasonEntity entity = seasonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Season not found: " + id));
        return mapper.toDTO(entity);
    }

    // ======================
    // SEZNAM VŠECH SEZÓN
    // ======================

    /**
     * Vrátí všechny sezóny seřazené podle začátku ({@code startDate ASC}),
     * namapované na {@link SeasonDTO}.
     * <p>
     * Typické použití:
     * <ul>
     *     <li>administrace sezón,</li>
     *     <li>přehled v UI (tabulka sezón).</li>
     * </ul>
     */
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

    /**
     * Nastaví konkrétní sezónu jako aktivní.
     * <p>
     * Kroky:
     * <ol>
     *     <li>ověří, že sezóna s daným ID existuje,</li>
     *     <li>pomocí {@link #setOnlyActiveSeason(Long)} ji nastaví jako jedinou aktivní
     *         (všechny ostatní sezóny deaktivuje).</li>
     * </ol>
     *
     * @param seasonId ID sezóny, která má být aktivní
     *
     * @throws SeasonNotFoundException pokud sezóna s daným ID neexistuje
     */
    @Override
    @Transactional
    public void setActiveSeason(Long seasonId) {
        // 1) ověř, že existuje
        SeasonEntity toActivate = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new SeasonNotFoundException(seasonId
                ));

        // 2) nastavíme ji jako jedinou aktivní
        setOnlyActiveSeason(toActivate.getId());
    }



    // ======================
    // PRIVÁTNÍ VALIDACE DAT
    // ======================

    /**
     * Validuje datumy sezóny a kontroluje překryvy s ostatními sezónami.
     *
     * @param seasonDTO       DTO s daty sezóny
     * @param currentSeasonId ID aktuální sezóny (u UPDATE), nebo {@code null} u CREATE
     *
     * Kontroluje:
     * <ul>
     *     <li>{@code startDate} != null a {@code endDate} != null,</li>
     *     <li>{@code startDate} &lt; {@code endDate},</li>
     *     <li>žádná jiná sezóna se nepřekrývá s intervalem &lt;=end, &gt;=start
     *         (při UPDATE ignoruje sezónu s id = currentSeasonId).</li>
     * </ul>
     *
     * @throws InvalidSeasonPeriodDateException pokud jsou neplatná data
     * @throws SeasonPeriodOverlapException     pokud dochází k překryvu s jinou sezónou
     */
    private void validateDates(SeasonDTO seasonDTO, Long currentSeasonId) {
        LocalDate start = seasonDTO.getStartDate();
        LocalDate end = seasonDTO.getEndDate();

        // základní null-check
        if (start == null || end == null) {
            throw new InvalidSeasonPeriodDateException("BE - Datum od a do nesmí být null.");
        }

        // logická kontrola pořadí
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

    // ======================
    // PRIVÁTNÍ POMOCNÁ METODA
    // ======================

    /**
     * Nastaví danou sezónu jako JEDINOU aktivní.
     * <p>
     * Implementace:
     * <ul>
     *     <li>načte všechny sezóny z DB,</li>
     *     <li>té s {@code id == activeSeasonId} nastaví {@code active = true},</li>
     *     <li>všem ostatním nastaví {@code active = false},</li>
     *     <li>uloží změny pomocí {@link SeasonRepository#saveAll(Iterable)}.</li>
     * </ul>
     *
     * Tím garantujeme invariant:
     * <ul>
     *     <li>v aplikaci existuje v každém okamžiku právě jedna aktivní sezóna.</li>
     * </ul>
     */
    private void setOnlyActiveSeason(Long activeSeasonId) {
        List<SeasonEntity> all = seasonRepository.findAll();
        for (SeasonEntity season : all) {
            season.setActive(season.getId().equals(activeSeasonId));
        }
        seasonRepository.saveAll(all);
    }
}
