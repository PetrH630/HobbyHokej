package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.player.dto.PlayerInactivityPeriodDTO;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.player.entities.PlayerInactivityPeriodEntity;
import cz.phsoft.hokej.player.exceptions.InactivityPeriodNotFoundException;
import cz.phsoft.hokej.player.exceptions.InactivityPeriodOverlapException;
import cz.phsoft.hokej.player.exceptions.InvalidInactivityPeriodDateException;
import cz.phsoft.hokej.player.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.player.mappers.PlayerInactivityPeriodMapper;
import cz.phsoft.hokej.player.repositories.PlayerInactivityPeriodRepository;
import cz.phsoft.hokej.player.repositories.PlayerRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementace servisní vrstvy pro správu období neaktivity hráčů.
 *
 * Obchodní význam:
 * - určuje, zda je hráč v daném čase aktivní nebo neaktivní,
 * - poskytuje podklady pro rozhodování, zda může být hráč
 *   zařazen do zápasu nebo mít přístup k určitým funkcím.
 *
 * Typické použití:
 * - při filtrování zápasů dostupných pro hráče,
 * - v přístupové logice k detailu zápasu,
 * - při registraci hráče na zápas.
 *
 * Klíčové pravidlo:
 * - hráč nesmí mít překrývající se období neaktivity;
 *   překryv je považován za chybu dat.
 *
 * Třída:
 * - řeší pouze doménová pravidla pro neaktivitu hráče,
 * - neřeší bezpečnost, role ani notifikace,
 * - používá PlayerInactivityPeriodMapper pro převod mezi entitami a DTO,
 * - spolupracuje s PlayerInactivityPeriodRepository a PlayerRepository
 *   pro přístup k perzistentní vrstvě.
 */
@Service
public class PlayerInactivityPeriodServiceImpl implements PlayerInactivityPeriodService {

    private final PlayerInactivityPeriodRepository inactivityRepository;
    private final PlayerRepository playerRepository;
    private final PlayerInactivityPeriodMapper mapper;

    /**
     * Vytvoří instanci služby pro správu období neaktivity hráčů.
     *
     * Závislosti jsou injektovány konstruktorově. Třída poté slouží
     * jako centrální místo pro práci s obdobími neaktivity v doméně hráče.
     *
     * @param inactivityRepository repository pro práci s obdobími neaktivity
     * @param playerRepository repository pro přístup k hráčům
     * @param mapper mapper pro převod mezi entitou a DTO
     */
    public PlayerInactivityPeriodServiceImpl(
            PlayerInactivityPeriodRepository inactivityRepository,
            PlayerRepository playerRepository,
            PlayerInactivityPeriodMapper mapper
    ) {
        this.inactivityRepository = inactivityRepository;
        this.playerRepository = playerRepository;
        this.mapper = mapper;
    }


    // READ OPERACE


    /**
     * Vrátí všechna období neaktivity všech hráčů.
     *
     * Metoda se používá zejména v administrátorských přehledech
     * nebo interních reportech, které pracují s kompletním seznamem.
     *
     * @return seznam všech období neaktivity ve formě DTO
     */
    @Override
    public List<PlayerInactivityPeriodDTO> getAll() {
        return inactivityRepository.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    /**
     * Vrátí konkrétní období neaktivity podle identifikátoru.
     *
     * Pokud záznam neexistuje, je vyhozena výjimka
     * InactivityPeriodNotFoundException.
     *
     * @param id identifikátor období neaktivity
     * @return DTO reprezentace období neaktivity
     * @throws InactivityPeriodNotFoundException pokud záznam neexistuje
     */
    @Override
    public PlayerInactivityPeriodDTO getById(Long id) {
        PlayerInactivityPeriodEntity entity = inactivityRepository.findById(id)
                .orElseThrow(() -> new InactivityPeriodNotFoundException(id));

        return mapper.toDTO(entity);
    }

    /**
     * Vrátí všechna období neaktivity konkrétního hráče.
     *
     * Období jsou seřazena podle začátku neaktivity od nejstaršího.
     * Před načtením období se ověřuje, zda daný hráč existuje.
     *
     * @param playerId identifikátor hráče
     * @return seznam období neaktivity ve formě DTO
     * @throws PlayerNotFoundException pokud hráč neexistuje
     */
    @Override
    public List<PlayerInactivityPeriodDTO> getByPlayer(Long playerId) {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));

