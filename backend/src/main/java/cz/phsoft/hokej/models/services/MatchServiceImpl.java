package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.*;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.data.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.*;
import cz.phsoft.hokej.models.dto.*;
import cz.phsoft.hokej.models.mappers.MatchMapper;
import cz.phsoft.hokej.models.mappers.PlayerMapper;
import cz.phsoft.hokej.models.services.notification.NotificationService;
import cz.phsoft.hokej.models.services.notification.MatchTimeChangeContext;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import cz.phsoft.hokej.data.entities.AppUserEntity;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service vrstva se používá pro práci se zápasy.
 * <p>
 * Zajišťuje CRUD operace nad zápasy v rámci sezón, filtrování
 * nadcházejících a proběhlých zápasů, výpočet statistik a přehledů
 * pro hráče i administrátory a rušení či obnovu zápasů.
 * <p>
 * Třída neřeší podrobnou logiku registrací hráčů, která je
 * umístěna v {@link MatchRegistrationService}, ani výběr
 * aktuálního hráče, který je spravován v {@link CurrentPlayerService}
 * a ve vrstvách controllerů.
 */
@Service
public class MatchServiceImpl implements MatchService {

    private static final Logger logger = LoggerFactory.getLogger(MatchServiceImpl.class);

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
    private final NotificationService notificationService;


    private final AppUserRepository appUserRepository;

    public MatchServiceImpl(MatchRepository matchRepository,
                            MatchRegistrationRepository matchRegistrationRepository,
                            MatchMapper matchMapper,
                            MatchRegistrationService registrationService,
                            PlayerRepository playerRepository,
                            PlayerInactivityPeriodService playerInactivityPeriodService,
                            PlayerMapper playerMapper,
                            CurrentPlayerService currentPlayerService,
                            SeasonService seasonService,
                            CurrentSeasonService currentSeasonService,
                            NotificationService notificationService,
                            AppUserRepository appUserRepository) {
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
        this.notificationService = notificationService;
        this.appUserRepository = appUserRepository;
    }

    // ======================
    // ZÁKLADNÍ SEZNAMY ZÁPASŮ
    // ======================

    /**
     * Vrátí všechny zápasy v rámci aktuálně používané sezóny.
     * <p>
     * Zápasy jsou seřazeny podle data a času vzestupně.
     * Součástí výstupu je i číslo zápasu v sezóně.
     */
    @Override
    public List<MatchDTO> getAllMatches() {
        Long seasonId = getCurrentSeasonIdOrActive();
        List<MatchEntity> matches =
                matchRepository.findAllBySeasonIdOrderByDateTimeAsc(seasonId);

        Map<Long, Integer> matchNumberMap = buildMatchNumberMapForSeason(seasonId);
        return assignMatchNumbers(matches, matchMapper::toDTO, matchNumberMap);
    }

    /**
     * Vrátí všechny nadcházející zápasy v aktuální sezóně.
     * <p>
     * Zápasy mají datum a čas v budoucnosti a jsou řazeny
     * podle data vzestupně. Výstup zahrnuje i číslo zápasu
     * v rámci sezóny.
     */
    @Override
    public List<MatchDTO> getUpcomingMatches() {
        Long seasonId = getCurrentSeasonIdOrActive();
        List<MatchEntity> upcomingMatches = findUpcomingMatchesForCurrentSeason();

        Map<Long, Integer> matchNumberMap = buildMatchNumberMapForSeason(seasonId);
        return assignMatchNumbers(upcomingMatches, matchMapper::toDTO, matchNumberMap);
    }

    /**
     * Vrátí všechny proběhlé zápasy v aktuální sezóně.
     * <p>
     * Zápasy mají datum a čas v minulosti a jsou řazeny
     * podle data sestupně (nejnovější jako první).
     * Výstup zahrnuje i číslo zápasu v rámci sezóny.
     */
    @Override
    public List<MatchDTO> getPastMatches() {
        Long seasonId = getCurrentSeasonIdOrActive();
        List<MatchEntity> pastMatches = findPastMatchesForCurrentSeason();

        Map<Long, Integer> matchNumberMap = buildMatchNumberMapForSeason(seasonId);
        return assignMatchNumbers(pastMatches, matchMapper::toDTO, matchNumberMap);
    }

