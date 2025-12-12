package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.enums.PlayerType;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.MatchNotFoundException;
import cz.phsoft.hokej.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.models.dto.*;
import cz.phsoft.hokej.models.dto.mappers.MatchMapper;
import cz.phsoft.hokej.models.dto.mappers.PlayerMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final PlayerMapper playerMapper;
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_MANAGER = "ROLE_MANAGER";

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
        return matchRepository.findAll().stream()
                .map(matchMapper::toDTO)
                .toList();
    }

    @Override
    public List<MatchDTO> getUpcomingMatches() {
        return matchRepository.findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now())
                .stream()
                .map(matchMapper::toDTO)
                .toList();
    }

    public List<MatchDTO> getPastMatches() {
        return matchRepository.findByDateTimeBeforeOrderByDateTimeDesc(LocalDateTime.now())
                .stream()
                .map(matchMapper::toDTO)
                .toList();
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
        return matchMapper.toDTO(findMatchOrThrow(id));
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

        // Přepočet registrací pokud došlo ke změně maxPlayers
        if (saved.getMaxPlayers() != oldMaxPlayers) {
            registrationService.recalcStatusesForMatch(saved.getId());
        }

        return matchMapper.toDTO(saved);
    }

    @Override
    public SuccessResponseDTO deleteMatch(Long id) {
        MatchEntity match = findMatchOrThrow(id);

        // 2) Pokud existuje, smažeme ho
        matchRepository.delete(match);

        return new SuccessResponseDTO(
                "Zápas " + match.getId() + match.getDateTime() + " byl úspěšně smazán",
                id,
                LocalDateTime.now().toString()
        );
    }

    @Override
    public MatchDetailDTO getMatchDetail(Long id) {
        MatchEntity match = findMatchOrThrow(id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdminOrManager = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(ROLE_ADMIN) || a.getAuthority().equals(ROLE_MANAGER));

        // --- oddělena logika přístupu hráče do privátní metody ---
        checkAccessForPlayer(match, auth);

        // --- sběr statistik hráčů přes privátní metodu ---
        return collectPlayerStatus(match, isAdminOrManager);
    }

    // --- privátní metoda pro kontrolu přístupu hráče ---
    private void checkAccessForPlayer(MatchEntity match, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return;

        boolean isAdminOrManager = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(ROLE_ADMIN) || a.getAuthority().equals(ROLE_MANAGER));

        if (isAdminOrManager) return;

        Object principal = auth.getPrincipal();
        if (!(principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails)) return;

        List<PlayerEntity> ownedPlayers = playerRepository.findAll().stream()
                .filter(p -> p.getUser() != null && p.getUser().getEmail().equals(userDetails.getUsername()))
                .toList();

        boolean hasRestrictedPlayer = ownedPlayers.stream()
                .anyMatch(p -> {
                    List<MatchRegistrationDTO> registrations = registrationService.getRegistrationsForMatch(match.getId());

                    boolean noResponse = registrations.stream()
                            .noneMatch(r -> r.getPlayerId().equals(p.getId()));

                    boolean inactiveForMatch = !playerInactivityPeriodService.isActive(p, match.getDateTime());

                    return noResponse || inactiveForMatch;
                });

        if (hasRestrictedPlayer) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Nemáte přístup k detailu tohoto zápasu."
            );
        }
    }

    // --- privátní metoda pro sběr statistik hráčů ---
    private MatchDetailDTO collectPlayerStatus(MatchEntity match, boolean isAdminOrManager) {
        List<MatchRegistrationDTO> registrations = registrationService.getRegistrationsForMatch(match.getId());

        // Převod všech registrací na Map<PlayerMatchStatus, List<PlayerDTO>>
        var statusToPlayersMap = registrations.stream()
                .map(r -> playerRepository.findById(r.getPlayerId())
                        .map(playerMapper::toDTO)
                        .map(dto -> new java.util.AbstractMap.SimpleEntry<>(r.getStatus(), dto))
                )
                .filter(java.util.Optional::isPresent) // odstraníme chybějící hráče
                .map(java.util.Optional::get)
                .collect(Collectors.groupingBy(
                        java.util.Map.Entry::getKey,
                        Collectors.mapping(java.util.Map.Entry::getValue, Collectors.toList())
                ));

        List<PlayerEntity> allPlayers = playerRepository.findAll();
        Set<Long> respondedIds = registrations.stream()
                .map(MatchRegistrationDTO::getPlayerId)
                .collect(Collectors.toSet());

        List<PlayerDTO> noResponsePlayers = allPlayers.stream()
                .filter(p -> !respondedIds.contains(p.getId()))
                .map(playerMapper::toDTO)
                .toList();

        // Počty hráčů podle statusu
        int inGamePlayers = statusToPlayersMap.getOrDefault(PlayerMatchStatus.REGISTERED, List.of()).size();
        int outGamePlayers = statusToPlayersMap.getOrDefault(PlayerMatchStatus.UNREGISTERED, List.of()).size()
                + statusToPlayersMap.getOrDefault(PlayerMatchStatus.EXCUSED, List.of()).size();
        int waitingPlayers = statusToPlayersMap.getOrDefault(PlayerMatchStatus.RESERVED, List.of()).size();
        int noActionPlayers = noResponsePlayers.size();
        int remainingSlots = match.getMaxPlayers() - inGamePlayers;
        double pricePerRegistered = inGamePlayers > 0 ? match.getPrice() / (double) inGamePlayers : 0;

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

        // Nastavení hráčů podle statusu z mapy
        dto.setRegisteredPlayers(statusToPlayersMap.getOrDefault(PlayerMatchStatus.REGISTERED, List.of()));
        dto.setReservedPlayers(statusToPlayersMap.getOrDefault(PlayerMatchStatus.RESERVED, List.of()));
        dto.setUnregisteredPlayers(statusToPlayersMap.getOrDefault(PlayerMatchStatus.UNREGISTERED, List.of()));
        dto.setExcusedPlayers(statusToPlayersMap.getOrDefault(PlayerMatchStatus.EXCUSED, List.of()));

        // pouze admin/manager uvidí no-response hráče
        dto.setNoResponsePlayers(isAdminOrManager ? noResponsePlayers : null);

        return dto;
    }


    @Override
    public List<MatchDTO> getAvailableMatchesForPlayer(Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);

        // Filtrace přes stream bez mezivýsledků
        return matchRepository.findAll().stream()
                .filter(match -> playerInactivityPeriodService.isActive(player, match.getDateTime()))
                .map(matchMapper::toDTO)
                .toList();
    }

    @Override
    public List<MatchDTO> getUpcomingMatchesForPlayer(Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);
        PlayerType type = player.getType();

        // 1) Nejbližší nadcházející zápasy podle data
        List<MatchEntity> upcomingAll = matchRepository.findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now());

        List<MatchEntity> limited = switch (type) {
            case VIP -> upcomingAll;
            case STANDARD -> upcomingAll.stream().limit(2).toList();
            case BASIC -> upcomingAll.isEmpty() ? List.of() : List.of(upcomingAll.get(0));
        };

        // 2) Filtrování podle aktivity hráče
        return limited.stream()
                .filter(match -> playerInactivityPeriodService.isActive(player, match.getDateTime()))
                .map(matchMapper::toDTO)
                .toList();
    }

    private PlayerEntity findPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    private MatchEntity findMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }
}
