package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.entities.PlayerInactivityPeriodEntity;
import cz.phsoft.hokej.data.repositories.PlayerInactivityPeriodRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.models.dto.PlayerInactivityPeriodDTO;
import cz.phsoft.hokej.models.dto.mappers.PlayerInactivityPeriodMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

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
    public List<PlayerInactivityPeriodEntity> getAll() {
        return inactivityRepository.findAll();
    }

    @Override
    public PlayerInactivityPeriodEntity getById(Long id) {
        return inactivityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Období neaktivity s ID " + id + " neexistuje."
                ));
    }

    @Override
    public List<PlayerInactivityPeriodEntity> getByPlayer(Long playerId) {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        return inactivityRepository.findByPlayerOrderByInactiveFromAsc(player);
    }


    @Override
    public PlayerInactivityPeriodEntity create(PlayerInactivityPeriodDTO dto) {
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
        return inactivityRepository.save(entity);
    }

    @Override
    public PlayerInactivityPeriodEntity update(Long id, PlayerInactivityPeriodDTO dto) {
        PlayerInactivityPeriodEntity entity = getById(id);

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

        // update entity z DTO
        mapper.updateEntityFromDto(dto, entity);
        return inactivityRepository.save(entity);
    }

    @Override
    public void delete(Long id) {
        PlayerInactivityPeriodEntity entity = getById(id);
        inactivityRepository.delete(entity);
    }


    // true = aktivní, false = neaktivní
    public boolean isActive(PlayerEntity player, LocalDateTime dateTime) {
        return !inactivityRepository.existsByPlayerAndInactiveFromLessThanEqualAndInactiveToGreaterThanEqual(
                player, dateTime, dateTime);
    }

}