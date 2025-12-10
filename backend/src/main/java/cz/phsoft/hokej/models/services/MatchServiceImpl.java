package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.enums.PlayerType;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.models.dto.MatchDTO;
import cz.phsoft.hokej.models.dto.MatchDetailDTO;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.mappers.MatchMapper;
import cz.phsoft.hokej.models.dto.mappers.PlayerMapper;
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
    private final PlayerRepository playerRepository;
    private final PlayerInactivityPeriodService playerInactivityPeriodService;
    private final PlayerMapper playerMapper; // nově injektován

    public MatchServiceImpl(MatchRepository matchRepository,
                            MatchMapper matchMapper,
                            MatchRegistrationService registrationService,
                            PlayerRepository playerRepository,
                            PlayerInactivityPeriodService playerInactivityPeriodService,
                            PlayerMapper playerMapper) {
        this.matchRepository = matchRepository;
        this.matchMapper = matchMapper;
        this.registrationService = registrationService;
        this.playerRepository = playerRepository;
        this.playerInactivityPeriodService = playerInactivityPeriodService;
        this.playerMapper = playerMapper;
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
        MatchEntity match = findMatchOrThrow(id);
        return matchMapper.toDTO(match);
    }

    @Override
    public MatchDTO createMatch(MatchDTO dto) {
        MatchEntity entity = matchMapper.toEntity(dto);
        return matchMapper.toDTO(matchRepository.save(entity));
    }

    @Override
    public MatchDTO updateMatch(Long id, MatchDTO dto) {
        MatchEntity match = findMatchOrThrow(id);

        int oldMaxPlayers = match.getMaxPlayers();
        matchMapper.updateEntity(dto, match);
        MatchEntity saved = matchRepository.save(match);

        if (saved.getMaxPlayers() != oldMaxPlayers) {
            registrationService.recalcStatusesForMatch(saved.getId());
        }

        return matchMapper.toDTO(saved);
    }

    @Override
    public void deleteMatch(Long id) {
        matchRepository.deleteById(id);
    }

    @Override
    public MatchDetailDTO getMatchDetail(Long id) {

        MatchEntity match = findMatchOrThrow(id);

        // Registrace jako DTO (service vrací DTOy)
        List<MatchRegistrationDTO> registrations = registrationService.getRegistrationsForMatch(id);

        List<MatchRegistrationDTO> registered = registrations.stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .toList();

        List<MatchRegistrationDTO> reserved = registrations.stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.RESERVED)
                .toList();

        List<MatchRegistrationDTO> unregistered = registrations.stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.UNREGISTERED)
                .toList();

        List<MatchRegistrationDTO> excused = registrations.stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.EXCUSED)
                .toList();

        // --- 2) NO-RESPONSE HRÁČI PŘÍMO ZDE ---
        List<PlayerEntity> allPlayers = playerRepository.findAll();

        Set<Long> respondedIds = registrations.stream()
                .map(MatchRegistrationDTO::getPlayerId)
                .collect(Collectors.toSet());

        List<PlayerEntity> noResponsePlayers = allPlayers.stream()
                .filter(p -> !respondedIds.contains(p.getId()))
                .toList();

        // počty hráčů - stats
        int inGamePlayers = registered.size();
        int outGamePlayers = unregistered.size() + excused.size();
        int waitingPlayers = reserved.size();
        int noActionPlayers = noResponsePlayers.size();

        int remainingSlots = match.getMaxPlayers() - inGamePlayers;
        double pricePerRegistered = inGamePlayers > 0
                ? match.getPrice() / (double) inGamePlayers
                : 0;

        // naplnění DTO — nyní používáme PlayerDTO (mapované přes playerMapper)
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

        // pomocná lambda: z MatchRegistrationDTO -> PlayerDTO (pokud hráč existuje)
        List<PlayerDTO> registeredPlayers = registered.stream()
                .map(r -> playerRepository.findById(r.getPlayerId()))
                .filter(java.util.Optional::isPresent)
                .map(opt -> playerMapper.toDTO(opt.get()))
                .toList();

        List<PlayerDTO> reservedPlayers = reserved.stream()
                .map(r -> playerRepository.findById(r.getPlayerId()))
                .filter(java.util.Optional::isPresent)
                .map(opt -> playerMapper.toDTO(opt.get()))
                .toList();

        List<PlayerDTO> unregisteredPlayers = unregistered.stream()
                .map(r -> playerRepository.findById(r.getPlayerId()))
                .filter(java.util.Optional::isPresent)
                .map(opt -> playerMapper.toDTO(opt.get()))
                .toList();

        List<PlayerDTO> excusedPlayers = excused.stream()
                .map(r -> playerRepository.findById(r.getPlayerId()))
                .filter(java.util.Optional::isPresent)
                .map(opt -> playerMapper.toDTO(opt.get()))
                .toList();

        List<PlayerDTO> noResponsePlayerDTOs = noResponsePlayers.stream()
                .map(playerMapper::toDTO)
                .toList();

        dto.setRegisteredPlayers(registeredPlayers);
        dto.setReservedPlayers(reservedPlayers);
        dto.setUnregisteredPlayers(unregisteredPlayers);
        dto.setExcusedPlayers(excusedPlayers);
        dto.setNoResponsePlayers(noResponsePlayerDTOs);

        return dto;
    }

    public List<MatchEntity> getAvailableMatchesForPlayer(Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);

        List<MatchEntity> allMatches = matchRepository.findAll();

        return allMatches.stream()
                .filter(match -> playerInactivityPeriodService.isActive(player, match.getDateTime()))
                .collect(Collectors.toList());
    }

    public List<MatchEntity> getUpcomingMatchesForPlayer(Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);

        PlayerType type = player.getType();

        LocalDateTime now = LocalDateTime.now();

        List<MatchEntity> upcoming = matchRepository.findByDateTimeAfterOrderByDateTimeAsc(now);

        List<MatchEntity> activeMatches = upcoming.stream()
                .filter(match -> playerInactivityPeriodService.isActive(player, match.getDateTime()))
                .toList();

        return switch (type) {
            case VIP -> activeMatches;
            case STANDARD -> activeMatches.stream().limit(2).toList();
            case BASIC -> activeMatches.isEmpty() ? List.of() : List.of(activeMatches.get(0));
        };
    }

    private PlayerEntity findPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found: " + playerId));
    }

    private MatchEntity findMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found: " + matchId));
    }
}
