package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.SeasonEntity;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.data.repositories.SeasonRepository;
import cz.phsoft.hokej.exceptions.InvalidSeasonPeriodDateException;
import cz.phsoft.hokej.exceptions.InvalidSeasonStateException;
import cz.phsoft.hokej.exceptions.SeasonNotFoundException;
import cz.phsoft.hokej.exceptions.SeasonPeriodOverlapException;
import cz.phsoft.hokej.models.dto.SeasonDTO;
import cz.phsoft.hokej.models.mappers.SeasonMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

/**
 * Service vrstva pro správu sezón ({@link SeasonEntity}).
 *
 * Odpovědnosti:
 * - vytváření a úprava sezón včetně validace datumů a překryvů,
 * - správa aktivní sezóny (právě jedna aktivní sezóna v systému),
 * - poskytování seznamu sezón pro administrativní přehledy,
 * - poskytování aktivní sezóny pro další služby (např. {@code MatchServiceImpl}).
 *
 * Invarianty:
 * - datum začátku sezóny musí předcházet datu konce,
 * - dvě sezóny se nesmí časově překrývat,
 * - v jednom okamžiku smí být aktivní právě jedna sezóna,
 * - systém by neměl zůstat bez aktivní sezóny (řeší se při vytvoření a změně sezóny).
 *
 * Třída je čistě doménová:
 * - neřeší autentizaci ani autorizaci (tyto kontroly se provádějí v controlleru),
 * - neodesílá notifikace,
 * - garantuje konzistentní stav kalendáře sezón pro zbytek aplikace.
 */
@Service
public class SeasonServiceImpl implements SeasonService {

    private final SeasonRepository seasonRepository;
    private final SeasonMapper mapper;
    private final AppUserRepository appUserRepository;

    public SeasonServiceImpl(SeasonRepository seasonRepository,
                             SeasonMapper mapper,
                             AppUserRepository appUserRepository) {
        this.seasonRepository = seasonRepository;
        this.mapper = mapper;
        this.appUserRepository = appUserRepository;
    }
    // CREATE

    /**
     * Vytvoří novou sezónu.
     *
     * Postup:
     * - provede validaci datumového rozmezí a překryvů s ostatními sezónami,
     * - uloží novou sezónu do databáze,
     * - zajistí, aby v systému vždy existovala aktivní sezóna:
     *   - pokud je nová sezóna označena jako aktivní, stane se jedinou aktivní,
     *   - pokud je to první sezóna v systému a není aktivní, nastaví se jako aktivní automaticky.
     *
     * @param seasonDTO vstupní data sezóny
     * @return vytvořená sezóna ve formě {@link SeasonDTO}
     *
     * @throws InvalidSeasonPeriodDateException pokud jsou neplatná data od/do
     * @throws SeasonPeriodOverlapException     pokud se sezóna překrývá s existující sezónou
     */
    @Override
    @Transactional
    public SeasonDTO createSeason(SeasonDTO seasonDTO) {
        // u create neexistuje ID → validace ignoruje ID a kontroluje překryv se všemi sezónami
        validateDates(seasonDTO, null);

        SeasonEntity entity = mapper.toEntity(seasonDTO);
        // *** NOVÉ *** – nastavíme ID uživatele, který sezónu vytvořil
        Long currentUserId = getCurrentUserIdOrNull();
        entity.setCreatedByUserId(currentUserId);

        SeasonEntity saved = seasonRepository.save(entity);

        long activeCount = seasonRepository.countByActiveTrue();

        if (seasonDTO.isActive()) {
            // nová sezóna má být aktivní → nastaví se jako jediná aktivní
            setOnlyActiveSeason(saved.getId());
        } else if (activeCount == 0) {
            // v systému není žádná aktivní sezóna → tato sezóna se nastaví jako aktivní
            setOnlyActiveSeason(saved.getId());
        }

        return mapper.toDTO(saved);
    }

    // UPDATE

    /**
     * Aktualizuje existující sezónu.
     *
     * Postup:
     * - ověří se existence sezóny podle ID,
     * - zvaliduje se datumové rozmezí s ignorováním této sezóny
     *   při kontrole překryvů,
     * - zkontroluje se, že nelze deaktivovat poslední aktivní sezónu,
     * - promítnou se změny z DTO do entity a sezóna se uloží,
     * - pokud se sezóna stala nově aktivní, nastaví se jako jediná aktivní.
     *
     * @param id        ID upravované sezóny
     * @param seasonDTO nové hodnoty sezóny
     *
     * @return aktualizovaná sezóna ve formě {@link SeasonDTO}
     *
     * @throws SeasonNotFoundException            pokud sezóna s daným ID neexistuje
     * @throws InvalidSeasonPeriodDateException   pokud jsou neplatná data od/do
     * @throws SeasonPeriodOverlapException       pokud se sezóna překrývá s jinou
     * @throws InvalidSeasonStateException        pokud se pokouší deaktivovat jediná aktivní sezóna
     */
    @Override
    @Transactional
    public SeasonDTO updateSeason(Long id, SeasonDTO seasonDTO) {
        SeasonEntity existing = seasonRepository.findById(id)
                .orElseThrow(() -> new SeasonNotFoundException(id));

        validateDates(seasonDTO, id);

        boolean wasActive = existing.isActive();
        boolean willBeActive = seasonDTO.isActive();

        // striktní režim: nelze deaktivovat poslední aktivní sezónu
        if (wasActive && !willBeActive) {
            long activeCount = seasonRepository.countByActiveTrue();
            if (activeCount <= 1) {
                throw new InvalidSeasonStateException(
                        "BE - Nelze deaktivovat jedinou aktivní sezónu. " +
                                "Nejprve nastav jinou sezónu jako aktivní."
                );
            }
        }

        mapper.updateEntityFromDTO(seasonDTO, existing);
        SeasonEntity saved = seasonRepository.save(existing);

        if (!wasActive && saved.isActive()) {
            setOnlyActiveSeason(saved.getId());
        }

        return mapper.toDTO(saved);
    }

