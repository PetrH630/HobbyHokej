import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.entities.PlayerInactivityPeriodEntity;
import cz.phsoft.hokej.data.repositories.PlayerInactivityPeriodRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.models.dto.mappers.PlayerInactivityPeriodMapper;
import cz.phsoft.hokej.models.dto.PlayerInactivityPeriodDTO;
import cz.phsoft.hokej.models.services.PlayerInactivityPeriodService;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class PlayerInactivityPeriodServiceImpl implements PlayerInactivityPeriodService {

    private final PlayerInactivityPeriodRepository inactivityRepository;
    private final PlayerRepository playerRepository;
    private final PlayerInactivityPeriodMapper mapper;

    public PlayerInactivityPeriodServiceImpl(PlayerInactivityPeriodRepository inactivityRepository,
                                             PlayerRepository playerRepository,
                                             PlayerInactivityPeriodMapper mapper) {
        this.inactivityRepository = inactivityRepository;
        this.playerRepository = playerRepository;
        this.mapper = mapper;
    }

    @Override
    public List<PlayerInactivityPeriodDTO> getAll() {
        return inactivityRepository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PlayerInactivityPeriodDTO getById(Long id) {
        PlayerInactivityPeriodEntity entity = inactivityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Období neaktivity s ID " + id + " neexistuje."
                ));
        return mapper.toDTO(entity);
    }

    @Override
    public List<PlayerInactivityPeriodDTO> getByPlayer(Long playerId) {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        return inactivityRepository.findByPlayerOrderByInactiveFromAsc(player)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public PlayerInactivityPeriodDTO create(PlayerInactivityPeriodDTO dto) {
        PlayerEntity player = playerRepository.findById(dto.getPlayerId())
                .orElseThrow(() -> new IllegalArgumentException("Hráč s ID " + dto.getPlayerId() + " neexistuje."));

        if (dto.getInactiveFrom() == null || dto.getInactiveTo() == null) {
            throw new IllegalArgumentException("Datum od a do nesmí být null.");
        }
        if (!dto.getInactiveFrom().isBefore(dto.getInactiveTo())) {
            throw new IllegalArgumentException("inactiveFrom musí být před inactiveTo.");
        }

        // kontrola překryvu
        boolean overlaps = !inactivityRepository
                .findByPlayerAndInactiveToGreaterThanEqualAndInactiveFromLessThanEqual(
                        player, dto.getInactiveFrom(), dto.getInactiveTo()
                ).isEmpty();

        if (overlaps) {
            throw new IllegalStateException("Nové období se překrývá s existujícím obdobím neaktivity hráče.");
        }

        PlayerInactivityPeriodEntity entity = mapper.toEntity(dto, player);
        return mapper.toDTO(inactivityRepository.save(entity));
    }

    @Override
    public PlayerInactivityPeriodDTO update(Long id, PlayerInactivityPeriodDTO dto) {
        PlayerInactivityPeriodEntity entity = inactivityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Období neaktivity s ID " + id + " neexistuje."));

        if (dto.getInactiveFrom() == null || dto.getInactiveTo() == null) {
            throw new IllegalArgumentException("Datum od a do nesmí být null.");
        }
        if (!dto.getInactiveFrom().isBefore(dto.getInactiveTo())) {
            throw new IllegalArgumentException("inactiveFrom musí být před inactiveTo.");
        }

        // kontrola překryvu, ignoruje aktuální záznam
        boolean overlaps = inactivityRepository
                .findByPlayerAndInactiveToGreaterThanEqualAndInactiveFromLessThanEqual(
                        entity.getPlayer(), dto.getInactiveFrom(), dto.getInactiveTo()
                ).stream()
                .anyMatch(p -> !p.getId().equals(id));

        if (overlaps) {
            throw new IllegalStateException("Upravené období se překrývá s jiným obdobím neaktivity hráče.");
        }

        mapper.updateEntityFromDto(dto, entity);
        return mapper.toDTO(inactivityRepository.save(entity));
    }

    @Override
    public void delete(Long id) {
        PlayerInactivityPeriodEntity entity = inactivityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Období neaktivity s ID " + id + " neexistuje."));
        inactivityRepository.delete(entity);
    }

    // true = aktivní, false = neaktivní
    public boolean isActive(PlayerEntity player, LocalDateTime dateTime) {
        return !inactivityRepository.existsByPlayerAndInactiveFromLessThanEqualAndInactiveToGreaterThanEqual(
                player, dateTime, dateTime);
    }
}
