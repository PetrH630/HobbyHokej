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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final CurrentPlayerService currentPlayerService;
    private static final Logger logger = LoggerFactory.getLogger(MatchServiceImpl.class);

    public MatchServiceImpl(MatchRepository matchRepository,
                            MatchMapper matchMapper,
                            MatchRegistrationService registrationService,
                            PlayerRepository playerRepository,
                            PlayerInactivityPeriodService playerInactivityPeriodService,
                            PlayerMapper playerMapper,  CurrentPlayerService currentPlayerService) {
        this.matchRepository = matchRepository;
        this.matchMapper = matchMapper;
        this.registrationService = registrationService;
        this.playerRepository = playerRepository;
        this.playerInactivityPeriodService = playerInactivityPeriodService;
        this.playerMapper = playerMapper;
        this.currentPlayerService = currentPlayerService;
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
                "BE - Zápas " + match.getId() + match.getDateTime() + " byl úspěšně smazán",
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
        MatchDetailDTO dto = collectPlayerStatus(match, isAdminOrManager);

        // 🔹 Zjistit aktuálního hráče (pokud je nastaven)
        Long currentPlayerId = null;
        try {
            currentPlayerId = currentPlayerService.getCurrentPlayerId();
        } catch (Exception e) {
            // pokud není vybraný aktuální hráč, necháme currentPlayerId = null → NO_RESPONSE
            logger.debug("Nebyl nalezen currentPlayerId pro match detail {}", id);
        }

        // 🔹 Určit status aktuálního hráče podle seznamů v DTO
        PlayerMatchStatus status = resolveStatusForPlayer(dto, currentPlayerId);
        dto.setStatus(status);

        return dto;

    }

    // privátní metoda pro kontrolu přístupu hráče - jen pokud byl registrován na zápas
    private void checkAccessForPlayer(MatchEntity match, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            // pokud máš na controlleru @PreAuthorize("isAuthenticated()"),
            // klidně můžeš jen return; ale výjimka je bezpečnější
            throw new org.springframework.security.access.AccessDeniedException(
                    "BE - Musíte být přihlášen."
            );
        }

        boolean isAdminOrManager = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(ROLE_ADMIN) || a.getAuthority().equals(ROLE_MANAGER));

        // Admin/manager vidí vždy vše
        if (isAdminOrManager) return;

        Object principal = auth.getPrincipal();
        if (!(principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails)) {
            // bezpečnostní fallback
            throw new org.springframework.security.access.AccessDeniedException(
                    "BE - Nemáte přístup k detailu tohoto zápasu."
            );
        }

        // všichni hráči patřící aktuálnímu uživateli
        List<PlayerEntity> ownedPlayers = playerRepository.findAll().stream()
                .filter(p -> p.getUser() != null && p.getUser().getEmail().equals(userDetails.getUsername()))
                .toList();

        if (ownedPlayers.isEmpty()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "BE - Nemáte přiřazeného žádného hráče."
            );
        }

        var now = LocalDateTime.now();
        boolean isPastOrNow = !match.getDateTime().isAfter(now); // true = minulý nebo právě teď
        List<Long> ownedPlayerIds = ownedPlayers.stream()
                .map(PlayerEntity::getId)
                .toList();

        // všechny registrace pro tenhle zápas
        List<MatchRegistrationDTO> registrations =
                registrationService.getRegistrationsForMatch(match.getId());

        if (!isPastOrNow) {
            // 🔹 NADCHÁZEJÍCÍ ZÁPAS
            // hráč má přístup, pokud má alespoň jednoho svého hráče,
            // který je pro datum zápasu "aktivní" (inactivity period)

            boolean hasActivePlayerForMatch = ownedPlayers.stream()
                    .anyMatch(p -> playerInactivityPeriodService.isActive(p, match.getDateTime()));

            if (!hasActivePlayerForMatch) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "BE - Nemáte aktivního hráče pro tento zápas."
                );
            }

            // žádná podmínka na status – REGISTERED/NO_RESPONSE/EXCUSED… neřešíme
            return;
        }

        // 🔹 UPLYNULÝ ZÁPAS
        // přístup jen pokud některý z hráčů měl status REGISTERED
        boolean wasRegistered = registrations.stream()
                .anyMatch(r ->
                        r.getStatus() == PlayerMatchStatus.REGISTERED
                                && ownedPlayerIds.contains(r.getPlayerId())
                );

        if (!wasRegistered) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "BE - K tomuto uplynulému zápasu nemáte oprávnění (nejste mezi registrovanými hráči)."
            );
        }
    }



    // privátní metoda pro sběr statistik hráčů
    private MatchDetailDTO collectPlayerStatus(MatchEntity match, boolean isAdminOrManager) {
        List<MatchRegistrationDTO> registrations = registrationService.getRegistrationsForMatch(match.getId());

        // mapování status -> hráči (z registrací)
        var statusToPlayersMap = registrations.stream()
                .map(r -> playerRepository.findById(r.getPlayerId())
                        .map(playerMapper::toDTO)
                        .map(dto -> new java.util.AbstractMap.SimpleEntry<>(r.getStatus(), dto))
                )
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.groupingBy(
                        java.util.Map.Entry::getKey,
                        Collectors.mapping(java.util.Map.Entry::getValue, Collectors.toList())
                ));

        List<PlayerEntity> allPlayers = playerRepository.findAll();
        Set<Long> respondedIds = registrations.stream()
                .map(MatchRegistrationDTO::getPlayerId)
                .collect(Collectors.toSet());

        // hráči bez registrace = NO_RESPONSE (pokud to používáš)
        List<PlayerDTO> noResponsePlayers = allPlayers.stream()
                .filter(p -> !respondedIds.contains(p.getId()))
                .map(playerMapper::toDTO)
                .toList();

        // ❗ TADY byla chyba – tohle pole bylo předtím spočítané stejně jako noResponsePlayers.
        // Už ho nepotřebujeme jako zvláštní kolekci, NO_EXCUSED bereme z mapy statusů.

        // --- POČTY HRÁČŮ ---

        int inGamePlayers =
                statusToPlayersMap.getOrDefault(PlayerMatchStatus.REGISTERED, List.of()).size();

        int outGamePlayers =
                statusToPlayersMap.getOrDefault(PlayerMatchStatus.UNREGISTERED, List.of()).size()
                        + statusToPlayersMap.getOrDefault(PlayerMatchStatus.EXCUSED, List.of()).size()
                        + statusToPlayersMap.getOrDefault(PlayerMatchStatus.NO_EXCUSED, List.of()).size(); // ⬅️ přidáno

        int waitingPlayers =
                statusToPlayersMap.getOrDefault(PlayerMatchStatus.RESERVED, List.of()).size();

        int noExcusedPlayersSum =
                statusToPlayersMap.getOrDefault(PlayerMatchStatus.NO_EXCUSED, List.of()).size(); // ⬅️ používáme mapu

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
        dto.setOutGamePlayers(outGamePlayers);
        dto.setWaitingPlayers(waitingPlayers);
        dto.setNoExcusedPlayersSum(noExcusedPlayersSum);   // ⬅️ vyplněno
        dto.setNoActionPlayers(noActionPlayers);
        dto.setPricePerRegisteredPlayer(pricePerRegistered);
        dto.setRemainingSlots(remainingSlots);

        // hráči podle statusů z mapy
        dto.setRegisteredPlayers(statusToPlayersMap.getOrDefault(PlayerMatchStatus.REGISTERED, List.of()));
        dto.setReservedPlayers(statusToPlayersMap.getOrDefault(PlayerMatchStatus.RESERVED, List.of()));
        dto.setUnregisteredPlayers(statusToPlayersMap.getOrDefault(PlayerMatchStatus.UNREGISTERED, List.of()));
        dto.setExcusedPlayers(statusToPlayersMap.getOrDefault(PlayerMatchStatus.EXCUSED, List.of()));
        dto.setNoExcusedPlayers(statusToPlayersMap.getOrDefault(PlayerMatchStatus.NO_EXCUSED, List.of())); // ⬅️ tady je hlavní seznam

        // no-response vidí jen admin/manager
        dto.setNoResponsePlayers(isAdminOrManager ? noResponsePlayers : null);

        return dto;
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

        if (isIn(dto.getUnregisteredPlayers(), playerId)) {
            return PlayerMatchStatus.UNREGISTERED;
        }

        if (isIn(dto.getNoExcusedPlayers(), playerId)) {
            return PlayerMatchStatus.NO_EXCUSED;
        }

        // hráč není v žádném seznamu → žádná registrace
        return PlayerMatchStatus.NO_RESPONSE;
    }

    private boolean isIn(List<PlayerDTO> players, Long playerId) {
        return players != null
                && players.stream().anyMatch(p -> p.getId().equals(playerId));
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
                ? match.getPrice() / (double) inGamePlayers : match.getPrice();
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
                                s == PlayerMatchStatus.RESERVED ||
                                s == PlayerMatchStatus.NO_EXCUSED
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
                                            s == PlayerMatchStatus.RESERVED ||
                                            s == PlayerMatchStatus.NO_EXCUSED
                            )
                            .orElse(PlayerMatchStatus.NO_RESPONSE);

                    overview.setStatus(status);
                    return overview;
                })
                .toList();
    }
    @Override
    public MatchRegistrationDTO markNoExcused(Long matchId, Long playerId, String adminNote) {
        // tady jen deleguješ na RegistrationService
        // (příp. můžeš přidat další validační logiku na úrovni zápasu/uživatele)
        return registrationService.markNoExcused(matchId, playerId, adminNote);
    }


}