    /**
     * Vrátí nejbližší nadcházející zápas v aktuální sezóně.
     * <p>
     * Pokud žádný nadcházející zápas neexistuje, vrací se {@code null}.
     */
    @Override
    public MatchDTO getNextMatch() {
        return findUpcomingMatchesForCurrentSeason()
                .stream()
                .findFirst()
                .map(matchMapper::toDTO)
                .orElse(null);
    }

    /**
     * Vrátí základní informace o zápasu podle ID.
     * <p>
     * Pokud zápas neexistuje, vyvolá se {@link MatchNotFoundException}.
     *
     * @param id ID zápasu
     * @return zápas ve formě {@link MatchDTO}
     */
    @Override
    public MatchDTO getMatchById(Long id) {
        return matchMapper.toDTO(findMatchOrThrow(id));
    }

    /**
     * Vytvoří nový zápas.
     * <p>
     * DTO se namapuje na entitu, ověří se, že datum zápasu
     * spadá do intervalu aktivní sezóny, a k zápasu se přiřadí
     * aktivní sezóna. Zápas se uloží a vrátí se ve formě DTO.
     */
    @Override
    public MatchDTO createMatch(MatchDTO dto) {
        MatchEntity entity = matchMapper.toEntity(dto);
        validateMatchDateInActiveSeason(entity.getDateTime());

        entity.setSeason(seasonService.getActiveSeason());

        // autor vytvoření + poslední úprava = aktuální uživatel
        Long currentUserId = getCurrentUserIdOrNull();
        entity.setCreatedByUserId(currentUserId);
        entity.setLastModifiedByUserId(currentUserId);

        return matchMapper.toDTO(matchRepository.save(entity));
    }

    /**
     * Aktualizuje existující zápas.
     * <p>
     * Zápas se načte z databáze, zkontroluje se oprávnění
     * podle role uživatele a ověří se, zda může být v aktivní sezóně upravován.
     * Poté se přenesou změny z DTO do entity a zápas se uloží.
     * Pokud se změní kapacita, přepočítají se stavy registrací.
     * Pokud se změní datum nebo čas, spustí se notifikace
     * o změně termínu zápasu.
     *
     * @throws InvalidMatchStatusException   pokud uživatel bez role administrátora
     *                                       nebo manažera upravuje zápas mimo aktivní sezónu
     * @throws InvalidMatchDateTimeException pokud by po úpravě zápas spadl do minulosti
     */
    @Override
    public MatchDTO updateMatch(Long id, MatchDTO dto) {
        MatchEntity entity = findMatchOrThrow(id);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrManager = hasAdminOrManagerRole(auth);

        if (!isAdminOrManager) {
            Long activeSeasonId = seasonService.getActiveSeason().getId();
            if (!entity.getSeason().getId().equals(activeSeasonId)) {
                throw new InvalidMatchStatusException(
                        id, " - Zápas nepatří do aktuální sezóny, nelze ho upravit."
                );
            }
        }

        int oldMaxPlayers = entity.getMaxPlayers();
        LocalDateTime oldDateTime = entity.getDateTime();

        matchMapper.updateEntity(dto, entity);

        // kdo provedl tuto změnu
        Long currentUserId = getCurrentUserIdOrNull();
        entity.setLastModifiedByUserId(currentUserId);

        if (!isAdminOrManager) {
            validateMatchDateInActiveSeason(entity.getDateTime());
        }

        MatchEntity saved = matchRepository.save(entity);

        if (saved.getMaxPlayers() != oldMaxPlayers) {
            registrationService.recalcStatusesForMatch(saved.getId());
        }

        if (saved.getDateTime().isBefore(LocalDateTime.now())) {
            throw new InvalidMatchDateTimeException("Zápas by již byl minulostí");
        }

        boolean dateTimeChanged = !saved.getDateTime().isEqual(oldDateTime);

        if (dateTimeChanged) {
            MatchTimeChangeContext ctx = new MatchTimeChangeContext(saved, oldDateTime);
            notifyPlayersAboutMatchChanges(ctx, MatchStatus.UPDATED);
        }

        return matchMapper.toDTO(saved);
    }