        return inactivityRepository.findByPlayerOrderByInactiveFromAsc(player).stream()
                .map(mapper::toDTO)
                .toList();
    }


    // CREATE


    /**
     * Vytvoří nové období neaktivity hráče.
     *
     * Před uložením se ověřuje:
     * - existence hráče,
     * - platnost dat (od/do),
     * - nepřekrývání s jinými obdobími neaktivity daného hráče.
     *
     * Při zjištěném překryvu je vyhozena výjimka
     * InactivityPeriodOverlapException.
     *
     * @param dto data nového období neaktivity
     * @return nově vytvořené období neaktivity ve formě DTO
     * @throws PlayerNotFoundException              pokud hráč neexistuje
     * @throws InvalidInactivityPeriodDateException pokud je rozsah dat neplatný
     * @throws InactivityPeriodOverlapException     pokud se nové období překrývá s existujícím
     */
    @Override
    @Transactional
    public PlayerInactivityPeriodDTO create(PlayerInactivityPeriodDTO dto) {

        PlayerEntity player = playerRepository.findById(dto.getPlayerId())
                .orElseThrow(() -> new PlayerNotFoundException(dto.getPlayerId()));

        validateDates(dto);

        boolean overlaps = !inactivityRepository
                .findByPlayerAndInactiveToGreaterThanEqualAndInactiveFromLessThanEqual(
                        player,
                        dto.getInactiveFrom(),
                        dto.getInactiveTo()
                ).isEmpty();

        if (overlaps) {
            throw new InactivityPeriodOverlapException();
        }

        PlayerInactivityPeriodEntity entity = mapper.toEntity(dto, player);
        PlayerInactivityPeriodEntity saved = inactivityRepository.save(entity);

        return mapper.toDTO(saved);
    }


    // UPDATE


    /**
     * Aktualizuje existující období neaktivity.
     *
     * Při aktualizaci se ověřuje platnost dat a kontroluje se překryv
     * s ostatními obdobími neaktivity daného hráče. Aktuální záznam
     * se při kontrole překryvu ignoruje, aby nebyl považován za kolizi
     * sám se sebou.
     *
     * @param id  identifikátor upravovaného období
     * @param dto nové hodnoty období neaktivity
     * @return aktualizované období neaktivity ve formě DTO
     * @throws InactivityPeriodNotFoundException     pokud záznam neexistuje
     * @throws InvalidInactivityPeriodDateException  pokud je rozsah dat neplatný
     * @throws InactivityPeriodOverlapException      pokud se upravené období překrývá s jiným
     */
    @Override
    @Transactional
    public PlayerInactivityPeriodDTO update(Long id, PlayerInactivityPeriodDTO dto) {

        PlayerInactivityPeriodEntity entity = inactivityRepository.findById(id)
                .orElseThrow(() -> new InactivityPeriodNotFoundException(id));

        validateDates(dto);

        boolean overlaps = inactivityRepository
                .findByPlayerAndInactiveToGreaterThanEqualAndInactiveFromLessThanEqual(
                        entity.getPlayer(),
                        dto.getInactiveFrom(),
                        dto.getInactiveTo()
                ).stream()
                .anyMatch(p -> !p.getId().equals(id));

        if (overlaps) {
            throw new InactivityPeriodOverlapException(
                    "BE - Upravené období se překrývá s jiným obdobím neaktivity hráče."
            );
        }

        entity.setInactiveFrom(dto.getInactiveFrom());
        entity.setInactiveTo(dto.getInactiveTo());

        PlayerInactivityPeriodEntity saved = inactivityRepository.save(entity);

        return mapper.toDTO(saved);
    }


    // DELETE


    /**
     * Smaže období neaktivity podle identifikátoru.
     *
     * Pokud záznam neexistuje, je vyhozena výjimka
     * InactivityPeriodNotFoundException. Po úspěšném smazání
     * není vrácena žádná hodnota.
     *
     * @param id identifikátor období neaktivity
     * @throws InactivityPeriodNotFoundException pokud záznam neexistuje
     */
    @Override
    @Transactional
    public void delete(Long id) {
        PlayerInactivityPeriodEntity entity = inactivityRepository.findById(id)
                .orElseThrow(() -> new InactivityPeriodNotFoundException(id));

        inactivityRepository.delete(entity);
    }


    // AKTIVITA HRÁČE


    /**
     * Ověří, zda je hráč v daném čase aktivní.
     *
     * Hráč je považován za neaktivního, pokud existuje období neaktivity,
     * které daný časový okamžik zahrnuje. Metoda vrací negaci této podmínky,
     * tedy true pouze v případě, že hráč není v daném čase v žádném období
     * neaktivity.
     *
     * Typické použití:
     * - v MatchServiceImpl při filtrování dostupných zápasů,
     * - v přístupové logice k detailu zápasu,
     * - při posuzování, zda má hráč nárok na účast v zápase.
     *
     * @param player hráč, jehož aktivita se ověřuje
     * @param dateTime časový okamžik, pro který se aktivita kontroluje
     * @return true, pokud hráč není v daném čase v žádném období neaktivity, jinak false
     */
    @Override
    public boolean isActive(PlayerEntity player, LocalDateTime dateTime) {
        return !inactivityRepository
                .existsByPlayerAndInactiveFromLessThanEqualAndInactiveToGreaterThanEqual(
                        player,
                        dateTime,
                        dateTime
                );
    }


    // PRIVÁTNÍ VALIDACE


    /**
     * Validuje časový rozsah období neaktivity.
     *
     * Kontroluje se:
     * - že datum od i datum do není null,
     * - že datum od je skutečně před datem do.
     *
     * Při neplatném rozsahu je vyhozena výjimka
     * InvalidInactivityPeriodDateException s popisnou zprávou.
     *
     * @param dto DTO s daty období neaktivity
     * @throws InvalidInactivityPeriodDateException při neplatném rozsahu dat
     */
    private void validateDates(PlayerInactivityPeriodDTO dto) {
        if (dto.getInactiveFrom() == null || dto.getInactiveTo() == null) {
            throw new InvalidInactivityPeriodDateException(
                    "BE - Datum od a do nesmí být null."
            );
        }

        if (!dto.getInactiveFrom().isBefore(dto.getInactiveTo())) {
            throw new InvalidInactivityPeriodDateException(
                    "BE - Datum 'od' musí být před 'do'."
            );
        }
    }
}