package cz.phsoft.hokej.player.services;

import cz.phsoft.hokej.match.services.MatchServiceImpl;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.player.entities.PlayerInactivityPeriodEntity;
import cz.phsoft.hokej.player.repositories.PlayerInactivityPeriodRepository;
import cz.phsoft.hokej.player.repositories.PlayerRepository;
import cz.phsoft.hokej.player.exceptions.InactivityPeriodNotFoundException;
import cz.phsoft.hokej.player.exceptions.InactivityPeriodOverlapException;
import cz.phsoft.hokej.player.exceptions.InvalidInactivityPeriodDateException;
import cz.phsoft.hokej.player.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.player.dto.PlayerInactivityPeriodDTO;
import cz.phsoft.hokej.player.mappers.PlayerInactivityPeriodMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service implementace pro správu období neaktivity hráčů.
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
 * - hráč nesmí mít překrývající se období neaktivity
 *   (překryv je považován za chybu dat).
 *
 * Tato service:
 * - řeší pouze doménová pravidla pro neaktivitu,
 * - neřeší bezpečnost, role ani notifikace,
 * - využívá mapper {@link PlayerInactivityPeriodMapper}
 *   pro převod mezi entitami a DTO.
 */
@Service
public class PlayerInactivityPeriodServiceImpl implements PlayerInactivityPeriodService {

    private final PlayerInactivityPeriodRepository inactivityRepository;
    private final PlayerRepository playerRepository;
    private final PlayerInactivityPeriodMapper mapper;

    public PlayerInactivityPeriodServiceImpl(
            PlayerInactivityPeriodRepository inactivityRepository,
            PlayerRepository playerRepository,
            PlayerInactivityPeriodMapper mapper
    ) {
        this.inactivityRepository = inactivityRepository;
        this.playerRepository = playerRepository;
        this.mapper = mapper;
    }

    // ======================
    // READ OPERACE
    // ======================

    /**
     * Vrátí všechna období neaktivity všech hráčů.
     *
     * Používá se zejména v administrátorských přehledech
     * nebo interních reportech.
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
     * Vrátí konkrétní období neaktivity podle ID.
     *
     * @param id ID období neaktivity
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
     * Vrátí všechna období neaktivity konkrétního hráče,
     * seřazená podle začátku neaktivity od nejstaršího.
     *
     * @param playerId ID hráče
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

    // ======================
    // CREATE
    // ======================

    /**
     * Vytvoří nové období neaktivity hráče.
     *
     * Před uložením se ověřuje:
     * - existence hráče,
     * - platnost dat (od/do),
     * - nepřekrývání s jinými obdobími neaktivity daného hráče.
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

        // kontrola překryvu existujících období pro daného hráče
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

    // ======================
    // UPDATE
    // ======================

    /**
     * Aktualizuje existující období neaktivity.
     *
     * Oproti vytvoření nového období se při kontrole překryvu
     * ignoruje aktuální záznam (aby nebyl považován za kolizi sám se sebou).
     *
     * @param id  ID upravovaného období
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

        // kontrola překryvu – existující záznamy kromě aktuálního ID
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

    // ======================
    // DELETE
    // ======================

    /**
     * Smaže období neaktivity podle ID.
     *
     * @param id ID období neaktivity
     * @throws InactivityPeriodNotFoundException pokud záznam neexistuje
     */
    @Override
    @Transactional
    public void delete(Long id) {
        PlayerInactivityPeriodEntity entity = inactivityRepository.findById(id)
                .orElseThrow(() -> new InactivityPeriodNotFoundException(id));

        inactivityRepository.delete(entity);
    }

    // ======================
    // AKTIVITA HRÁČE
    // ======================

    /**
     * Ověří, zda je hráč v daném čase aktivní.
     *
     * Hráč je považován za neaktivního, pokud existuje období neaktivity,
     * které daný časový okamžik zahrnuje. Metoda vrací negaci této podmínky.
     *
     * Typické použití:
     * - v {@link MatchServiceImpl} při filtrování dostupných zápasů,
     * - v přístupové logice k detailu zápasu,
     * - při posuzování, zda má hráč „nárok“ na účast v zápase.
     *
     * @param player   hráč, jehož aktivita se ověřuje
     * @param dateTime časový okamžik, pro který se aktivita kontroluje
     * @return true, pokud hráč není v daném čase v žádném období neaktivity, jinak false
     */
    @Override
    public boolean isActive(PlayerEntity player, LocalDateTime dateTime) {
        // pokud existuje záznam pokrývající daný čas → hráč je neaktivní
        return !inactivityRepository
                .existsByPlayerAndInactiveFromLessThanEqualAndInactiveToGreaterThanEqual(
                        player,
                        dateTime,
                        dateTime
                );
    }

    // ======================
    // PRIVÁTNÍ VALIDACE
    // ======================

    /**
     * Validuje časový rozsah období neaktivity.
     *
     * Kontroluje se:
     * - že datum od i do není null,
     * - že datum od je před datem do.
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
