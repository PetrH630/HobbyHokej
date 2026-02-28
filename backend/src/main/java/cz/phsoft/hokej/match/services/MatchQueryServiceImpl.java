package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.dto.MatchDTO;
import cz.phsoft.hokej.match.dto.MatchDetailDTO;
import cz.phsoft.hokej.match.dto.MatchOverviewDTO;
import cz.phsoft.hokej.match.dto.NumberedMatchDTO;
import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.match.enums.MatchStatus;
import cz.phsoft.hokej.match.exceptions.MatchNotFoundException;
import cz.phsoft.hokej.match.mappers.MatchMapper;
import cz.phsoft.hokej.match.repositories.MatchRepository;
import cz.phsoft.hokej.notifications.services.NotificationService;
import cz.phsoft.hokej.player.dto.PlayerDTO;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.player.enums.PlayerType;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.player.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.player.mappers.PlayerMapper;
import cz.phsoft.hokej.player.repositories.PlayerRepository;
import cz.phsoft.hokej.player.services.CurrentPlayerService;
import cz.phsoft.hokej.player.services.PlayerInactivityPeriodService;
import cz.phsoft.hokej.registration.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.registration.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.registration.services.MatchRegistrationService;
import cz.phsoft.hokej.season.exceptions.InvalidSeasonPeriodDateException;
import cz.phsoft.hokej.season.services.CurrentSeasonService;
import cz.phsoft.hokej.season.services.SeasonService;
import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.user.repositories.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementace čtecí service vrstvy pro zápasy.
 *
 * Poskytuje metody pro načítání seznamů zápasů, detailu zápasu
 * a přehledů zápasů pro konkrétního hráče. Zajišťuje také
 * vyhodnocení přístupových práv k detailu zápasu a sestavení
 * agregovaných statistik registrací hráčů.
 *
 * Třída neprovádí žádné změny stavu v databázi ani neodesílá
 * notifikace. Změnové operace jsou řešeny v MatchCommandService.
 */
@Service
public class MatchQueryServiceImpl implements MatchQueryService {

    private static final Logger logger = LoggerFactory.getLogger(MatchQueryServiceImpl.class);

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_MANAGER = "ROLE_MANAGER";

    private final MatchRepository matchRepository;
    private final MatchRegistrationRepository matchRegistrationRepository;
    private final MatchMapper matchMapper;
    private final MatchRegistrationService registrationService;
    private final PlayerRepository playerRepository;
    private final PlayerInactivityPeriodService playerInactivityPeriodService;
    private final PlayerMapper playerMapper;
    private final CurrentPlayerService currentPlayerService;
    private final SeasonService seasonService;
    private final CurrentSeasonService currentSeasonService;
    private final Clock clock;

    public MatchQueryServiceImpl(
            MatchRepository matchRepository,
            MatchRegistrationRepository matchRegistrationRepository,
            MatchMapper matchMapper,
            MatchRegistrationService registrationService,
            PlayerRepository playerRepository,
            PlayerInactivityPeriodService playerInactivityPeriodService,
            PlayerMapper playerMapper,
            CurrentPlayerService currentPlayerService,
            SeasonService seasonService,
            CurrentSeasonService currentSeasonService,
            Clock clock
    ) {
        this.matchRepository = matchRepository;
        this.matchRegistrationRepository = matchRegistrationRepository;
        this.matchMapper = matchMapper;
        this.registrationService = registrationService;
        this.playerRepository = playerRepository;
        this.playerInactivityPeriodService = playerInactivityPeriodService;
        this.playerMapper = playerMapper;
        this.currentPlayerService = currentPlayerService;
        this.seasonService = seasonService;
        this.currentSeasonService = currentSeasonService;
        this.clock = clock;
    }

    // ======================
    // ZÁKLADNÍ SEZNAMY ZÁPASŮ
    // ======================

    @Override
    public List<MatchDTO> getAllMatches() {
        Long seasonId = getCurrentSeasonIdOrActive();
        List<MatchEntity> matches =
                matchRepository.findAllBySeasonIdOrderByDateTimeAsc(seasonId);

        Map<Long, Integer> matchNumberMap = buildMatchNumberMapForSeason(seasonId);
        return assignMatchNumbers(matches, matchMapper::toDTO, matchNumberMap);
    }

