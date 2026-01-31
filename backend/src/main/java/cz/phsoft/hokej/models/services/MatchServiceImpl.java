package cz.phsoft.hokej.models.services;


import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.*;
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


import java.time.LocalDateTime;
import java.util.*;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service vrstva pro práci se zápasy.
 * <p>
 * Odpovědnosti:
 * <ul>
 *     <li>CRUD nad zápasy v rámci aktivní sezóny,</li>
 *     <li>filtrování zápasů podle data a typu hráče (VIP/STANDARD/BASIC),</li>
 *     <li>výpočet detailních statistik k zápasu (MatchDetailDTO),</li>
 *     <li>přístupová logika k detailu zápasu (hráč vs admin/manager),</li>
 *     <li>přehledy zápasů pro konkrétního hráče (overview),</li>
 *     <li>rušení a obnovení zápasů (CANCELLED / uncancel).</li>
 * </ul>
 * <p>
 * Tato service:
 * <ul>
 *     <li>neřeší registrace (detailní stav hráče) – to řeší {@link MatchRegistrationService},</li>
 *     <li>neřeší výběr aktuálního hráče – to řeší {@link CurrentPlayerService} a controller.</li>
 * </ul>
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
                            NotificationService notificationService) {
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
    }

    // ======================
    // ZÁKLADNÍ SEZNAMY ZÁPASŮ
    // ======================

    /**
     * Vrátí všechny zápasy v rámci aktivní sezóny
     * seřazené podle data vzestupně.
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
     * Vrátí všechny nadcházející zápasy v aktivní sezóně
     * (datum > aktuální čas), seřazené podle data vzestupně.
     */
    @Override
    public List<MatchDTO> getUpcomingMatches() {
        Long seasonId = getCurrentSeasonIdOrActive();
        List<MatchEntity> upcomingMatches = findUpcomingMatchesForCurrentSeason();

        Map<Long, Integer> matchNumberMap = buildMatchNumberMapForSeason(seasonId);
        return assignMatchNumbers(upcomingMatches, matchMapper::toDTO, matchNumberMap);
    }

    /**
     * Vrátí všechny již proběhlé zápasy v aktivní sezóně
     * (datum < aktuální čas), seřazené podle data sestupně
     * (nejnovější první).
     */
    @Override
    public List<MatchDTO> getPastMatches() {
        Long seasonId = getCurrentSeasonIdOrActive();
        List<MatchEntity> pastMatches = findPastMatchesForCurrentSeason();

        Map<Long, Integer> matchNumberMap = buildMatchNumberMapForSeason(seasonId);
        return assignMatchNumbers(pastMatches, matchMapper::toDTO, matchNumberMap);
    }

    /**
     * Vrátí nejbližší nadcházející zápas v aktivní sezóně
     * nebo {@code null}, pokud žádný neexistuje.
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
     * Vrátí konkrétní zápas podle ID.
     *
     * @throws MatchNotFoundException pokud zápas neexistuje
     */
    @Override
    public MatchDTO getMatchById(Long id) {
        return matchMapper.toDTO(findMatchOrThrow(id));
    }

    /**
     * Vytvoří nový zápas.
     * <ul>
     *     <li>namapuje DTO na entitu,</li>
     *     <li>ověří, že datum zápasu spadá do aktivní sezóny,</li>
     *     <li>přiřadí aktivní sezónu,</li>
     *     <li>uloží zápas a vrátí DTO.</li>
     * </ul>
     */
    @Override
    public MatchDTO createMatch(MatchDTO dto) {
        MatchEntity entity = matchMapper.toEntity(dto);
        validateMatchDateInActiveSeason(entity.getDateTime());

        entity.setSeason(seasonService.getActiveSeason());

        return matchMapper.toDTO(matchRepository.save(entity));
    }

    /**
     * Aktualizuje existující zápas.
     * <ul>
     *     <li>ověří, že zápas patří do aktivní sezóny,</li>
     *     <li>přenese změny z DTO do entity,</li>
     *     <li>znovu zvaliduje datum v rámci aktivní sezóny,</li>
     *     <li>uloží změny,</li>
     *     <li>pokud se změnil maxPlayers, přepočítá REGISTERED/RESERVED.</li>
     * </ul>
     *
     * @throws InvalidMatchStatusException pokud zápas nepatří do aktivní sezóny
     */
    @Override
    public MatchDTO updateMatch(Long id, MatchDTO dto) {
        MatchEntity entity = findMatchOrThrow(id);

        // zjistíme roli uživatele
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrManager = hasAdminOrManagerRole(auth);

        // Kontrola sezóny jen pro "běžné" uživatele (kdyby se tahle metoda někdy použila i pro ně)
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

        // validace data v aktivní sezóně jen pro ne-adminy
        if (!isAdminOrManager) {
            validateMatchDateInActiveSeason(entity.getDateTime());
        }

        MatchEntity saved = matchRepository.save(entity);
        //pokud se změnil maxPlayers, přepočet REGISTERED/RESERVED
        if (saved.getMaxPlayers() != oldMaxPlayers) {
            registrationService.recalcStatusesForMatch(saved.getId());
        }
        // pokud se změnilo datum nebo čas - MATCH_TIME_CHANGED
        if (saved.getDateTime().isBefore(LocalDateTime.now())) {
            throw new InvalidMatchDateTimeException("Zápas by již byl minulostí");
        }
        MatchStatus matchStatus = saved.getMatchStatus();
        boolean dateTimeChanged = !saved.getDateTime().isEqual(oldDateTime);

        if (dateTimeChanged) {
            // vytvoří context se starým datem/časem
            MatchTimeChangeContext ctx = new MatchTimeChangeContext(saved, oldDateTime);
            notifyPlayersAboutMatchChanges(ctx, MatchStatus.UPDATED);
        }

        return matchMapper.toDTO(saved);
    }

    /**
     * Smaže zápas podle ID.
     * <p>
     * Pokud zápas neexistuje, vyhodí {@link MatchNotFoundException}.
     *
     * @return {@link SuccessResponseDTO} s potvrzením smazání
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
     * <ul>
     *     <li>nastaví MatchStatus.CANCELLED,</li>
     *     <li>uloží důvod zrušení,</li>
     *     <li>pokud je již zrušen, vyhodí InvalidMatchStatusException.</li>
     * </ul>
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
     * <ul>
     *     <li>MatchStatus nastaví na null,</li>
     *     <li>cancelReason nastaví na null,</li>
     *     <li>pokud zápas nebyl zrušen, vyhodí InvalidMatchStatusException.</li>
     * </ul>
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

        return new SuccessResponseDTO(
                "BE - Zápas " + match.getId() + match.getDateTime() + " byl úspěšně obnoven",
                match.getId(),
                LocalDateTime.now().toString()
        );
    }

    // ======================
    // POMOCNÉ METODY – ENTITY
    // ======================


    // ======================
    // DETAIL ZÁPASU
    // ======================

    /**
     * Vrátí detail zápasu (MatchDetailDTO) včetně:
     * <ul>
     *     <li>seskupení hráčů podle statusu (REGISTERED/RESERVED/EXCUSED/...),</li>
     *     <li>počtů hráčů v zápase, mimo zápas, náhradníků, NO_EXCUSED, NO_RESPONSE,</li>
     *     <li>ceny na registrovaného hráče,</li>
     *     <li>stavu zápasu (MatchStatus + důvod zrušení),</li>
     *     <li>stavu aktuálního hráče (PlayerMatchStatus) a jeho omluvy.</li>
     * </ul>
     * <p>
     * Obsahuje i přístupovou logiku:
     * <ul>
     *     <li>ADMIN/MANAGER vidí vždy,</li>
     *     <li>běžný uživatel:
     *          <ul>
     *              <li>nadcházející zápas → musí mít aktivního hráče k datu zápasu,</li>
     *              <li>proběhlý zápas → některý jeho hráč musel být REGISTERED.</li>
     *          </ul>
     *     </li>
     * </ul>
     */
    @Override
    public MatchDetailDTO getMatchDetail(Long id) {
        MatchEntity match = findMatchOrThrow(id);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrManager = hasAdminOrManagerRole(auth);

        // 1) přístupová logika
        checkAccessForPlayer(match, auth);

        // 2) sběr statistik a stavů hráčů
        MatchDetailDTO dto = collectPlayerStatus(match, isAdminOrManager);

        // 3) stav aktuálního hráče (pokud je zvolen)
        Long currentPlayerId = null;
        try {
            currentPlayerId = currentPlayerService.getCurrentPlayerId();
        } catch (Exception e) {
            logger.debug("Nebyl nalezen currentPlayerId pro match detail {}", id);
        }

        PlayerMatchStatus playerMatchStatus = resolveStatusForPlayer(dto, currentPlayerId);
        dto.setPlayerMatchStatus(playerMatchStatus);

        // 4) omluva aktuálního hráče (pokud existuje registrace)
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

        // 5) stav zápasu
        dto.setMatchStatus(match.getMatchStatus());
        dto.setCancelReason(match.getCancelReason());

        // 6) číslo zápasu v sezóně podle globálního pořadí v sezóně   
        if (match.getSeason() != null && match.getSeason().getId() != null) {
            Long seasonId = match.getSeason().getId();
            Map<Long, Integer> matchNumberMap = buildMatchNumberMapForSeason(seasonId);
            Integer number = matchNumberMap.get(match.getId());
            dto.setMatchNumber(number);
        }

        return dto;
    }

    // --------------------------------------------------
    // Přístupová logika – kdo smí vidět detail zápasu
    // --------------------------------------------------

    /**
     * Ověří, zda má aktuální uživatel přístup k detailu zápasu.
     * <ul>
     *     <li>nepřihlášený → AccessDenied,</li>
     *     <li>ADMIN/MANAGER → vždy povoleno,</li>
     *     <li>běžný uživatel:
     *         <ul>
     *             <li>získá své hráče (ownedPlayers) podle emailu,</li>
     *             <li>bez hráčů → AccessDenied,</li>
     *             <li>nadcházející zápas → musí mít aktivního hráče k datu zápasu,</li>
     *             <li>uplynulý zápas → jeho hráč musel být REGISTERED.</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @throws org.springframework.security.access.AccessDeniedException pokud uživatel nesplňuje podmínky
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
            // nadcházející zápas – hráč musí být aktivní pro datum zápasu
            boolean hasActivePlayerForMatch = ownedPlayers.stream()
                    .anyMatch(p -> isPlayerActiveForMatch(p, match.getDateTime()));

            if (!hasActivePlayerForMatch) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "BE - Nemáte aktivního hráče pro tento zápas."
                );
            }
            return;
        }

        // uplynulý zápas – hráč musí být mezi REGISTERED
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

    // --------------------------------------------------
    // Sběr statistik hráčů pro MatchDetailDTO
    // --------------------------------------------------

    /**
     * Sestaví {@link MatchDetailDTO} pro daný zápas:
     * <ul>
     *     <li>seskupí hráče podle statusu (REGISTERED/RESERVED/EXCUSED/...)</li>
     *     <li>spočítá počty hráčů v jednotlivých kategoriích,</li>
     *     <li>vypočítá cenu na registrovaného hráče,</li>
     *     <li>naplní seznamy hráčů k jednotlivým statusům,</li>
     *     <li>pole noResponsePlayers vyplní jen pro admin/manager.</li>
     * </ul>
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

        int inGamePlayers =
                statusToPlayersMap.getOrDefault(PlayerMatchStatus.REGISTERED, List.of()).size();


        registrations.forEach(r -> {
            System.out.println("Reg: status=" + r.getStatus()
                    + ", team=" + r.getTeam());
        });


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

        return dto;
    }

    /**
     * Z MatchDetailDTO odvodí status konkrétního hráče
     * na základě jeho členství v seznamu hráčů dle statusu.
     * Pokud není v žádném seznamu, vrací NO_RESPONSE.
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
     * Pomocná metoda – zjistí, zda je hráč s daným ID v seznamu PlayerDTO.
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
     * <ul>
     *     <li>vezme všechny zápasy (všechny sezóny),</li>
     *     <li>filtruje je podle aktivity hráče v daném datu
     *         (PlayerInactivityPeriodService.isActive).</li>
     * </ul>
     * <p>
     * POZOR: tady se zápasy tahají přes všechny sezóny, takže globální
     * "číslo v sezóně" nedává smysl – DTO se nečíslují.
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
     * Najde ID hráče podle emailu uživatele (User.email).
     *
     * @throws PlayerNotFoundException pokud uživatel nemá hráče
     */
    @Override
    public Long getPlayerIdByEmail(String email) {
        return playerRepository.findByUserEmail(email)
                .map(PlayerEntity::getId)
                .orElseThrow(() -> new PlayerNotFoundException(email));
    }

    /**
     * Vrátí přehled nadcházejících zápasů pro konkrétního hráče.
     * <ul>
     *     <li>zjistí PlayerType hráče,</li>
     *     <li>vezme všechny nadcházející zápasy v aktivní sezóně,</li>
     *     <li>omezí jejich počet podle PlayerType (VIP/STANDARD/BASIC),</li>
     *     <li>filtruje jen zápasy, kde je hráč aktivní,</li>
     *     <li>namapuje na MatchOverviewDTO včetně PlayerMatchStatus.</li>
     * </ul>
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
     * Vrátí seznam nadcházejících zápasů pro konkrétního hráče
     * v plném MatchDTO formátu, s omezením podle PlayerType.
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
     * Vrátí overview všech proběhlých zápasů aktivní sezóny,
     * kterých se hráč mohl účastnit.
     * <ul>
     *     <li>bere jen proběhlé zápasy v aktivní sezóně,</li>
     *     <li>filtruje podle aktivity hráče v datu zápasu,</li>
     *     <li>najednou načte všechny registrace k těmto zápasům,</li>
     *     <li>z nich sestaví mapu matchId → playerId → status,</li>
     *     <li>pro každý zápas sestaví MatchOverviewDTO a nastaví PlayerMatchStatus.</li>
     * </ul>
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

        // původní logika + globální číslo zápasu v sezóně
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



    private PlayerEntity findPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    private MatchEntity findMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    private MatchRegistrationEntity findMatchRegistrationOrThrow(Long playerId, Long matchId) {
        return matchRegistrationRepository.findByPlayerIdAndMatchId(playerId, matchId)
                .orElseThrow(() -> new MatchRegistrationNotFoundException(playerId, matchId));
    }

    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    // ======================
    // POMOCNÉ METODY – DTO MAPOVÁNÍ
    // ======================

    /**
     * Sestaví základní {@link MatchOverviewDTO} pro daný zápas
     * (bez ohledu na konkrétního hráče).
     * <ul>
     *     <li>vypočítá počet REGISTERED hráčů,</li>
     *     <li>vypočítá cenu na registrovaného hráče.</li>
     * </ul>
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
     * Rozšířená verze overview o stav konkrétního hráče v zápase.
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
     * Zjistí, zda má uživatel roli ADMIN nebo MANAGER.
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
     * Všechny nadcházející zápasy v aktivní sezóně (datum > teď),
     * seřazené podle data vzestupně.
     */
    private List<MatchEntity> findUpcomingMatchesForCurrentSeason() {
        return matchRepository.findBySeasonIdAndDateTimeAfterOrderByDateTimeAsc(
                getCurrentSeasonIdOrActive(),
                now()
        );
    }

    /**
     * Všechny proběhlé zápasy v aktivní sezóně (datum < teď),
     * seřazené podle data sestupně.
     */
    private List<MatchEntity> findPastMatchesForCurrentSeason() {
        return matchRepository.findBySeasonIdAndDateTimeBeforeOrderByDateTimeDesc(
                getCurrentSeasonIdOrActive(),
                now()
        );
    }

    /**
     * Omezení seznamu nadcházejících zápasů podle typu hráče.
     * <ul>
     *     <li>VIP → max 3 zápasy,</li>
     *     <li>STANDARD → max 2 zápasy,</li>
     *     <li>BASIC → pouze nejbližší zápas.</li>
     * </ul>
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
     * Ověří, zda je hráč aktivní pro dané datum zápasu,
     * pomocí {@link PlayerInactivityPeriodService}.
     */
    private boolean isPlayerActiveForMatch(PlayerEntity player, LocalDateTime dateTime) {
        return playerInactivityPeriodService.isActive(player, dateTime);
    }

    /**
     * Normalizuje status hráče.
     * <ul>
     *     <li>null → NO_RESPONSE,</li>
     *     <li>jinak vrací status, pokud je z podporovaného seznamu.</li>
     * </ul>
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
     * Ověří, že datum zápasu spadá do intervalu aktivní sezóny,
     * jinak vyhodí {@link InvalidSeasonPeriodDateException}.
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
     * Sezona pro uživatele
     */
    private Long getCurrentSeasonIdOrActive() {
        Long id = currentSeasonService.getCurrentSeasonIdOrDefault();
        if (id != null) {
            return id;
        }
        // fallback – kdyby náhodou nebyla v session ani globálně aktivní
        return seasonService.getActiveSeason().getId();
    }

    /**
     * Generická metoda pro číslování zápasů:
     * číslo se bere z mapy matchId -> pořadí v sezóně.
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
     * Pro danou sezónu vrátí mapu:
     * matchId -> pořadové číslo zápasu v sezóně (1..N)
     * Pořadí je podle dateTime ASC.
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


    private void notifyPlayersAboutMatchChanges(Object context, MatchStatus matchStatus) {
        // z contextu vytáhneme MatchEntity
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
                        // tady chceme vidět i staré datum -> musíme poslat celý context
                        notificationService.notifyPlayer(
                                player,
                                NotificationType.MATCH_TIME_CHANGED,
                                context // MatchTimeChangeContext
                        );
                    }

                    if (matchStatus == MatchStatus.CANCELLED) {
                        notificationService.notifyPlayer(
                                player,
                                NotificationType.MATCH_CANCELED,
                                match // stačí MatchEntity
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


}