package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.SeasonEntity;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.data.repositories.SeasonRepository;
import cz.phsoft.hokej.exceptions.*;
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
 * Implementace service vrstvy používaná pro správu sezón.
 *
 * Třída zajišťuje vytváření a úpravy sezón včetně validace datumového rozsahu, kontroly překryvů a kontroly duplicity názvu.
 * Současně se zajišťuje správa aktivní sezóny tak, aby byl v systému v jeden okamžik aktivní právě jeden záznam.
 *
 * Načítání a ukládání dat se deleguje do {@link SeasonRepository} a převod mezi entitou a DTO se deleguje do {@link SeasonMapper}.
 * Identifikátor autora vytvoření sezóny se odvozuje z bezpečnostního kontextu a dohledává se přes {@link AppUserRepository}.
 *
 * Třída neřeší autorizaci endpointů, protože kontrola oprávnění se předpokládá v controller vrstvě.
 * Zároveň se zde neodesílají notifikace a řeší se pouze konzistence doménového stavu sezón.
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
     * Vytváří novou sezónu.
     *
     * Před uložením se provádí validace datumového rozmezí, kontrola překryvu s existujícími sezónami a kontrola duplicity názvu.
     * Následně se DTO převede na entitu a uloží se přes repository vrstvu. Do entity se doplní identifikátor uživatele,
     * který sezónu vytvořil, pokud je možné jej odvodit z autentizace.
     *
     * Po uložení se vynucuje invariant aktivní sezóny. Pokud je nová sezóna označena jako aktivní, nastaví se jako jediná aktivní.
     * Pokud aktivní sezóna v systému neexistuje, nastaví se vytvořená sezóna jako aktivní automaticky.
     *
     * @param seasonDTO Vstupní data sezóny.
     * @return Vytvořená sezóna ve formě {@link SeasonDTO}.
     * @throws InvalidSeasonPeriodDateException Pokud jsou neplatná data od a do.
     * @throws SeasonPeriodOverlapException Pokud se sezóna překrývá s existující sezónou.
     * @throws DuplicateSeasonNameException Pokud již existuje sezóna se stejným názvem.
     */
    @Override
    @Transactional
    public SeasonDTO createSeason(SeasonDTO seasonDTO) {
        validateDates(seasonDTO, null);
        checkSeasonName(seasonDTO, null);

        SeasonEntity entity = mapper.toEntity(seasonDTO);

        // *** NOVÉ *** – nastavíme ID uživatele, který sezónu vytvořil
        Long currentUserId = getCurrentUserIdOrNull();
        entity.setCreatedByUserId(currentUserId);

        SeasonEntity saved = seasonRepository.save(entity);

        long activeCount = seasonRepository.countByActiveTrue();

        if (seasonDTO.isActive()) {
            setOnlyActiveSeason(saved.getId());
        } else if (activeCount == 0) {
            setOnlyActiveSeason(saved.getId());
        }

        return mapper.toDTO(saved);
    }

    // UPDATE

    /**
     * Aktualizuje existující sezónu.
     *
     * Nejprve se ověří existence sezóny podle identifikátoru. Poté se zvaliduje datumové rozmezí a překryvy s ostatními sezónami
     * s tím, že upravovaná sezóna se při kontrole překryvů ignoruje. Současně se kontroluje duplicita názvu sezóny.
     *
     * Při pokusu o deaktivaci aktivní sezóny se ověřuje, že v systému zůstane alespoň jedna aktivní sezóna.
     * Následně se změny z DTO promítnou do entity pomocí mapper vrstvy a sezóna se uloží přes repository vrstvu.
     *
     * Pokud se sezóna stane nově aktivní, nastaví se jako jediná aktivní sezóna a ostatní sezóny se deaktivují.
     *
     * @param id Identifikátor upravované sezóny.
     * @param seasonDTO Nové hodnoty sezóny.
     * @return Aktualizovaná sezóna ve formě {@link SeasonDTO}.
     * @throws SeasonNotFoundException Pokud sezóna s daným ID neexistuje.
     * @throws InvalidSeasonPeriodDateException Pokud jsou neplatná data od a do.
     * @throws SeasonPeriodOverlapException Pokud se sezóna překrývá s jinou sezónou.
     * @throws InvalidSeasonStateException Pokud se pokouší deaktivovat jediná aktivní sezóna.
     * @throws DuplicateSeasonNameException Pokud již existuje sezóna se stejným názvem.
     */
    @Override
    @Transactional
    public SeasonDTO updateSeason(Long id, SeasonDTO seasonDTO) {
        SeasonEntity existing = seasonRepository.findById(id)
                .orElseThrow(() -> new SeasonNotFoundException(id));

        validateDates(seasonDTO, id);
        checkSeasonName(seasonDTO, id);

        boolean wasActive = existing.isActive();
        boolean willBeActive = seasonDTO.isActive();

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
     * Vrací aktuálně aktivní sezónu jako entitu.
     *
     * Metoda se používá v service vrstvě jako zdroj pravdy pro aktuální sezónu, typicky pro filtrování zápasů nebo dalších dat.
     * Načtení se deleguje do {@link SeasonRepository}. Pokud aktivní sezóna není nastavena, vyhazuje se {@link SeasonNotFoundException}.
     *
     * @return Aktuálně aktivní sezóna jako {@link SeasonEntity}.
     * @throws SeasonNotFoundException Pokud není nastavena žádná aktivní sezóna.
     */
    @Override
    public SeasonEntity getActiveSeason() {
        return seasonRepository.findByActiveTrue()
                .orElseThrow(() -> new SeasonNotFoundException(
                        "BE - Není nastavena žádná aktivní sezóna."
                ));
    }

    /**
     * Vrací aktuálně aktivní sezónu ve formě DTO nebo null.
     *
     * Metoda se používá v místech, kde je absence aktivní sezóny akceptovatelná a nemá být považována za chybový stav.
     * Načtení se deleguje do {@link SeasonRepository} a mapování se deleguje do {@link SeasonMapper}.
     *
     * @return Aktivní sezóna ve formě {@link SeasonDTO}, nebo null pokud aktivní sezóna neexistuje.
     */
    @Override
    public SeasonDTO getActiveSeasonOrNull() {
        return seasonRepository.findByActiveTrue()
                .map(mapper::toDTO)
                .orElse(null);
    }

    /**
     * Vrací sezónu podle identifikátoru ve formě DTO.
     *
     * Sezóna se načítá z repository vrstvy. Pokud sezóna neexistuje, vyhazuje se {@link SeasonNotFoundException}.
     * Mapování entity na DTO se deleguje do {@link SeasonMapper}.
     *
     * @param id Identifikátor sezóny.
     * @return Sezóna ve formě {@link SeasonDTO}.
     * @throws SeasonNotFoundException Pokud sezóna s daným ID neexistuje.
     */
    @Override
    public SeasonDTO getSeasonById(Long id) {
        SeasonEntity entity = seasonRepository.findById(id)
                .orElseThrow(() -> new SeasonNotFoundException(id));
        return mapper.toDTO(entity);
    }

    // SEZNAM VŠECH SEZÓN

    /**
     * Vrací všechny sezóny seřazené podle začátku vzestupně.
     *
     * Načtení se deleguje do {@link SeasonRepository} a mapování na DTO se deleguje do {@link SeasonMapper}.
     * Metoda se používá pro administrativní přehledy a pro zobrazení seznamu sezón v uživatelském rozhraní.
     *
     * @return Seznam všech sezón ve formě {@link SeasonDTO}.
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
     * Nastavuje konkrétní sezónu jako aktivní.
     *
     * Nejprve se ověřuje existence sezóny podle identifikátoru. Následně se provede nastavení sezóny jako jediné aktivní
     * pomocí interní metody {@link #setOnlyActiveSeason(Long)}, která současně deaktivuje všechny ostatní sezóny.
     *
     * @param seasonId Identifikátor sezóny, která má být nastavena jako aktivní.
     * @throws SeasonNotFoundException Pokud sezóna s daným ID neexistuje.
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
     * Validuje datumové rozmezí sezóny a kontroluje překryv s ostatními sezónami.
     *
     * Kontroluje se, že datum začátku a konce není null, že datum začátku předchází datu konce a že rozsah sezóny
     * není v překryvu s jinou sezónou. Při aktualizaci se z kontroly překryvu vynechává sezóna s identifikátorem
     * currentSeasonId.
     *
     * @param seasonDTO DTO s daty sezóny.
     * @param currentSeasonId Identifikátor aktuální sezóny při aktualizaci, nebo null při vytváření.
     * @throws InvalidSeasonPeriodDateException Pokud jsou data neplatná.
     * @throws SeasonPeriodOverlapException Pokud se sezóna překrývá s jinou sezónou.
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

    /**
     * Validuje název sezóny kontrolou duplicity vůči ostatním sezónám.
     *
     * Při vytváření se ověřuje existence názvu vůči všem sezónám. Při aktualizaci se kontrola provádí vůči všem sezónám
     * s tím, že sezóna s identifikátorem currentSeasonId se z kontroly vynechává.
     *
     * @param seasonDTO DTO s daty sezóny.
     * @param currentSeasonId Identifikátor aktuální sezóny při aktualizaci, nebo null při vytváření.
     * @throws DuplicateSeasonNameException Pokud již existuje sezóna se stejným názvem.
     */
    private void checkSeasonName(SeasonDTO seasonDTO, Long currentSeasonId) {
        String seasonName = seasonDTO.getName().trim();

        boolean existSeasonName;
        if (currentSeasonId == null) {
            existSeasonName = seasonRepository.existsByName(seasonName);
        } else {
            existSeasonName = seasonRepository.existsByNameAndIdNot(seasonName, currentSeasonId);
        }

        if (existSeasonName) {
            throw new DuplicateSeasonNameException(seasonName);
        }
    }

    // PRIVÁTNÍ POMOCNÁ METODA

    /**
     * Nastavuje zadanou sezónu jako jedinou aktivní.
     *
     * Všechny sezóny se načtou z repository vrstvy a následně se u sezóny se zadaným identifikátorem nastaví active na true,
     * zatímco u všech ostatních sezón se active nastaví na false. Změny se uloží hromadně přes {@link SeasonRepository}.
     * Tím se vynucuje invariant, že v systému existuje v jeden okamžik právě jedna aktivní sezóna.
     *
     * @param activeSeasonId Identifikátor sezóny, která má být nastavena jako jediná aktivní.
     */
    private void setOnlyActiveSeason(Long activeSeasonId) {
        List<SeasonEntity> all = seasonRepository.findAll();
        for (SeasonEntity season : all) {
            season.setActive(season.getId().equals(activeSeasonId));
        }
        seasonRepository.saveAll(all);
    }

    /**
     * Vrací identifikátor aktuálně přihlášeného uživatele nebo null, pokud není možné uživatele určit.
     *
     * Identifikátor se odvozuje z autentizace v bezpečnostním kontextu. Jako klíč se používá e-mail uživatele
     * a dohledání se provádí přes {@link AppUserRepository}. Pokud autentizace neexistuje, uživatel není přihlášen
     * nebo záznam uživatele nelze dohledat, vrací se null.
     *
     * @return Identifikátor aktuálního uživatele, nebo null pokud jej nelze určit.
     */
    private Long getCurrentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        String email = auth.getName();
        return appUserRepository.findByEmail(email)
                .map(AppUserEntity::getId)
                .orElse(null);
    }
}