    @Override
    public List<MatchDTO> getUpcomingMatches() {
        Long seasonId = getCurrentSeasonIdOrActive();
        List<MatchEntity> upcomingMatches = findUpcomingMatchesForCurrentSeason();

        Map<Long, Integer> matchNumberMap = buildMatchNumberMapForSeason(seasonId);
        return assignMatchNumbers(upcomingMatches, matchMapper::toDTO, matchNumberMap);
    }

    @Override
    public List<MatchDTO> getPastMatches() {
        Long seasonId = getCurrentSeasonIdOrActive();
        List<MatchEntity> pastMatches = findPastMatchesForCurrentSeason();

        Map<Long, Integer> matchNumberMap = buildMatchNumberMapForSeason(seasonId);
        return assignMatchNumbers(pastMatches, matchMapper::toDTO, matchNumberMap);
    }

    @Override
    public MatchDTO getNextMatch() {
        return findUpcomingMatchesForCurrentSeason()
                .stream()
                .findFirst()
                .map(matchMapper::toDTO)
                .orElse(null);
    }

    @Override
    public MatchDTO getMatchById(Long id) {
        return matchMapper.toDTO(findMatchOrThrow(id));
    }

    // ======================
    // DETAIL ZÁPASU
    // ======================

    @Override
    public MatchDetailDTO getMatchDetail(Long id) {
        MatchEntity match = findMatchOrThrow(id);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrManager = hasAdminOrManagerRole(auth);

        checkAccessForPlayer(match, auth);

        MatchDetailDTO dto = collectPlayerStatus(match, isAdminOrManager);

        Long currentPlayerId = null;
        try {
            currentPlayerId = currentPlayerService.getCurrentPlayerId();
        } catch (Exception e) {
            logger.debug("Nebyl nalezen currentPlayerId pro match detail {}", id);
        }

        PlayerMatchStatus playerMatchStatus = resolveStatusForPlayer(dto, currentPlayerId);
        dto.setPlayerMatchStatus(playerMatchStatus);

        if (currentPlayerId != null) {
            matchRegistrationRepository.findByPlayerIdAndMatchId(currentPlayerId, match.getId())
                    .ifPresent(reg -> {
                        dto.setExcuseReason(reg.getExcuseReason());
                        dto.setExcuseNote(reg.getExcuseNote());
                    });
        } else {
            dto.setExcuseReason(null);
            dto.setExcuseNote(null);
        }

        dto.setMatchStatus(match.getMatchStatus());
        dto.setCancelReason(match.getCancelReason());
        dto.setMatchMode(match.getMatchMode());

        if (match.getSeason() != null && match.getSeason().getId() != null) {
            Long seasonId = match.getSeason().getId();
            Map<Long, Integer> matchNumberMap = buildMatchNumberMapForSeason(seasonId);
            Integer number = matchNumberMap.get(match.getId());
            dto.setMatchNumber(number);
            dto.setSeasonId(seasonId);
        }

        return dto;
    }

    // ======================
    // DALŠÍ PUBLIC METODY (READ)
    // ======================

    @Override
    public List<MatchDTO> getAvailableMatchesForPlayer(Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);