    // AKTIVNÍ SEZÓNA
    /**
     * Vrátí aktuálně aktivní sezónu.
     *
     * Metoda se používá v business vrstvě (například v {@code MatchServiceImpl})
     * pro filtrování zápasů podle sezóny.
     *
     * @return aktivní sezóna jako entita {@link SeasonEntity}
     * @throws SeasonNotFoundException pokud není nastavena žádná aktivní sezóna
     */
    @Override
    public SeasonEntity getActiveSeason() {
        return seasonRepository.findByActiveTrue()
                .orElseThrow(() -> new SeasonNotFoundException(
                        "BE - Není nastavena žádná aktivní sezóna."
                ));
    }

    /**
     * Vrátí aktivní sezónu ve formě {@link SeasonDTO} nebo null.
     *
     * Metoda se používá tam, kde je absence aktivní sezóny
     * akceptovatelná a nemá být považována za výjimku.
     *
     * @return aktivní sezóna nebo null
     */
    @Override
    public SeasonDTO getActiveSeasonOrNull() {
        return seasonRepository.findByActiveTrue()
                .map(mapper::toDTO)
                .orElse(null);
    }

    /**
     * Vrátí sezónu podle ID.
     *
     * @param id ID sezóny
     * @return sezóna ve formě {@link SeasonDTO}
     * @throws RuntimeException pokud sezóna neexistuje
     *                          (lze později nahradit za SeasonNotFoundException)
     */
    @Override
    public SeasonDTO getSeasonById(Long id) {
        SeasonEntity entity = seasonRepository.findById(id)
                .orElseThrow(() -> new SeasonNotFoundException(id));
        return mapper.toDTO(entity);
    }

    // SEZNAM VŠECH SEZÓN
    /**
     * Vrátí všechny sezóny seřazené podle začátku stoupajícím způsobem.
     *
     * Typické použití:
     * - administrace sezón,
     * - přehled sezón v UI.
     *
     * @return seznam všech sezón ve formě {@link SeasonDTO}
     */
    @Override
    public List<SeasonDTO> getAllSeasons() {
        return seasonRepository.findAllByOrderByStartDateAsc()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }
    // NASTAVENÍ AKTIVNÍ SEZÓNY

    /**
     * Nastaví konkrétní sezónu jako aktivní.
     *
     * Postup:
     * - ověří se existence sezóny podle ID,
     * - pomocí metody {@link #setOnlyActiveSeason(Long)} se sezóna nastaví
     *   jako jediná aktivní a ostatní sezóny se deaktivují.
     *
     * @param seasonId ID sezóny, která má být aktivní
     * @throws SeasonNotFoundException pokud sezóna s daným ID neexistuje
     */
    @Override
    @Transactional
    public void setActiveSeason(Long seasonId) {
        SeasonEntity toActivate = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new SeasonNotFoundException(seasonId));

        setOnlyActiveSeason(toActivate.getId());
    }

    // PRIVÁTNÍ VALIDACE DAT

    /**
     * Validuje datumy sezóny a kontroluje překryvy s ostatními sezónami.
     *
     * Kontroluje:
     * - startDate a endDate nesmí být null,
     * - startDate musí být před endDate,
     * - existující sezóny se nesmí překrývat s rozsahem nové sezóny.
     *
     * Při CREATE:
     * - kontroluje se překryv se všemi existujícími sezónami.
     *
     * Při UPDATE:
     * - kontroluje se překryv se všemi ostatními sezónami
     *   (sezóna s ID currentSeasonId se ignoruje).
     *
     * @param seasonDTO       DTO s daty sezóny
     * @param currentSeasonId ID aktuální sezóny (u UPDATE), nebo null u CREATE
     *
     * @throws InvalidSeasonPeriodDateException pokud jsou neplatná data
     * @throws SeasonPeriodOverlapException     pokud se sezóna překrývá s jinou
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
            overlaps = seasonRepository
                    .existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(end, start);
        } else {
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

    // PRIVÁTNÍ POMOCNÁ METODA

    /**
     * Nastaví zadanou sezónu jako jedinou aktivní.
     *
     * Implementace:
     * - načte všechny sezóny z databáze,
     * - sezóně s odpovídajícím ID nastaví příznak active na true,
     * - všem ostatním sezónám nastaví active na false,
     * - uloží změny hromadně pomocí {@link SeasonRepository#saveAll(Iterable)}.
     *
     * Tím se zajišťuje invariant, že v aplikaci existuje v každém okamžiku
     * právě jedna aktivní sezóna.
     *
     * @param activeSeasonId ID sezóny, která má být jedinou aktivní sezónou
     */
    private void setOnlyActiveSeason(Long activeSeasonId) {
        List<SeasonEntity> all = seasonRepository.findAll();
        for (SeasonEntity season : all) {
            season.setActive(season.getId().equals(activeSeasonId));
        }
        seasonRepository.saveAll(all);
    }

    /**
     * Získá ID aktuálně přihlášeného uživatele nebo null,
     * pokud se nepodaří uživatele určit (například při testech).
     */
    private Long getCurrentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        String email = auth.getName(); // username = email
        return appUserRepository.findByEmail(email)
                .map(AppUserEntity::getId)
                .orElse(null);
    }
}