    /**
     * Smaže zápas podle ID.
     * <p>
     * Pokud zápas neexistuje, vyvolá se {@link MatchNotFoundException}.
     * Při úspěchu vrací standardizovanou odpověď s potvrzením
     * a časovou známkou operace.
     *
     * @param id ID zápasu
     * @return odpověď s informací o úspěchu smazání
     */
    @Override
    public SuccessResponseDTO deleteMatch(Long id) {
        MatchEntity match = findMatchOrThrow(id);
        matchRepository.delete(match);

        return new SuccessResponseDTO(
                "BE - Zápas " + match.getId() + match.getDateTime() + " byl úspěšně smazán",
                id,
                LocalDateTime.now().toString()
        );
    }

    /**
     * Zruší zápas s uvedeným důvodem.
     * <p>
     * Zápasu se nastaví stav {@link MatchStatus#CANCELLED}
     * a uloží se důvod zrušení. Pokud je zápas již zrušen,
     * vyvolá se {@link InvalidMatchStatusException}. Po zrušení
     * se odešlou notifikace přihlášeným hráčům.
     */
    @Override
    @Transactional
    public SuccessResponseDTO cancelMatch(Long matchId, MatchCancelReason reason) {
        MatchEntity match = findMatchOrThrow(matchId);
        String message = " je již zrušen";

        if (match.getMatchStatus() == MatchStatus.CANCELLED) {
            throw new InvalidMatchStatusException(matchId, message);
        }

        match.setMatchStatus(MatchStatus.CANCELLED);
        match.setCancelReason(reason);

        // kdo zápas zrušil
        Long currentUserId = getCurrentUserIdOrNull();
        match.setLastModifiedByUserId(currentUserId);

        MatchEntity saved = matchRepository.save(match);
        notifyPlayersAboutMatchChanges(saved, MatchStatus.CANCELLED);

        return new SuccessResponseDTO(
                "BE - Zápas " + match.getId() + match.getDateTime() + " byl úspěšně zrušen",
                match.getId(),
                LocalDateTime.now().toString()
        );
    }

    /**
     * Obnoví dříve zrušený zápas.
     * <p>
     * Zápasu se odstraní stav {@link MatchStatus#CANCELLED}
     * a důvod zrušení. Pokud zápas zrušen nebyl, vyvolá se
     * {@link InvalidMatchStatusException}. Implementace může
     * navazovat odeslání notifikací o obnovení zápasu.
     */
    @Override
    @Transactional
    public SuccessResponseDTO unCancelMatch(Long matchId) {
        MatchEntity match = findMatchOrThrow(matchId);
        String message = " ještě nebyl zrušen";

        if (match.getMatchStatus() != MatchStatus.CANCELLED) {
            throw new InvalidMatchStatusException(matchId, message);
        }

        match.setMatchStatus(null);
        match.setCancelReason(null);

        // kdo zápas obnovil
        Long currentUserId = getCurrentUserIdOrNull();
        match.setLastModifiedByUserId(currentUserId);

        return new SuccessResponseDTO(
                "BE - Zápas " + match.getId() + match.getDateTime() + " byl úspěšně obnoven",
                match.getId(),
                LocalDateTime.now().toString()
        );
    }

    // ======================
    // DETAIL ZÁPASU
    // ======================

    /**
     * Vrátí detail zápasu ve formě {@link MatchDetailDTO}.
     * <p>
     * Detail obsahuje souhrnné statistiky, seskupení hráčů podle stavu,
     * informace o kapacitě, cenu na registrovaného hráče a stav
     * aktuálně zvoleného hráče v zápase. Metoda současně uplatňuje
     * přístupová pravidla podle role uživatele a jeho navázaných hráčů.
     */
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

        if (match.getSeason() != null && match.getSeason().getId() != null) {
            Long seasonId = match.getSeason().getId();
            Map<Long, Integer> matchNumberMap = buildMatchNumberMapForSeason(seasonId);
            Integer number = matchNumberMap.get(match.getId());
            dto.setMatchNumber(number);
        }

