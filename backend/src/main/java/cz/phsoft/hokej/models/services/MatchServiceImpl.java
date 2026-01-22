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
import java.util.Optional;
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
    // metoda pro získání všech zápasů
    @Override
    public List<MatchDTO> getAllMatches() {
        return matchRepository.findAll().stream()
                .map(matchMapper::toDTO)
                .toList();
    }
    // metoda pro získání všech nadcházejících zápasů
    @Override
    public List<MatchDTO> getUpcomingMatches() {
        return matchRepository.findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now())
                .stream()
                .map(matchMapper::toDTO)
                .toList();
    }
    // metoda pro získání uplynulých zápasů
    public List<MatchDTO> getPastMatches() {
        return matchRepository.findByDateTimeBeforeOrderByDateTimeDesc(LocalDateTime.now())
                .stream()
                .map(matchMapper::toDTO)
                .toList();
    }
    // metoda pro získání prvního nadcházejícího zápasu
    @Override
    public MatchDTO getNextMatch() {
        return matchRepository.findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now())
                .stream()
                .findFirst()
                .map(matchMapper::toDTO)
                .orElse(null);
    }
    // metoda pro zápas dle ID
    @Override
    public MatchDTO getMatchById(Long id) {
        return matchMapper.toDTO(findMatchOrThrow(id));
    }

    // metoda pro vytvoření zápasu
    @Override
    public MatchDTO createMatch(MatchDTO dto) {
        MatchEntity entity = matchMapper.toEntity(dto);
        return matchMapper.toDTO(matchRepository.save(entity));
    }

    // metoda pro úpravu zápasu
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

    // metoda pro odstranění zápasu
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

    // metoda pro detail zápasu - omezen výpis pro ADMIN, MANAGER, PLAYER
    @Override
    public MatchDetailDTO getMatchDetail(Long id) {
        MatchEntity match = findMatchOrThrow(id);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdminOrManager = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(ROLE_ADMIN) || a.getAuthority().equals(ROLE_MANAGER));

        // oddělena logika přístupu hráče do privátní metody
        checkAccessForPlayer(match, auth);

        // sběr statistik hráčů přes privátní metodu
        return collectPlayerStatus(match, isAdminOrManager);
    }

    // privátní metoda pro kontrolu přístupu hráče - jen pokud byl registrován na zápas
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

    // privátní metoda pro sběr statistik hráčů
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

    // dostupné zápasy pro hráče - byl nebo je aktivní
    @Override
    public List<MatchDTO> getAvailableMatchesForPlayer(Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);

        // Filtrace přes stream bez mezivýsledků
        return matchRepository.findAll().stream()
                .filter(match -> playerInactivityPeriodService.isActive(player, match.getDateTime()))
                .map(matchMapper::toDTO)
                .toList();
    }

    // získání hráče dle emailu
    public Long getPlayerIdByEmail(String email) {
        return playerRepository.findByUserEmail(email)
                .map(PlayerEntity::getId)
                .orElseThrow(() -> new PlayerNotFoundException(email));
    }


    // náhled nadcházejících zápasu pro hráče - dle PlayerType
    @Override
    public List<MatchOverviewDTO> getUpcomingMatchesOverviewForPlayer(Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);
        PlayerType type = player.getType();

        // 1) Nejbližší nadcházející zápasy podle data
        List<MatchEntity> upcomingAll = matchRepository.findByDateTimeAfterOrderByDateTimeAsc(LocalDateTime.now());

        // 2) Omezení podle typu hráče
        List<MatchEntity> limited = switch (type) {
            case VIP -> upcomingAll;
            case STANDARD -> upcomingAll.stream().limit(2).toList();
            case BASIC -> upcomingAll.isEmpty() ? List.of() : List.of(upcomingAll.get(0));
        };

        // 3) Filtrování podle aktivity hráče a mapování na MatchOverviewDTO
        return limited.stream()
                .filter(match -> playerInactivityPeriodService.isActive(player, match.getDateTime()))
                .map(match -> toOverviewDTO(match, playerId))
                .toList();
    }

    // nadcházející zápas
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

    // pomocné metody
    private PlayerEntity findPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    private MatchEntity findMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    private MatchOverviewDTO toOverviewDTO(MatchEntity match) {
        MatchOverviewDTO dto = new MatchOverviewDTO();
        dto.setId(match.getId());
        dto.setDateTime(match.getDateTime());
        dto.setLocation(match.getLocation());
        dto.setDescription(match.getDescription());
        dto.setPrice(match.getPrice());
        dto.setMaxPlayers(match.getMaxPlayers());


        // počet registrovaných hráčů
        int inGamePlayers = registrationService.getRegistrationsForMatch(match.getId()).stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .mapToInt(r -> 1)
                .sum();
        dto.setInGamePlayers(inGamePlayers);

        // cena na registrovaného hráče
        double pricePerPlayer = inGamePlayers > 0 && match.getPrice() != null
                ? match.getPrice() / (double) inGamePlayers : 0;
        dto.setPricePerRegisteredPlayer(pricePerPlayer);



        return dto;
    }

    private MatchOverviewDTO toOverviewDTO(MatchEntity match, Long playerId) {

        MatchOverviewDTO dto = toOverviewDTO(match); // ← znovupoužití tvé původní metody

        PlayerMatchStatus status = registrationService
                .getRegistrationsForMatch(match.getId()).stream()
                .filter(r -> r.getPlayerId().equals(playerId))
                .map(MatchRegistrationDTO::getStatus)
                .findFirst()
                .filter(s ->
                        s == PlayerMatchStatus.REGISTERED ||
                                s == PlayerMatchStatus.UNREGISTERED ||
                                s == PlayerMatchStatus.EXCUSED ||
                                s == PlayerMatchStatus.RESERVED
                )
                .orElse(PlayerMatchStatus.NO_RESPONSE);

        dto.setStatus(status);
        return dto;
    }

    @Override
    public List<MatchOverviewDTO> getAllPassedMatchesForPlayer(Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);

        // Vezmeme dostupné zápasy jako entity (bez DTO)
        List<MatchEntity> availableMatches = matchRepository.findAll().stream()
                .filter(match -> match.getDateTime().isBefore(LocalDateTime.now()) || match.getDateTime().isEqual(LocalDateTime.now()))
                .filter(match -> playerInactivityPeriodService.isActive(player, match.getDateTime()))
                .toList();

        if (availableMatches.isEmpty()) {
            return List.of();
        }

        // Všechny ID zápasů
        List<Long> matchIds = availableMatches.stream()
                .map(MatchEntity::getId)
                .toList();

        // Jeden jediný dotaz na všechny registrace
        List<MatchRegistrationDTO> allRegistrations =
                registrationService.getRegistrationsForMatches(matchIds);

        // Mapa: matchId -> (playerId -> status)
        var statusMap = allRegistrations.stream()
                .collect(Collectors.groupingBy(
                        MatchRegistrationDTO::getMatchId,
                        Collectors.toMap(
                                MatchRegistrationDTO::getPlayerId,
                                MatchRegistrationDTO::getStatus,
                                (a, b) -> a
                        )
                ));

        // Mapování na MatchOverviewDTO + nastavení statusu
        return availableMatches.stream()
                .map(match -> {
                    MatchOverviewDTO overview = toOverviewDTO(match);

                    PlayerMatchStatus status = Optional.ofNullable(statusMap.get(match.getId()))
                            .map(m -> m.get(playerId))
                            .filter(s ->
                                    s == PlayerMatchStatus.REGISTERED ||
                                            s == PlayerMatchStatus.UNREGISTERED ||
                                            s == PlayerMatchStatus.EXCUSED ||
                                            s == PlayerMatchStatus.RESERVED
                            )
                            .orElse(PlayerMatchStatus.NO_RESPONSE);

                    overview.setStatus(status);
                    return overview;
                })
                .toList();
    }



}
