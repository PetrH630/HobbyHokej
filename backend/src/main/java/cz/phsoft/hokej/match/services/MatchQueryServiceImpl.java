package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.dto.MatchDTO;
import cz.phsoft.hokej.match.dto.MatchDetailDTO;
import cz.phsoft.hokej.match.dto.MatchOverviewDTO;
import cz.phsoft.hokej.match.dto.NumberedMatchDTO;
import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.match.exceptions.MatchNotFoundException;
import cz.phsoft.hokej.match.mappers.MatchMapper;
import cz.phsoft.hokej.match.repositories.MatchRepository;
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
import cz.phsoft.hokej.season.services.CurrentSeasonService;
import cz.phsoft.hokej.season.services.SeasonService;
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

    /**
     * Vytváří instanci čtecí servisní vrstvy pro zápasy.
     *
     * V konstruktoru se předávají všechny závislosti potřebné
     * pro načítání zápasů, registrací, hráčů a sezón a pro práci s časem.
     *
     * @param matchRepository repozitář zápasů
     * @param matchRegistrationRepository repozitář registrací k zápasům
     * @param matchMapper mapper pro převod entit zápasů na DTO
     * @param registrationService servisní vrstva pro práci s registracemi
     * @param playerRepository repozitář hráčů
     * @param playerInactivityPeriodService služba pro vyhodnocení aktivity hráče
     * @param playerMapper mapper pro převod entit hráčů na DTO
     * @param currentPlayerService služba pro zjištění aktuálního hráče
     * @param seasonService služba pro práci se sezónami
     * @param currentSeasonService služba pro určení aktuální sezóny
     * @param clock zdroj aktuálního času
     */
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


    // ZÁKLADNÍ SEZNAMY ZÁPASŮ


    /**
     * Vrací všechny zápasy aktuální nebo aktivní sezóny.
     *
     * Zápasy jsou načteny podle identifikátoru sezóny, seřazeny podle data
     * a převedeny na DTO včetně přiřazeného pořadového čísla zápasu v sezóně.
     *
     * @return seznam zápasů aktuální sezóny
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
     * Vrací všechny nadcházející zápasy aktuální sezóny.
     *
     * Zápasy jsou filtrovány podle aktuálního času a seřazeny vzestupně
     * podle data a času konání. Každému zápasu je přiřazeno pořadové číslo v sezóně.
     *
     * @return seznam nadcházejících zápasů
     */
    @Override
    public List<MatchDTO> getUpcomingMatches() {
        Long seasonId = getCurrentSeasonIdOrActive();
        List<MatchEntity> upcomingMatches = findUpcomingMatchesForCurrentSeason();

        Map<Long, Integer> matchNumberMap = buildMatchNumberMapForSeason(seasonId);
        return assignMatchNumbers(upcomingMatches, matchMapper::toDTO, matchNumberMap);
    }

    /**
     * Vrací všechny již odehrané zápasy aktuální sezóny.
     *
     * Zápasy jsou určeny podle aktuálního času a seřazeny sestupně
     * podle data a času konání. Každému zápasu je přiřazeno pořadové číslo v sezóně.
     *
     * @return seznam odehraných zápasů
     */
    @Override
    public List<MatchDTO> getPastMatches() {
        Long seasonId = getCurrentSeasonIdOrActive();
        List<MatchEntity> pastMatches = findPastMatchesForCurrentSeason();

        Map<Long, Integer> matchNumberMap = buildMatchNumberMapForSeason(seasonId);
        return assignMatchNumbers(pastMatches, matchMapper::toDTO, matchNumberMap);
    }

    /**
     * Vrací nejbližší nadcházející zápas aktuální sezóny.
     *
     * Pokud v sezóně neexistuje žádný budoucí zápas, vrací se null.
     *
     * @return nejbližší budoucí zápas nebo null
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
     * Vrací základní informace o zápasu podle jeho identifikátoru.
     *
     * Metoda vyhledá entitu zápasu a převede ji na MatchDTO.
     *
     * @param id identifikátor zápasu
     * @return DTO reprezentující zápas
     */
    @Override
    public MatchDTO getMatchById(Long id) {
        return matchMapper.toDTO(findMatchOrThrow(id));
    }


    // DETAIL ZÁPASU


    /**
     * Vrací detail zápasu včetně agregovaných statistik a stavu aktuálního hráče.
     *
     * Součástí metody je:
     * - kontrola oprávnění k přístupu k detailu,
     * - sestavení přehledu registrací hráčů podle statusu,
     * - doplnění informací o stavu aktuálního hráče v zápase,
     * - doplnění informací o skóre, výsledku a sezóně.
     *
     * @param id identifikátor zápasu
     * @return detail zápasu jako MatchDetailDTO
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


    // DALŠÍ PUBLIC METODY (READ)


    /**
     * Vrací zápasy, které jsou dostupné konkrétnímu hráči.
     *
     * Hráč je považován za způsobilého k zápasu, pokud je aktivní
     * v čase konání zápasu podle pravidel PlayerInactivityPeriodService.
     *
     * @param playerId identifikátor hráče
     * @return seznam dostupných zápasů jako MatchDTO
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
     * Vrací identifikátor hráče podle e-mailu uživatele.
     *
     * E-mail je hledán na navázané entitě User u hráče.
     *
     * @param email e-mail uživatele
     * @return identifikátor hráče
     * @throws PlayerNotFoundException pokud pro daný e-mail neexistuje hráč
     */
    @Override
    public Long getPlayerIdByEmail(String email) {
        return playerRepository.findByUserEmail(email)
                .map(PlayerEntity::getId)
                .orElseThrow(() -> new PlayerNotFoundException(email));
    }

    /**
     * Vrací přehled nadcházejících zápasů pro konkrétního hráče.
     *
     * Metoda:
     * - určí typ hráče (VIP, STANDARD, BASIC),
     * - omezí počet vracených zápasů podle typu,
     * - zohlední aktivitu hráče v čase konání zápasu,
     * - přiřadí pořadová čísla zápasů v sezóně,
     * - sestaví MatchOverviewDTO včetně stavu daného hráče.
     *
     * @param playerId identifikátor hráče
     * @return seznam přehledových DTO nadcházejících zápasů
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
     * Vrací nadcházející zápasy pro konkrétního hráče jako MatchDTO.
     *
     * Omezí počet zápasů podle typu hráče a zohlední aktivitu hráče
     * v čase konání zápasu. Každému zápasu je přiřazeno pořadové číslo v sezóně.
     *
     * @param playerId identifikátor hráče
     * @return seznam nadcházejících zápasů jako MatchDTO
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
     * Vrací přehled všech již odehraných zápasů pro konkrétního hráče.
     *
     * Metoda:
     * - filtruje zápasy od založení hráče,
     * - zohlední aktivitu hráče v čase konání,
     * - načte registrace pro všechny vybrané zápasy,
     * - doplní PlayerMatchStatus pro daného hráče,
     * - přiřadí pořadová čísla zápasů v sezóně.
     *
     * @param playerId identifikátor hráče
     * @return seznam přehledových DTO odehraných zápasů
     */
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


    // PŘÍSTUP A DETAIL – PRIVÁTNÍ METODY


    /**
     * Ověřuje, zda má aktuálně přihlášený uživatel přístup k detailu zápasu.
     *
     * Logika:
     * - administrátor a manažer mají plný přístup,
     * - běžný uživatel má přístup pouze k zápasům aktuální sezóny,
     * - u budoucích zápasů musí mít alespoň jednoho aktivního hráče,
     * - u minulých zápasů musí mít hráče registrovaného na daný zápas.
     *
     * @param match entita zápasu
     * @param auth autentizační informace aktuálního uživatele
     * @throws AccessDeniedException pokud uživatel nemá oprávnění
     */
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

    /**
     * Sestavuje agregované statistiky registrací a přehled hráčů pro detail zápasu.
     *
     * Metoda:
     * - načte všechny registrace k zápasu,
     * - rozřadí hráče podle PlayerMatchStatus,
     * - spočítá počty hráčů v jednotlivých skupinách,
     * - sestaví seznamy hráčů pro jednotlivé statusy,
     * - spočítá jednotkovou cenu na registrovaného hráče,
     * - doplní informace o skóre, výsledku a vítězi.
     *
     * @param match entita zápasu
     * @param isAdminOrManager příznak, zda má uživatel zvýšená oprávnění
     * @return detail zápasu doplněný o statistiky a seznamy hráčů
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

        // === NOVÉ: mapování skóre, výsledku a vítěze pro detail ===
        if (match.getScore() != null) {
            dto.setScoreLight(match.getScore().getLight());
            dto.setScoreDark(match.getScore().getDark());
        } else {
            dto.setScoreLight(null);
            dto.setScoreDark(null);
        }
        dto.setResult(match.getResult());
        dto.setWinner(match.getWinner());
        // === KONEC NOVÉHO KÓDU ===

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

    /**
     * Vrací seznam registrovaných hráčů pro konkrétní tým.
     *
     * Registrace se filtrují podle statusu REGISTERED a daného týmu
     * a následně se načtou a převedou DTO jednotlivých hráčů.
     *
     * @param registrations seznam registrací k zápasu
     * @param team tým, pro který se hráči filtrují
     * @return seznam hráčů daného týmu ve stavu REGISTERED
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
     * Vyhodnocuje stav hráče v konkrétním zápase na základě přehledu MatchDetailDTO.
     *
     * Kadidátní status se určuje na základě toho, ve kterém seznamu hráčů
     * (registered, reserved, excused, substitute, unregistered, noExcused)
     * se daný hráč nachází. Pokud není nalezen v žádném seznamu, vrací se NO_RESPONSE.
     *
     * @param dto přehledový detail zápasu
     * @param playerId identifikátor hráče
     * @return status hráče v zápase
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
     * Ověřuje, zda je hráč v seznamu hráčů reprezentovaných pomocí PlayerDTO.
     *
     * @param players seznam hráčů
     * @param playerId identifikátor hráče
     * @return true, pokud je hráč v seznamu nalezen, jinak false
     */
    private boolean isIn(List<PlayerDTO> players, Long playerId) {
        return players != null
                && players.stream().anyMatch(p -> p.getId().equals(playerId));
    }


    // DTO MAPOVÁNÍ A HELPERY


    /**
     * Vytváří přehledové DTO MatchOverviewDTO z entitního zápasu bez kontextu hráče.
     *
     * Metoda doplní základní údaje o zápasu, spočítá počet registrovaných hráčů,
     * vypočítá jednotkovou cenu na registrovaného hráče a doplní informace
     * o stavu, důvodu zrušení, módu, skóre, výsledku a vítězi.
     *
     * @param match entita zápasu
     * @return přehledové DTO zápasu
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

        dto.setMatchStatus(match.getMatchStatus());
        dto.setCancelReason(match.getCancelReason());
        dto.setMatchMode(match.getMatchMode());

        if (match.getSeason() != null && match.getSeason().getId() != null) {
            dto.setSeasonId(match.getSeason().getId());
        }

        // mapování skóre, výsledku a vítěze pro overview
        if (match.getScore() != null) {
            dto.setScoreLight(match.getScore().getLight());
            dto.setScoreDark(match.getScore().getDark());
        } else {
            dto.setScoreLight(null);
            dto.setScoreDark(null);
        }
        dto.setResult(match.getResult());
        dto.setWinner(match.getWinner());

        return dto;
    }

    /**
     * Vytváří přehledové DTO MatchOverviewDTO z entitního zápasu v kontextu konkrétního hráče.
     *
     * Metoda volá základní toOverviewDTO a následně doplní PlayerMatchStatus
     * podle registrace daného hráče k zápasu.
     *
     * @param match entita zápasu
     * @param playerId identifikátor hráče
     * @return přehledové DTO zápasu s doplněným stavem hráče
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

    /**
     * Normalizuje stav hráče v zápase.
     *
     * Pokud je vstupní status null, vrací se NO_RESPONSE.
     * Pokud je status mezi očekávanými hodnotami, vrací se beze změny.
     * V ostatních případech se také vrací NO_RESPONSE.
     *
     * @param status původní status hráče
     * @return normalizovaný status hráče
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
     * Zjišťuje, zda má uživatel roli administrátora nebo manažera.
     *
     * @param auth autentizační informace aktuálního uživatele
     * @return true, pokud má uživatel roli ADMIN nebo MANAGER, jinak false
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
     * Vrací seznam nadcházejících zápasů aktuální sezóny.
     *
     * Používá aktuální čas ze zdroje Clock a identifikátor aktuální sezóny.
     *
     * @return seznam budoucích zápasů
     */
    private List<MatchEntity> findUpcomingMatchesForCurrentSeason() {
        return matchRepository.findBySeasonIdAndDateTimeAfterOrderByDateTimeAsc(
                getCurrentSeasonIdOrActive(),
                now()
        );
    }

    /**
     * Vrací seznam již odehraných zápasů aktuální sezóny.
     *
     * @return seznam minulých zápasů
     */
    private List<MatchEntity> findPastMatchesForCurrentSeason() {
        return matchRepository.findBySeasonIdAndDateTimeBeforeOrderByDateTimeDesc(
                getCurrentSeasonIdOrActive(),
                now()
        );
    }

    /**
     * Omezuje počet nadcházejících zápasů podle typu hráče.
     *
     * Typ hráče určuje maximální počet zápasů, které se zobrazí:
     * - VIP: maximálně tři zápasy,
     * - STANDARD: maximálně dva zápasy,
     * - BASIC: pouze první nadcházející zápas.
     *
     * @param upcomingAll seznam všech nadcházejících zápasů
     * @param type typ hráče
     * @return omezený seznam zápasů podle typu hráče
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
     * Vyhledá hráče podle identifikátoru nebo vyhodí výjimku, pokud neexistuje.
     *
     * @param playerId identifikátor hráče
     * @return entita hráče
     * @throws PlayerNotFoundException pokud hráč neexistuje
     */
    private PlayerEntity findPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    /**
     * Vyhledá zápas podle identifikátoru nebo vyhodí výjimku, pokud neexistuje.
     *
     * @param matchId identifikátor zápasu
     * @return entita zápasu
     * @throws MatchNotFoundException pokud zápas neexistuje
     */
    private MatchEntity findMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    /**
     * Ověřuje, zda je hráč aktivní v čase konání zápasu.
     *
     * Vyhodnocení aktivity je delegováno do PlayerInactivityPeriodService.
     *
     * @param player entita hráče
     * @param dateTime datum a čas konání zápasu
     * @return true, pokud je hráč aktivní, jinak false
     */
    private boolean isPlayerActiveForMatch(PlayerEntity player, LocalDateTime dateTime) {
        return playerInactivityPeriodService.isActive(player, dateTime);
    }

    /**
     * Vrací identifikátor aktuální sezóny nebo aktivní sezóny.
     *
     * Metoda nejprve zkouší získat ID aktuální sezóny z CurrentSeasonService.
     * Pokud není dostupné, použije se aktivní sezóna ze SeasonService.
     *
     * @return identifikátor aktuální nebo aktivní sezóny
     */
    private Long getCurrentSeasonIdOrActive() {
        Long id = currentSeasonService.getCurrentSeasonIdOrDefault();
        if (id != null) {
            return id;
        }
        return seasonService.getActiveSeason().getId();
    }

    /**
     * Přiřazuje zápasům jejich pořadové číslo v sezóně.
     *
     * Metoda zmapuje seznam entitních zápasů na seznam DTO typu NumberedMatchDTO
     * a do každého DTO doplní číslo zápasu podle mapy matchNumberMap.
     *
     * @param matches seznam entit zápasů
     * @param mapper funkce pro převod MatchEntity na DTO
     * @param matchNumberMap mapa id zápasu na jeho pořadové číslo v sezóně
     * @param <D> typ DTO implementující NumberedMatchDTO
     * @return seznam DTO s doplněnými pořadovými čísly
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
     * Vytváří mapu přiřazující každému zápasu v sezóně jeho pořadové číslo.
     *
     * Zápasy jsou seřazeny podle data a času konání a číslovány od jedné.
     *
     * @param seasonId identifikátor sezóny
     * @return mapa id zápasu na pořadové číslo v sezóně
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
     * Vrací aktuální datum a čas podle zadaného zdroje Clock.
     *
     * Metoda usnadňuje testování, protože Clock lze v testech nahradit.
     *
     * @return aktuální datum a čas
     */
    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}