        return dto;
    }

    /**
     * Ověří, zda má aktuální uživatel přístup k detailu zápasu.
     * <p>
     * Uživatel musí být přihlášen. Uživatel s rolí administrátora
     * nebo manažera má přístup vždy. Běžný uživatel musí mít
     * navázaného hráče a splnit podmínky podle toho, zda jde
     * o nadcházející nebo proběhlý zápas.
     * <p>
     * Pokud podmínky nejsou splněny, vyvolá se přístupová výjimka.
     */
    private void checkAccessForPlayer(MatchEntity match, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "BE - Musíte být přihlášen."
            );
        }

        boolean isAdminOrManager = hasAdminOrManagerRole(auth);
        if (isAdminOrManager) {
            return;
        }

        Long currentSeasonId = getCurrentSeasonIdOrActive();
        if (match.getSeason() == null || !match.getSeason().getId().equals(currentSeasonId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "BE - K detailu zápasu z jiné sezóny nemáte přístup."
            );
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "BE - Nemáte přístup k detailu tohoto zápasu."
            );
        }

        List<PlayerEntity> ownedPlayers =
                playerRepository.findByUser_EmailOrderByIdAsc(userDetails.getUsername());

        if (ownedPlayers.isEmpty()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "BE - Nemáte přiřazeného žádného hráče."
            );
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
                throw new org.springframework.security.access.AccessDeniedException(
                        "BE - Nemáte aktivního hráče pro tento zápas."
                );
            }
            return;
        }

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

    /**
     * Sestaví {@link MatchDetailDTO} pro daný zápas.
     * <p>
     * Zápis obsahuje seskupení hráčů podle statusů, počty hráčů
     * v jednotlivých kategoriích, počet volných míst, cenu
     * na registrovaného hráče a seznam hráčů bez reakce.
     * Seznam hráčů bez reakce je dostupný pouze pro administrátory
     * a manažery.
     */
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
                        java.util.Map.Entry::getKey,
                        Collectors.mapping(java.util.Map.Entry::getValue, Collectors.toList())
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

        return dto;
    }

    /**
     * Vytváří seznam hráčů pro daný tým a status registrace.
     *
     * Používá se k sestavení seznamu hráčů v konkrétním týmu
     * (např. DARK/LIGHT) se statusem REGISTERED pro daný zápas.
     */
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

    /**
     * Odvodí status konkrétního hráče z detailu zápasu.
     * <p>
     * Hráč se hledá v seznamech podle statusu. Pokud není
     * nalezen v žádné kategorii, vrací se stav NO_RESPONSE.
     */
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

    /**
     * Ověří, zda je hráč s daným ID přítomen v seznamu hráčů.
     */
    private boolean isIn(List<PlayerDTO> players, Long playerId) {
        return players != null
                && players.stream().anyMatch(p -> p.getId().equals(playerId));
    }

    // ======================
    // DALŠÍ PUBLIC METODY
    // ======================

    /**
     * Vrátí všechny zápasy, ve kterých může daný hráč potenciálně hrát.
     * <p>
     * Zápasy se vybírají napříč všemi sezónami a následně se filtrují
     * podle toho, zda je hráč v daném období aktivní podle
     * {@link PlayerInactivityPeriodService}.
     */
    @Override
    public List<MatchDTO> getAvailableMatchesForPlayer(Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);

        return matchRepository.findAll().stream()
                .filter(match -> isPlayerActiveForMatch(player, match.getDateTime()))
                .map(matchMapper::toDTO)
                .toList();
    }

    /**
     * Najde ID hráče podle e-mailu uživatele.
     * <p>
     * Pokud uživatel nemá žádného hráče, vyvolá se {@link PlayerNotFoundException}.
     */
    @Override
    public Long getPlayerIdByEmail(String email) {
        return playerRepository.findByUserEmail(email)
                .map(PlayerEntity::getId)
                .orElseThrow(() -> new PlayerNotFoundException(email));
    }

    /**
     * Vrátí přehled nadcházejících zápasů pro konkrétního hráče.
     * <p>
     * Zohledňuje typ hráče (VIP, STANDARD, BASIC) a omezuje počet
     * zobrazených zápasů. Současně filtruje jen zápasy, pro které
     * je hráč v daném datu aktivní, a nastavuje stav hráče v zápase.
     */
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

    /**
     * Vrátí seznam nadcházejících zápasů pro konkrétního hráče.
     * <p>
     * Výsledek je v plném formátu {@link MatchDTO} a je omezen
     * podle typu hráče a jeho aktivity v termínu zápasu.
     */
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

    /**
     * Vrátí přehled všech proběhlých zápasů aktuální sezóny,
     * kterých se hráč mohl účastnit.
     * <p>
     * Zápasy se filtrují podle aktivity hráče v datu zápasu,
     * hromadně se načtou registrace k těmto zápasům a pro každý
     * zápas se odvodí stav hráče. Výstup obsahuje i číslo zápasu
     * v sezóně.
     */
    @Override
    public List<MatchOverviewDTO> getAllPassedMatchesForPlayer(Long playerId) {
        PlayerEntity player = findPlayerOrThrow(playerId);

        List<MatchEntity> availableMatches =
                findPastMatchesForCurrentSeason().stream()
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

    /**
     * Najde hráče podle ID nebo vyhodí {@link PlayerNotFoundException}.
     */
    private PlayerEntity findPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    /**
     * Najde zápas podle ID nebo vyhodí {@link MatchNotFoundException}.
     */
    private MatchEntity findMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    /**
     * Najde registraci hráče k zápasu nebo vyhodí {@link MatchRegistrationNotFoundException}.
     */
    private MatchRegistrationEntity findMatchRegistrationOrThrow(Long playerId, Long matchId) {
        return matchRegistrationRepository.findByPlayerIdAndMatchId(playerId, matchId)
                .orElseThrow(() -> new MatchRegistrationNotFoundException(playerId, matchId));
    }

    /**
     * Vrátí aktuální čas.
     * <p>
     * Metoda je oddělena kvůli jednoduššímu testování.
     */
    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    // ======================
    // POMOCNÉ METODY – DTO MAPOVÁNÍ
    // ======================

    /**
     * Sestaví základní {@link MatchOverviewDTO} pro daný zápas.
     * <p>
     * DTO obsahuje základní informace o zápasu a počet hráčů
     * se stavem REGISTERED včetně vypočtené ceny na registrovaného
     * hráče.
     */
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

        return dto;
    }

    /**
     * Sestaví {@link MatchOverviewDTO} pro daný zápas v kontextu konkrétního hráče.
     * <p>
     * K základním údajům o zápasu doplní i stav hráče v daném zápase.
     */
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

    // ======================
    // DALŠÍ POMOCNÉ METODY
    // ======================

    /**
     * Zjistí, zda má uživatel roli administrátora nebo manažera.
     */
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

    /**
     * Vrátí všechny nadcházející zápasy v aktuální sezóně.
     * <p>
     * Zápasy mají datum a čas větší než aktuální okamžik
     * a jsou seřazeny podle data vzestupně.
     */
    private List<MatchEntity> findUpcomingMatchesForCurrentSeason() {
        return matchRepository.findBySeasonIdAndDateTimeAfterOrderByDateTimeAsc(
                getCurrentSeasonIdOrActive(),
                now()
        );
    }

    /**
     * Vrátí všechny proběhlé zápasy v aktuální sezóně.
     * <p>
     * Zápasy mají datum a čas menší než aktuální okamžik
     * a jsou seřazeny podle data sestupně.
     */
    private List<MatchEntity> findPastMatchesForCurrentSeason() {
        return matchRepository.findBySeasonIdAndDateTimeBeforeOrderByDateTimeDesc(
                getCurrentSeasonIdOrActive(),
                now()
        );
    }

    /**
     * Omezuje nadcházející zápasy podle typu hráče.
     * <p>
     * Pro typ VIP vrací tři nejbližší zápasy, pro STANDARD dva
     * a pro BASIC pouze jeden nejbližší zápas.
     */
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

    /**
     * Ověří, zda je hráč aktivní pro dané datum zápasu.
     * <p>
     * Informace o aktivitě hráče se zjišťuje pomocí
     * {@link PlayerInactivityPeriodService}.
     */
    private boolean isPlayerActiveForMatch(PlayerEntity player, LocalDateTime dateTime) {
        return playerInactivityPeriodService.isActive(player, dateTime);
    }

    /**
     * Normalizuje stav registrace hráče.
     * <p>
     * Pokud je stav {@code null}, vrací se NO_RESPONSE.
     * V případě ostatních podporovaných stavů se vrací
     * původní hodnota, jinak se použije NO_RESPONSE.
     */
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

    /**
     * Ověří, že datum zápasu spadá do období aktivní sezóny.
     * <p>
     * Pokud datum leží mimo interval aktivní sezóny, vyvolá se
     * {@link InvalidSeasonPeriodDateException}.
     */
    private void validateMatchDateInActiveSeason(LocalDateTime dateTime) {
        var activeSeason = seasonService.getActiveSeason();
        var date = dateTime.toLocalDate();

        if (date.isBefore(activeSeason.getStartDate()) ||
                date.isAfter(activeSeason.getEndDate())) {

            throw new InvalidSeasonPeriodDateException(
                    "BE - Datum zápasu musí být v rozmezí aktivní sezóny (" +
                            activeSeason.getStartDate() + " - " + activeSeason.getEndDate() + ")."
            );
        }
    }

    /**
     * Vrátí ID sezóny, která se má použít pro práci se zápasy.
     * <p>
     * Nejprve se použije sezóna uložená v {@link CurrentSeasonService}.
     * Pokud není k dispozici, použije se globálně aktivní sezóna
     * z {@link SeasonService}.
     */
    private Long getCurrentSeasonIdOrActive() {
        Long id = currentSeasonService.getCurrentSeasonIdOrDefault();
        if (id != null) {
            return id;
        }
        return seasonService.getActiveSeason().getId();
    }

    /**
     * Přidá zápasům číslo v sezóně podle mapy matchId → pořadí.
     * <p>
     * Metoda se používá jako společný pomocník pro mapování
     * entit na DTO, která implementují {@link NumberedMatchDTO}.
     */
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

    /**
     * Pro danou sezónu sestaví mapu matchId → pořadové číslo zápasu v sezóně.
     * <p>
     * Pořadí zápasů vychází z datumu a času zápasu řazeného vzestupně.
     */
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

    /**
     * Odešle notifikace hráčům o změnách souvisejících se zápasem.
     * <p>
     * Context představuje buď samotný zápas, nebo objekt
     * {@link MatchTimeChangeContext} v případě změny termínu.
     * Na základě typu změny zápasu se určí konkrétní typ notifikace.
     */
    private void notifyPlayersAboutMatchChanges(Object context, MatchStatus matchStatus) {
        MatchEntity match;
        if (context instanceof MatchTimeChangeContext mtc) {
            match = mtc.match();
        } else if (context instanceof MatchEntity m) {
            match = m;
        } else {
            throw new IllegalArgumentException("Nepodporovaný typ contextu: " + context);
        }

        var registrations = matchRegistrationRepository.findByMatchId(match.getId());

        registrations.stream()
                .filter(reg -> reg.getStatus() == PlayerMatchStatus.REGISTERED
                        || reg.getStatus() == PlayerMatchStatus.RESERVED)
                .forEach(reg -> {
                    PlayerEntity player = reg.getPlayer();

                    if (matchStatus == MatchStatus.UPDATED) {
                        notificationService.notifyPlayer(
                                player,
                                NotificationType.MATCH_TIME_CHANGED,
                                context
                        );
                    }

                    if (matchStatus == MatchStatus.CANCELLED) {
                        notificationService.notifyPlayer(
                                player,
                                NotificationType.MATCH_CANCELED,
                                match
                        );
                    }

                    if (matchStatus == MatchStatus.UNCANCELED) {
                        notificationService.notifyPlayer(
                                player,
                                NotificationType.MATCH_UNCANCELED,
                                match
                        );
                    }
                });
    }

    /**
     * Získá ID aktuálně přihlášeného uživatele nebo null,
     * pokud se nepodaří uživatele určit.
     */
    private Long getCurrentUserIdOrNull() { // *** NOVÉ ***
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        String email = auth.getName(); // předpoklad: username = email
        return appUserRepository.findByEmail(email)
                .map(AppUserEntity::getId)
                .orElse(null);
    }

}
