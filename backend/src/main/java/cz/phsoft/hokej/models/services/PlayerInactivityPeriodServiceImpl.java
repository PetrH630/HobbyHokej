package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.entities.PlayerInactivityPeriodEntity;
import cz.phsoft.hokej.data.repositories.PlayerInactivityPeriodRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.InactivityPeriodNotFoundException;
import cz.phsoft.hokej.exceptions.InactivityPeriodOverlapException;
import cz.phsoft.hokej.exceptions.InvalidInactivityPeriodDateException;
import cz.phsoft.hokej.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.models.dto.mappers.PlayerInactivityPeriodMapper;
import cz.phsoft.hokej.models.dto.PlayerInactivityPeriodDTO;
import java.util.List;
import jakarta.transaction.Transactional;
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
        return inactivityRepository.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public PlayerInactivityPeriodDTO getById(Long id) {
        PlayerInactivityPeriodEntity entity = inactivityRepository.findById(id)
                .orElseThrow(() -> new InactivityPeriodNotFoundException(id));

        return mapper.toDTO(entity);
    }

    @Override
    public List<PlayerInactivityPeriodDTO> getByPlayer(Long playerId) {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));

        return inactivityRepository.findByPlayerOrderByInactiveFromAsc(player)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    // --- TRANSACTIONAL pro zápis dat ---
    @Override
    @Transactional
    public PlayerInactivityPeriodDTO create(PlayerInactivityPeriodDTO dto) {
        PlayerEntity player = playerRepository.findById(dto.getPlayerId())
                .orElseThrow(() -> new PlayerNotFoundException(dto.getPlayerId()));

        validateDates(dto);

        // kontrola překryvu existujících období
        boolean overlaps = !inactivityRepository
                .findByPlayerAndInactiveToGreaterThanEqualAndInactiveFromLessThanEqual(
                        player, dto.getInactiveFrom(), dto.getInactiveTo()
                ).isEmpty();

        if (overlaps) {
            throw new InactivityPeriodOverlapException();
        }

        PlayerInactivityPeriodEntity entity = mapper.toEntity(dto, player);
        return mapper.toDTO(inactivityRepository.save(entity));
    }

    @Override
    @Transactional
    public PlayerInactivityPeriodDTO update(Long id, PlayerInactivityPeriodDTO dto) {
        PlayerInactivityPeriodEntity entity = inactivityRepository.findById(id)
                .orElseThrow(() -> new InactivityPeriodNotFoundException(id));

        validateDates(dto);

        // kontrola překryvu, ignoruje aktuální záznam
        boolean overlaps = inactivityRepository
                .findByPlayerAndInactiveToGreaterThanEqualAndInactiveFromLessThanEqual(
                        entity.getPlayer(), dto.getInactiveFrom(), dto.getInactiveTo()
                ).stream()
                .anyMatch(p -> !p.getId().equals(id));

        if (overlaps) {
            throw new InactivityPeriodOverlapException("BE - Upravené období se překrývá s jiným obdobím neaktivity hráče.");
        }

        mapper.updateEntityFromDto(dto, entity);
        return mapper.toDTO(inactivityRepository.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PlayerInactivityPeriodEntity entity = inactivityRepository.findById(id)
                .orElseThrow(() -> new InactivityPeriodNotFoundException(id));
        inactivityRepository.delete(entity);
    }

    // --- true = aktivní, false = neaktivní ---
    public boolean isActive(PlayerEntity player, LocalDateTime dateTime) {
        return !inactivityRepository.existsByPlayerAndInactiveFromLessThanEqualAndInactiveToGreaterThanEqual(
                player, dateTime, dateTime);
    }

    // --- privátní metoda pro validaci dat ---
    private void validateDates(PlayerInactivityPeriodDTO dto) {
        if (dto.getInactiveFrom() == null || dto.getInactiveTo() == null) {
            throw new InvalidInactivityPeriodDateException("BE - Datum od a do nesmí být null.");
        }
        if (!dto.getInactiveFrom().isBefore(dto.getInactiveTo())) {
            throw new InvalidInactivityPeriodDateException("BE - Datum 'od' musí být před 'do'.");
        }
    }
}