        return matchRepository.findAll().stream()
                .filter(match -> isPlayerActiveForMatch(player, match.getDateTime()))
                .map(matchMapper::toDTO)
                .toList();
    }

    @Override
    public Long getPlayerIdByEmail(String email) {
        return playerRepository.findByUserEmail(email)
                .map(PlayerEntity::getId)
                .orElseThrow(() -> new PlayerNotFoundException(email));
    }

    @Override
    public List<MatchOverviewDTO> getUpcomingMatchesOverviewForPlayer(Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);
        PlayerType type = player.getType();

        List<MatchEntity> upcomingAll = findUpcomingMatchesForCurrentSeason();
        List<MatchEntity> limited = limitMatchesByPlayerType(upcomingAll, type);

        List<MatchEntity> activeMatches = limited.stream()
                .filter(match -> isPlayerActiveForMatch(player, match.getDateTime()))
                .toList();

        Long seasonId = getCurrentSeasonIdOrActive();
        Map<Long, Integer> matchNumberMap = buildMatchNumberMapForSeason(seasonId);

        return assignMatchNumbers(
                activeMatches,
                match -> toOverviewDTO(match, playerId),
                matchNumberMap
        );
    }

    @Override
    public List<MatchDTO> getUpcomingMatchesForPlayer(Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);
        PlayerType type = player.getType();

        List<MatchEntity> upcomingAll = findUpcomingMatchesForCurrentSeason();
        List<MatchEntity> limited = limitMatchesByPlayerType(upcomingAll, type);

        List<MatchEntity> activeMatches = limited.stream()
                .filter(match -> isPlayerActiveForMatch(player, match.getDateTime()))
                .toList();

        Long seasonId = getCurrentSeasonIdOrActive();
        Map<Long, Integer> matchNumberMap = buildMatchNumberMapForSeason(seasonId);

        return assignMatchNumbers(activeMatches, matchMapper::toDTO, matchNumberMap);
    }

    @Override
    public List<MatchOverviewDTO> getAllPassedMatchesForPlayer(Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);

        LocalDateTime playerCreatedDate = player.getTimestamp();

        List<MatchEntity> availableMatches =
                findPastMatchesForCurrentSeason().stream()
                        .filter(match -> match.getDateTime().isAfter(playerCreatedDate))
                        .filter(match -> isPlayerActiveForMatch(player, match.getDateTime()))
                        .toList();

        if (availableMatches.isEmpty()) {
            return List.of();
        }

        List<Long> matchIds = availableMatches.stream()
                .map(MatchEntity::getId)
                .toList();

        List<MatchRegistrationDTO> allRegistrations =
                registrationService.getRegistrationsForMatches(matchIds);

        var statusMap = allRegistrations.stream()
                .collect(Collectors.groupingBy(
                        MatchRegistrationDTO::getMatchId,
                        Collectors.toMap(
                                MatchRegistrationDTO::getPlayerId,
                                MatchRegistrationDTO::getStatus,
                                (a, b) -> a
                        )
                ));

        List<MatchOverviewDTO> overviews = availableMatches.stream()
                .map(match -> {
                    MatchOverviewDTO overview = toOverviewDTO(match);
                    PlayerMatchStatus playerMatchStatus = Optional.ofNullable(statusMap.get(match.getId()))
                            .map(m -> normalizePlayerStatus(m.get(playerId)))
                            .orElse(PlayerMatchStatus.NO_RESPONSE);
                    overview.setPlayerMatchStatus(playerMatchStatus);
                    return overview;
                })
                .toList();

        Long seasonId = getCurrentSeasonIdOrActive();
        Map<Long, Integer> matchNumberMap = buildMatchNumberMapForSeason(seasonId);

        overviews.forEach(o -> o.setMatchNumber(matchNumberMap.get(o.getId())));

        return overviews;
    }

    // ======================
    // PŘÍSTUP A DETAIL – PRIVÁTNÍ METODY
    // ======================

    private void checkAccessForPlayer(MatchEntity match, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("BE - Musíte být přihlášen.");
        }

        boolean isAdminOrManager = hasAdminOrManagerRole(auth);
        if (isAdminOrManager) {
            return;
        }

        Long currentSeasonId = getCurrentSeasonIdOrActive();
        if (match.getSeason() == null || !match.getSeason().getId().equals(currentSeasonId)) {
            throw new AccessDeniedException("BE - K detailu zápasu z jiné sezóny nemáte přístup.");
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails)) {
            throw new AccessDeniedException("BE - Nemáte přístup k detailu tohoto zápasu.");
        }

        List<PlayerEntity> ownedPlayers =
                playerRepository.findByUser_EmailOrderByIdAsc(userDetails.getUsername());

        if (ownedPlayers.isEmpty()) {
            throw new AccessDeniedException("BE - Nemáte přiřazeného žádného hráče.");
        }

        LocalDateTime now = now();
        boolean isPastOrNow = !match.getDateTime().isAfter(now);
        List<Long> ownedPlayerIds = ownedPlayers.stream()
                .map(PlayerEntity::getId)
                .toList();

        List<MatchRegistrationDTO> registrations =
                registrationService.getRegistrationsForMatch(match.getId());

        if (!isPastOrNow) {
            boolean hasActivePlayerForMatch = ownedPlayers.stream()
                    .anyMatch(p -> isPlayerActiveForMatch(p, match.getDateTime()));

            if (!hasActivePlayerForMatch) {
                throw new AccessDeniedException("BE - Nemáte aktivního hráče pro tento zápas.");
            }
            return;
        }

        boolean wasRegistered = registrations.stream()
                .anyMatch(r ->
                        r.getStatus() == PlayerMatchStatus.REGISTERED
                                && ownedPlayerIds.contains(r.getPlayerId())
                );

        if (!wasRegistered) {
            throw new AccessDeniedException(
                    "BE - K tomuto uplynulému zápasu nemáte oprávnění (nejste mezi registrovanými hráči)."
            );
        }
    }

    private MatchDetailDTO collectPlayerStatus(MatchEntity match, boolean isAdminOrManager) {
        List<MatchRegistrationDTO> registrations =
                registrationService.getRegistrationsForMatch(match.getId());

        var statusToPlayersMap = registrations.stream()
                .map(r -> playerRepository.findById(r.getPlayerId())
                        .map(playerMapper::toDTO)
                        .map(dto -> new AbstractMap.SimpleEntry<>(r.getStatus(), dto))
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));

        List<PlayerDTO> noResponsePlayers =
                registrationService.getNoResponsePlayers(match.getId());
        List<PlayerDTO> registeredDarkPlayers = getRegisteredPlayersForTeam(registrations, Team.DARK);
        List<PlayerDTO> registeredLightPlayers = getRegisteredPlayersForTeam(registrations, Team.LIGHT);

        int inGamePlayers =
                statusToPlayersMap.getOrDefault(PlayerMatchStatus.REGISTERED, List.of()).size();

        int inGamePlayersDark =
                (int) registrations.stream()
                        .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                        .filter(r -> r.getTeam() == Team.DARK)
                        .count();

        int inGamePlayersLight =
                (int) registrations.stream()
                        .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                        .filter(r -> r.getTeam() == Team.LIGHT)
                        .count();

        int substitutePlayers =
                statusToPlayersMap.getOrDefault(PlayerMatchStatus.SUBSTITUTE, List.of()).size();

        int outGamePlayers =
                statusToPlayersMap.getOrDefault(PlayerMatchStatus.UNREGISTERED, List.of()).size()
                        + statusToPlayersMap.getOrDefault(PlayerMatchStatus.EXCUSED, List.of()).size()
                        + statusToPlayersMap.getOrDefault(PlayerMatchStatus.NO_EXCUSED, List.of()).size();

        int waitingPlayers =
                statusToPlayersMap.getOrDefault(PlayerMatchStatus.RESERVED, List.of()).size();

        int noExcusedPlayersSum =
                statusToPlayersMap.getOrDefault(PlayerMatchStatus.NO_EXCUSED, List.of()).size();

        int noActionPlayers = noResponsePlayers.size();

        int remainingSlots = match.getMaxPlayers() - inGamePlayers;
        double pricePerRegistered = inGamePlayers > 0
                ? match.getPrice() / (double) inGamePlayers
                : match.getPrice();

        MatchDetailDTO dto = new MatchDetailDTO();
        dto.setId(match.getId());
        dto.setDateTime(match.getDateTime());
        dto.setLocation(match.getLocation());
        dto.setDescription(match.getDescription());
        dto.setPrice(match.getPrice());
        dto.setMaxPlayers(match.getMaxPlayers());
        dto.setInGamePlayers(inGamePlayers);
        dto.setInGamePlayersDark(inGamePlayersDark);
        dto.setInGamePlayersLight(inGamePlayersLight);
        dto.setSubstitutePlayers(substitutePlayers);
        dto.setOutGamePlayers(outGamePlayers);
        dto.setWaitingPlayers(waitingPlayers);
        dto.setNoExcusedPlayersSum(noExcusedPlayersSum);
        dto.setNoActionPlayers(noActionPlayers);
        dto.setPricePerRegisteredPlayer(pricePerRegistered);
        dto.setRemainingSlots(remainingSlots);

        dto.setRegisteredPlayers(statusToPlayersMap.getOrDefault(PlayerMatchStatus.REGISTERED, List.of()));
        dto.setReservedPlayers(statusToPlayersMap.getOrDefault(PlayerMatchStatus.RESERVED, List.of()));
        dto.setUnregisteredPlayers(statusToPlayersMap.getOrDefault(PlayerMatchStatus.UNREGISTERED, List.of()));
        dto.setExcusedPlayers(statusToPlayersMap.getOrDefault(PlayerMatchStatus.EXCUSED, List.of()));
        dto.setSubstitutedPlayers(statusToPlayersMap.getOrDefault(PlayerMatchStatus.SUBSTITUTE, List.of()));
        dto.setNoExcusedPlayers(statusToPlayersMap.getOrDefault(PlayerMatchStatus.NO_EXCUSED, List.of()));

        dto.setNoResponsePlayers(isAdminOrManager ? noResponsePlayers : null);
        dto.setRegisteredDarkPlayers(registeredDarkPlayers);
        dto.setRegisteredLightPlayers(registeredLightPlayers);
        dto.setRegistrations(registrations);

        return dto;
    }

    private List<PlayerDTO> getRegisteredPlayersForTeam(List<MatchRegistrationDTO> registrations, Team team) {
        return registrations.stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .filter(r -> r.getTeam() == team)
                .map(MatchRegistrationDTO::getPlayerId)
                .distinct()
                .map(playerRepository::findById)
                .flatMap(Optional::stream)
                .map(playerMapper::toDTO)
                .toList();
    }

    private PlayerMatchStatus resolveStatusForPlayer(MatchDetailDTO dto, Long playerId) {
        if (dto == null || playerId == null) {
            return PlayerMatchStatus.NO_RESPONSE;
        }

        if (isIn(dto.getRegisteredPlayers(), playerId)) {
            return PlayerMatchStatus.REGISTERED;
        }
        if (isIn(dto.getReservedPlayers(), playerId)) {
            return PlayerMatchStatus.RESERVED;
        }
        if (isIn(dto.getExcusedPlayers(), playerId)) {
            return PlayerMatchStatus.EXCUSED;
        }
        if (isIn(dto.getSubstitutedPlayers(), playerId)) {
            return PlayerMatchStatus.SUBSTITUTE;
        }
        if (isIn(dto.getUnregisteredPlayers(), playerId)) {
            return PlayerMatchStatus.UNREGISTERED;
        }
        if (isIn(dto.getNoExcusedPlayers(), playerId)) {
            return PlayerMatchStatus.NO_EXCUSED;
        }

        return PlayerMatchStatus.NO_RESPONSE;
    }

    private boolean isIn(List<PlayerDTO> players, Long playerId) {
        return players != null
                && players.stream().anyMatch(p -> p.getId().equals(playerId));
    }

    // ======================
    // DTO MAPOVÁNÍ A HELPERY
    // ======================

    private MatchOverviewDTO toOverviewDTO(MatchEntity match) {
        MatchOverviewDTO dto = new MatchOverviewDTO();
        dto.setId(match.getId());
        dto.setDateTime(match.getDateTime());
        dto.setLocation(match.getLocation());
        dto.setDescription(match.getDescription());
        dto.setPrice(match.getPrice());
        dto.setMaxPlayers(match.getMaxPlayers());

        int inGamePlayers = registrationService.getRegistrationsForMatch(match.getId()).stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .mapToInt(r -> 1)
                .sum();
        dto.setInGamePlayers(inGamePlayers);

        double pricePerPlayer = inGamePlayers > 0 && match.getPrice() != null
                ? match.getPrice() / (double) inGamePlayers
                : match.getPrice();
        dto.setPricePerRegisteredPlayer(pricePerPlayer);

        dto.setMatchStatus(match.getMatchStatus());
        dto.setCancelReason(match.getCancelReason());
        dto.setMatchMode(match.getMatchMode());

        if (match.getSeason() != null && match.getSeason().getId() != null) {
            dto.setSeasonId(match.getSeason().getId());
        }

        return dto;
    }

    private MatchOverviewDTO toOverviewDTO(MatchEntity match, Long playerId) {
        MatchOverviewDTO dto = toOverviewDTO(match);

        PlayerMatchStatus playerMatchStatus = registrationService
                .getRegistrationsForMatch(match.getId()).stream()
                .filter(r -> r.getPlayerId().equals(playerId))
                .map(MatchRegistrationDTO::getStatus)
                .findFirst()
                .map(this::normalizePlayerStatus)
                .orElse(PlayerMatchStatus.NO_RESPONSE);

        dto.setPlayerMatchStatus(playerMatchStatus);
        return dto;
    }

    private PlayerMatchStatus normalizePlayerStatus(PlayerMatchStatus status) {
        if (status == null) {
            return PlayerMatchStatus.NO_RESPONSE;
        }

        return switch (status) {
            case REGISTERED,
                 UNREGISTERED,
                 EXCUSED,
                 SUBSTITUTE,
                 RESERVED,
                 NO_EXCUSED -> status;
            default -> PlayerMatchStatus.NO_RESPONSE;
        };
    }

    private boolean hasAdminOrManagerRole(Authentication auth) {
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a ->
                        ROLE_ADMIN.equals(a.getAuthority()) ||
                                ROLE_MANAGER.equals(a.getAuthority())
                );
    }

    private List<MatchEntity> findUpcomingMatchesForCurrentSeason() {
        return matchRepository.findBySeasonIdAndDateTimeAfterOrderByDateTimeAsc(
                getCurrentSeasonIdOrActive(),
                now()
        );
    }

    private List<MatchEntity> findPastMatchesForCurrentSeason() {
        return matchRepository.findBySeasonIdAndDateTimeBeforeOrderByDateTimeDesc(
                getCurrentSeasonIdOrActive(),
                now()
        );
    }

    private List<MatchEntity> limitMatchesByPlayerType(List<MatchEntity> upcomingAll, PlayerType type) {
        if (upcomingAll == null || upcomingAll.isEmpty()) {
            return List.of();
        }

        return switch (type) {
            case VIP -> upcomingAll.stream().limit(3).toList();
            case STANDARD -> upcomingAll.stream().limit(2).toList();
            case BASIC -> List.of(upcomingAll.get(0));
        };
    }

    private PlayerEntity findPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    private MatchEntity findMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    private boolean isPlayerActiveForMatch(PlayerEntity player, LocalDateTime dateTime) {
        return playerInactivityPeriodService.isActive(player, dateTime);
    }

    private Long getCurrentSeasonIdOrActive() {
        Long id = currentSeasonService.getCurrentSeasonIdOrDefault();
        if (id != null) {
            return id;
        }
        return seasonService.getActiveSeason().getId();
    }

    private <D extends NumberedMatchDTO> List<D> assignMatchNumbers(
            List<MatchEntity> matches,
            Function<MatchEntity, D> mapper,
            Map<Long, Integer> matchNumberMap
    ) {
        return matches.stream()
                .map(entity -> {
                    D dto = mapper.apply(entity);
                    Integer number = matchNumberMap.get(entity.getId());
                    dto.setMatchNumber(number);
                    return dto;
                })
                .toList();
    }

    private Map<Long, Integer> buildMatchNumberMapForSeason(Long seasonId) {
        List<MatchEntity> allMatchesInSeason =
                matchRepository.findAllBySeasonIdOrderByDateTimeAsc(seasonId);

        Map<Long, Integer> map = new HashMap<>();
        int counter = 1;
        for (MatchEntity m : allMatchesInSeason) {
            map.put(m.getId(), counter++);
        }
        return map;
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}