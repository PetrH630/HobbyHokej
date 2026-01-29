package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.entities.PlayerInactivityPeriodEntity;
import cz.phsoft.hokej.data.repositories.PlayerInactivityPeriodRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.InactivityPeriodNotFoundException;
import cz.phsoft.hokej.exceptions.InactivityPeriodOverlapException;
import cz.phsoft.hokej.exceptions.InvalidInactivityPeriodDateException;
import cz.phsoft.hokej.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.models.dto.PlayerInactivityPeriodDTO;
import cz.phsoft.hokej.models.mappers.PlayerInactivityPeriodMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Služba pro správu období neaktivity hráčů.
 *
 * OBCHODNÍ VÝZNAM:
 * <ul>
 *     <li>určuje, zda je hráč v daném čase aktivní/neaktivní,</li>
 *     <li>využívá se při:
 *         <ul>
 *             <li>zobrazování dostupných zápasů,</li>
 *             <li>kontrole přístupu k detailu zápasu,</li>
 *             <li>registracích na zápasy.</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * KLÍČOVÉ PRAVIDLO:
 * <ul>
 *     <li>hráč NESMÍ mít překrývající se období neaktivity.</li>
 * </ul>
 *
 * Tato služba je čistě doménová:
 * <ul>
 *     <li>neřeší bezpečnost ani role,</li>
 *     <li>neposílá notifikace,</li>
 *     <li>řeší pouze data a pravidla kolem neaktivity.</li>
 * </ul>
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
     * Použití:
     * <ul>
     *     <li>administrace,</li>
     *     <li>interní přehledy.</li>
     * </ul>
     */
    @Override
    public List<PlayerInactivityPeriodDTO> getAll() {
        return inactivityRepository.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    /**
     * Vrátí konkrétní období neaktivity dle ID.
     *
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
     * seřazená podle začátku neaktivity (od nejstaršího).
     *
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
     * Validace:
     * <ul>
     *     <li>hráč musí existovat,</li>
     *     <li>datum {@code inactiveFrom} &lt; {@code inactiveTo},</li>
     *     <li>nové období se NESMÍ překrývat s jiným obdobím neaktivity hráče.</li>
     * </ul>
     *
     * @throws PlayerNotFoundException               pokud hráč neexistuje
     * @throws InvalidInactivityPeriodDateException  pokud jsou špatná data od/do
     * @throws InactivityPeriodOverlapException      pokud se období překrývá s existujícím
     */
    @Override
    @Transactional
    public PlayerInactivityPeriodDTO create(PlayerInactivityPeriodDTO dto) {

        PlayerEntity player = playerRepository.findById(dto.getPlayerId())
                .orElseThrow(() -> new PlayerNotFoundException(dto.getPlayerId()));

        validateDates(dto);

        // kontrola překryvu existujících období:
        // pokud existuje JAKÝKOLI záznam, který se časově protíná, je to chyba
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
     * <p>
     * Oproti {@link #create(PlayerInactivityPeriodDTO)}:
     * <ul>
     *     <li>při kontrole překryvu ignoruje sám sebe (aktuální záznam).</li>
     * </ul>
     *
     * @throws InactivityPeriodNotFoundException     pokud záznam neexistuje
     * @throws InvalidInactivityPeriodDateException  pokud jsou špatná data od/do
     * @throws InactivityPeriodOverlapException      pokud se upravené období překrývá s jiným
     */
    @Override
    @Transactional
    public PlayerInactivityPeriodDTO update(Long id, PlayerInactivityPeriodDTO dto) {

        PlayerInactivityPeriodEntity entity = inactivityRepository.findById(id)
                .orElseThrow(() -> new InactivityPeriodNotFoundException(id));

        validateDates(dto);

        // kontrola překryvu – ignorujeme aktuální záznam (id)
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

        mapper.updateEntityFromDto(dto, entity);
        PlayerInactivityPeriodEntity saved = inactivityRepository.save(entity);

        return mapper.toDTO(saved);
    }

    // ======================
    // DELETE
    // ======================

    /**
     * Smaže období neaktivity dle ID.
     *
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
     * Zjistí, zda je hráč v daném čase AKTIVNÍ.
     *
     * @param player   hráč
     * @param dateTime čas, pro který ověřujeme aktivitu
     *
     * @return {@code true}  pokud hráč NENÍ v žádném období neaktivity<br>
     *         {@code false} pokud je v daném čase v období neaktivity
     *
     * Použití:
     * <ul>
     *     <li>v {@link MatchServiceImpl} (filtrace zápasů podle dostupnosti hráče),</li>
     *     <li>v přístupové logice (zda má hráč „nárok“ na daný zápas).</li>
     * </ul>
     */
    @Override
    public boolean isActive(PlayerEntity player, LocalDateTime dateTime) {
        // pokud existuje záznam, který obaluje daný čas → hráč je NEaktivní
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
     * Validace časového rozsahu neaktivity.
     * <ul>
     *     <li>datum od/do nesmí být {@code null},</li>
     *     <li>{@code inactiveFrom} musí být přísně před {@code inactiveTo}.</li>
     * </ul>
     *
     * @throws InvalidInactivityPeriodDateException při neplatných datech
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
