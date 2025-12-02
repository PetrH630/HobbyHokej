package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.models.dto.MatchDTO;
import cz.phsoft.hokej.models.dto.MatchDetailDTO;
import cz.phsoft.hokej.models.dto.mappers.MatchMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final MatchMapper matchMapper;
    private final MatchRegistrationService registrationService;
    private final PlayerRepository playerRepository;   // ← DOPLNĚNO !!!

    public MatchServiceImpl(MatchRepository matchRepository,
                            MatchMapper matchMapper,
                            MatchRegistrationService registrationService,
                            PlayerRepository playerRepository) { // ← DOPLNĚNO !!!
        this.matchRepository = matchRepository;
        this.matchMapper = matchMapper;
        this.registrationService = registrationService;
        this.playerRepository = playerRepository;       // ← DOPLNĚNO !!!
    }

    @Override
    public List<MatchDTO> getAllMatches() {
        return matchRepository.findAll()
                .stream()
                .map(matchMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchDTO> getUpcomingMatches() {
        return matchRepository.findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now())
                .stream()
                .map(matchMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<MatchDTO> getPastMatches() {
        return matchRepository.findByDateTimeBeforeOrderByDateTimeDesc(LocalDateTime.now())
                .stream()
                .map(matchMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MatchDTO getNextMatch() {
        return matchRepository.findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now())
                .stream()
                .findFirst()
                .map(matchMapper::toDTO)
                .orElse(null);
    }

    @Override
    public MatchDTO getMatchById(Long id) {
        return matchMapper.toDTO(matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found: " + id)));
    }

    @Override
    public MatchDTO createMatch(MatchDTO dto) {
        MatchEntity entity = matchMapper.toEntity(dto);
        return matchMapper.toDTO(matchRepository.save(entity));
    }

    @Override
    public MatchDTO updateMatch(Long id, MatchDTO dto) {
        MatchEntity entity = matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Match not found: " + id));

        int oldMaxPlayers = entity.getMaxPlayers();
        matchMapper.updateEntity(dto, entity);
        MatchEntity saved = matchRepository.save(entity);

        if (saved.getMaxPlayers() != oldMaxPlayers) {
            registrationService.recalcStatusesForMatch(saved.getId());
        }

        return matchMapper.toDTO(saved);
    }

    @Override
    public void deleteMatch(Long id) {
        matchRepository.deleteById(id);
    }

    // -------------------------------------------------------------------------
    // --------------------------- MATCH DETAIL --------------------------------
    // -------------------------------------------------------------------------

    @Override
    public MatchDetailDTO getMatchDetail(Long id) {

        MatchEntity match = matchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zápas nenalezen"));

        List<MatchRegistrationEntity> registrations = registrationService.getRegistrationsForMatch(id);

        // --- 1) ROZDĚLENÍ PODLE STATUSŮ ---
        List<MatchRegistrationEntity> registered = registrations.stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .toList();

        List<MatchRegistrationEntity> reserved = registrations.stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.RESERVED)
                .toList();

        List<MatchRegistrationEntity> unregistered = registrations.stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.UNREGISTERED)
                .toList();

        List<MatchRegistrationEntity> excused = registrations.stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.EXCUSED)
                .toList();

        // --- 2) NO-RESPONSE HRÁČI PŘÍMO ZDE ---
        List<PlayerEntity> allPlayers = playerRepository.findAll();

        Set<Long> respondedIds = registrations.stream()
                .map(r -> r.getPlayer().getId())
                .collect(Collectors.toSet());

        List<PlayerEntity> noResponsePlayers = allPlayers.stream()
                .filter(p -> !respondedIds.contains(p.getId()))
                .toList();

        // --- 3) POČTY ---
        int inGamePlayers = registered.size();
        int outGamePlayers = unregistered.size() + excused.size();
        int waitingPlayers = reserved.size();
        int noActionPlayers = noResponsePlayers.size();

        int remainingSlots = match.getMaxPlayers() - inGamePlayers;

        double pricePerRegistered = inGamePlayers > 0
                ? match.getPrice() / (double) inGamePlayers
                : 0;

        // --- 4) DTO ---
        MatchDetailDTO dto = new MatchDetailDTO();
        dto.setId(match.getId());
        dto.setDateTime(match.getDateTime());
        dto.setMaxPlayers(match.getMaxPlayers());

        dto.setInGamePlayers(inGamePlayers);
        dto.setOutGamePlayers(outGamePlayers);
        dto.setWaitingPlayers(waitingPlayers);
        dto.setNoActionPlayers(noActionPlayers);

        dto.setPricePerRegisteredPlayer(pricePerRegistered);
        dto.setRemainingSlots(remainingSlots);

        dto.setRegisteredPlayers(
                registered.stream()
                        .map(r -> r.getPlayer().getName() + " " + r.getPlayer().getSurname())
                        .toList()
        );

        dto.setReservedPlayers(
                reserved.stream()
                        .map(r -> r.getPlayer().getName() + " " + r.getPlayer().getSurname())
                        .toList()
        );

        dto.setUnregisteredPlayers(
                unregistered.stream()
                        .map(r -> r.getPlayer().getName() + " " + r.getPlayer().getSurname())
                        .toList()
        );

        dto.setExcusedPlayers(
                excused.stream()
                        .map(r -> r.getPlayer().getName() + " " + r.getPlayer().getSurname())
                        .toList()
        );

        dto.setNoResponsePlayers(
                noResponsePlayers.stream()
                        .map(p -> p.getName() + " " + p.getSurname())
                        .toList()
        );

        return dto;
    }

}
