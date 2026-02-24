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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import cz.phsoft.hokej.data.entities.AppUserEntity;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

//TODO KOMENTÁŘE - PROSTÉ
/**
 * Implementace service vrstvy, která se používá pro práci se zápasy.
 *
 * V této třídě se zajišťují CRUD operace nad zápasy v rámci sezón, filtrování nadcházejících a proběhlých zápasů,
 * sestavení detailu zápasu včetně agregovaných statistik a zpracování změn stavu zápasu, jako je zrušení nebo obnova.
 *
 * Součástí odpovědnosti je také doplnění číslování zápasů v sezóně a spouštění notifikací hráčům při vybraných změnách,
 * zejména při změně termínu, zrušení nebo obnovení zápasu. Notifikace se delegují do {@link NotificationService}.
 *
 * Třída neřeší detailní stavové přechody registrací hráčů na zápasy, které jsou spravovány v {@link MatchRegistrationService}.
 * Výběr aktuálního hráče se zajišťuje ve {@link CurrentPlayerService} a v controller vrstvě.
 */
@Service
public class MatchServiceImpl implements MatchService {

    @Value("${app.demo-mode:false}")
    private boolean isDemoMode;

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
     * Vrací všechny zápasy v rámci aktuálně používané sezóny.
     *
     * Zápasy se načítají z repository vrstvy, seřadí se podle data a času vzestupně a následně se doplní pořadové číslo
     * zápasu v sezóně. Mapování entit na DTO se deleguje do {@link MatchMapper}.
     *
     * @return Seznam zápasů aktuální sezóny převedených do DTO včetně pořadového čísla.
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
     * Vrací všechny nadcházející zápasy v aktuální sezóně.
     *
     * Zápasy se filtrují na základě data a času většího než aktuální okamžik, seřadí se podle data vzestupně
     * a doplní se pořadové číslo zápasu v sezóně.
     *
     * @return Seznam nadcházejících zápasů převedených do DTO včetně pořadového čísla.
     */
    @Override
    public List<MatchDTO> getUpcomingMatches() {
        Long seasonId = getCurrentSeasonIdOrActive();
        List<MatchEntity> upcomingMatches = findUpcomingMatchesForCurrentSeason();

        Map<Long, Integer> matchNumberMap = buildMatchNumberMapForSeason(seasonId);
        return assignMatchNumbers(upcomingMatches, matchMapper::toDTO, matchNumberMap);
    }

    /**
     * Vrací všechny proběhlé zápasy v aktuální sezóně.
     *
     * Zápasy se filtrují na základě data a času menšího než aktuální okamžik, seřadí se podle data sestupně
     * a doplní se pořadové číslo zápasu v sezóně.
     *
     * @return Seznam proběhlých zápasů převedených do DTO včetně pořadového čísla.
     */
    @Override
    public List<MatchDTO> getPastMatches() {
        Long seasonId = getCurrentSeasonIdOrActive();
        List<MatchEntity> pastMatches = findPastMatchesForCurrentSeason();

        Map<Long, Integer> matchNumberMap = buildMatchNumberMapForSeason(seasonId);
        return assignMatchNumbers(pastMatches, matchMapper::toDTO, matchNumberMap);
    }

    /**
     * Vrací nejbližší nadcházející zápas v aktuální sezóně.
     *
     * Pokud žádný nadcházející zápas neexistuje, vrací se null.
     *
     * @return Nejbližší nadcházející zápas převedený do DTO, nebo null pokud žádný neexistuje.
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
     * Vrací základní informace o zápasu podle identifikátoru.
     *
     * Zápas se načítá z repository vrstvy a mapuje se do DTO. Při neexistenci zápasu se vyhazuje
     * {@link MatchNotFoundException}.
     *
     * @param id Identifikátor zápasu.
     * @return Zápas převedený do {@link MatchDTO}.
     */
    @Override
    public MatchDTO getMatchById(Long id) {
        return matchMapper.toDTO(findMatchOrThrow(id));
    }

    /**
     * Vytváří nový zápas v aktivní sezóně.
     *
     * DTO se mapuje na entitu, ověří se, že datum zápasu spadá do období aktivní sezóny, a k zápasu se přiřadí
     * aktivní sezóna. Identifikátor autora vytvoření a poslední úpravy se nastaví na aktuálně přihlášeného uživatele,
     * pokud je dostupný. Uložení se provádí přes repository vrstvu.
     *
     * @param dto DTO obsahující data vytvářeného zápasu.
     * @return Vytvořený zápas převedený do DTO.
     */
    @Override
    public MatchDTO createMatch(MatchDTO dto) {
        MatchEntity entity = matchMapper.toEntity(dto);
        validateMatchDateInActiveSeason(entity.getDateTime());

        entity.setSeason(seasonService.getActiveSeason());

        Long currentUserId = getCurrentUserIdOrNull();
        entity.setCreatedByUserId(currentUserId);
        entity.setLastModifiedByUserId(currentUserId);

        return matchMapper.toDTO(matchRepository.save(entity));
    }

    /**
     * Aktualizuje existující zápas podle identifikátoru.
     *
     * Zápas se načte z repository vrstvy a vyhodnotí se oprávnění podle role uživatele. Uživatel bez role ADMIN
     * nebo MANAGER může upravovat pouze zápasy patřící do aktivní sezóny a současně se ověřuje, že datum zápasu
     * zůstává v období aktivní sezóny. Následně se přenesou změny z DTO do entity a zápas se uloží.
     *
     * Pokud se změní kapacita zápasu, přepočítají se stavy registrací přes {@link MatchRegistrationService}.
     * Pokud se změní termín zápasu, odešlou se notifikace o změně termínu přes {@link NotificationService}.
     * Při změně vybraných vlastností se nastaví stav zápasu na UPDATED.
     *
     * @param id Identifikátor upravovaného zápasu.
     * @param dto DTO obsahující nová data zápasu.
     * @return Aktualizovaný zápas převedený do DTO.
     * @throws InvalidMatchStatusException Pokud uživatel bez role ADMIN nebo MANAGER upravuje zápas mimo aktivní sezónu.
     * @throws InvalidMatchDateTimeException Pokud by po úpravě zápas spadl do minulosti.
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

        Integer oldMaxPlayers = entity.getMaxPlayers();
        LocalDateTime oldDateTime = entity.getDateTime();
        String oldLocation = entity.getLocation();
        Integer oldPrice = entity.getPrice();

        matchMapper.updateEntity(dto, entity);

        Long currentUserId = getCurrentUserIdOrNull();
        entity.setLastModifiedByUserId(currentUserId);

        if (!isAdminOrManager) {
            validateMatchDateInActiveSeason(entity.getDateTime());
        }

        if (entity.getDateTime() != null
                && entity.getDateTime().isBefore(LocalDateTime.now())) {
            throw new InvalidMatchDateTimeException("Zápas by již byl minulostí");
        }

        boolean maxPlayersChanged =
                !java.util.Objects.equals(entity.getMaxPlayers(), oldMaxPlayers);

        boolean dateTimeChanged =
                !java.util.Objects.equals(entity.getDateTime(), oldDateTime);

        boolean locationChanged =
                !java.util.Objects.equals(entity.getLocation(), oldLocation);

        boolean priceChanged =
                !java.util.Objects.equals(entity.getPrice(), oldPrice);

        if (maxPlayersChanged || dateTimeChanged || locationChanged || priceChanged) {
            entity.setMatchStatus(MatchStatus.UPDATED);
        }

        MatchEntity saved = matchRepository.save(entity);

        if (maxPlayersChanged) {
            registrationService.recalcStatusesForMatch(saved.getId());
        }

        if (dateTimeChanged) {
            MatchTimeChangeContext ctx = new MatchTimeChangeContext(saved, oldDateTime);
            notifyPlayersAboutMatchChanges(ctx, MatchStatus.UPDATED);
        }

        return matchMapper.toDTO(saved);
    }

    /**
     * Odstraňuje zápas podle identifikátoru.
     *
     * Zápas se načítá z repository vrstvy a při neexistenci se vyhazuje {@link MatchNotFoundException}.
     * V demo režimu se operace blokuje a vyhazuje se {@link DemoModeOperationNotAllowedException}.
     * Při úspěšném odstranění se vrací standardizovaná odpověď s potvrzením a časovou známkou.
     *
     * @param id Identifikátor odstraňovaného zápasu.
     * @return Standardizovaná odpověď o úspěšném odstranění.
     */
    @Override
    public SuccessResponseDTO deleteMatch(Long id) {
        MatchEntity match = findMatchOrThrow(id);

        if (isDemoMode) {
            throw new DemoModeOperationNotAllowedException(
                    "Zápas nebude odstraněn. Aplikace běží v DEMO režimu."
            );
        }

        matchRepository.delete(match);

        return new SuccessResponseDTO(
                "BE - Zápas " + match.getId() + match.getDateTime() + " byl úspěšně smazán",
                id,
                LocalDateTime.now().toString()
        );
    }

    /**
     * Ruší zápas a ukládá důvod zrušení.
     *
     * Pokud je zápas již zrušen, vyhazuje se {@link InvalidMatchStatusException}. Při úspěšném zrušení se nastaví stav
     * CANCELED, uloží se důvod zrušení a aktualizuje se identifikátor uživatele, který změnu provedl. Následně se odešlou
     * notifikace registrovaným hráčům o zrušení zápasu přes {@link NotificationService}.
     *
     * @param matchId Identifikátor rušeného zápasu.
     * @param reason Důvod zrušení zápasu.
     * @return Standardizovaná odpověď o úspěšném zrušení.
     */
    @Override
    @Transactional
    public SuccessResponseDTO cancelMatch(Long matchId, MatchCancelReason reason) {
        MatchEntity match = findMatchOrThrow(matchId);
        String message = " je již zrušen";

        if (match.getMatchStatus() == MatchStatus.CANCELED) {
            throw new InvalidMatchStatusException(matchId, message);
        }

        match.setMatchStatus(MatchStatus.CANCELED);
        match.setCancelReason(reason);

        Long currentUserId = getCurrentUserIdOrNull();
        match.setLastModifiedByUserId(currentUserId);

        MatchEntity saved = matchRepository.save(match);
        notifyPlayersAboutMatchChanges(saved, MatchStatus.CANCELED);

        return new SuccessResponseDTO(
                "BE - Zápas " + match.getId() + match.getDateTime() + " byl úspěšně zrušen",
                match.getId(),
                LocalDateTime.now().toString()
        );
    }

    /**
     * Obnovuje dříve zrušený zápas.
     *
     * Pokud zápas není ve stavu CANCELED, vyhazuje se {@link InvalidMatchStatusException}. Při úspěšné obnově se odstraní
     * důvod zrušení, nastaví se stav UNCANCELED a aktualizuje se identifikátor uživatele, který změnu provedl. Následně se
     * odešlou notifikace registrovaným hráčům o obnovení zápasu přes {@link NotificationService}.
     *
     * @param matchId Identifikátor obnovovaného zápasu.
     * @return Standardizovaná odpověď o úspěšné obnově.
     */
    @Override
    @Transactional
    public SuccessResponseDTO unCancelMatch(Long matchId) {
        MatchEntity match = findMatchOrThrow(matchId);
        String message = " ještě nebyl zrušen";

        if (match.getMatchStatus() != MatchStatus.CANCELED) {
            throw new InvalidMatchStatusException(matchId, message);
        }

        match.setMatchStatus(MatchStatus.UNCANCELED);
        match.setCancelReason(null);

        Long currentUserId = getCurrentUserIdOrNull();
        match.setLastModifiedByUserId(currentUserId);

        MatchEntity saved = matchRepository.save(match);
        notifyPlayersAboutMatchChanges(saved, MatchStatus.UNCANCELED);

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
     * Vrací detail zápasu ve formě {@link MatchDetailDTO}.
     *
     * Nejprve se načte zápas a vyhodnotí se přístupová pravidla dle role uživatele a jeho navázaných hráčů.
     * Následně se sestaví detailní DTO včetně agregovaných statistik a seskupení hráčů podle stavů registrací.
     * Do výsledku se doplní stav aktuálně zvoleného hráče, případné informace o omluvě, stav zápasu a důvod zrušení.
     * Pokud je zápas součástí sezóny, doplní se také pořadové číslo zápasu v sezóně.
     *
     * @param id Identifikátor zápasu.
     * @return Detail zápasu ve formě {@link MatchDetailDTO}.
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
     * Ověřuje, zda má aktuální uživatel přístup k detailu zápasu.
     *
     * Uživatel musí být přihlášen. Uživatel s rolí ADMIN nebo MANAGER má přístup vždy.
     * Uživatel bez administrátorské role musí mít navázaného alespoň jednoho hráče a současně platí omezení podle toho,
     * zda jde o nadcházející nebo proběhlý zápas v rámci aktuální sezóny.
     *
     * Pro nadcházející zápas se ověřuje, že uživatel má alespoň jednoho aktivního hráče pro termín zápasu.
     * Pro proběhlý zápas se ověřuje, že některý z hráčů uživatele byl v zápase registrován ve stavu REGISTERED.
     *
     * @param match Zápas, ke kterému se přístup vyhodnocuje.
     * @param auth Aktuální autentizace z bezpečnostního kontextu.
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
     * Sestavuje {@link MatchDetailDTO} pro daný zápas.
     *
     * V rámci sestavení se načtou registrace k zápasu, hráči se seskupí podle stavů registrací a spočítají se agregované
     * hodnoty, zejména počty hráčů v jednotlivých stavech, volná místa a cena na registrovaného hráče.
     *
     * Seznam hráčů bez reakce se do výsledku doplňuje pouze pro role ADMIN a MANAGER.
     *
     * @param match Zápas, pro který se detail sestavuje.
     * @param isAdminOrManager Příznak určující, zda má uživatel oprávnění pro administrátorský pohled.
     * @return Detail zápasu sestavený do DTO.
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
     * Sestavuje seznam hráčů registrovaných do konkrétního týmu.
     *
     * Seznam se sestavuje pouze ze stavu REGISTERED a používá se pro rychlé zobrazení složení týmů v detailu zápasu.
     * Načítání hráčů se provádí přes repository vrstvu a převod do DTO se deleguje do {@link PlayerMapper}.
     *
     * @param registrations Registrace zápasu, ze kterých se hráči vyhodnocují.
     * @param team Tým, pro který se hráči filtrují.
     * @return Seznam hráčů registrovaných v daném týmu převedených do DTO.
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
     * Odvozuje stav konkrétního hráče z detailu zápasu.
     *
     * Stav se vyhodnocuje na základě přítomnosti hráče v seznamech seskupených podle stavu registrace.
     * Pokud hráč není nalezen v žádné kategorii, vrací se NO_RESPONSE.
     *
     * @param dto Detail zápasu obsahující seskupené seznamy hráčů.
     * @param playerId Identifikátor hráče, pro kterého se stav vyhodnocuje.
     * @return Stav hráče vůči zápasu.
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
     * Vyhodnocuje přítomnost hráče v seznamu hráčů podle identifikátoru.
     *
     * @param players Seznam hráčů, ve kterém se vyhledává.
     * @param playerId Identifikátor hráče, který se v seznamu ověřuje.
     * @return True, pokud je hráč v seznamu přítomen, jinak false.
     */
    private boolean isIn(List<PlayerDTO> players, Long playerId) {
        return players != null
                && players.stream().anyMatch(p -> p.getId().equals(playerId));
    }

    // ======================
    // DALŠÍ PUBLIC METODY
    // ======================

    /**
     * Vrací zápasy, ve kterých může daný hráč potenciálně hrát.
     *
     * Zápasy se načítají napříč všemi sezónami a následně se filtrují podle aktivity hráče v termínu zápasu.
     * Vyhodnocení aktivity se deleguje do {@link PlayerInactivityPeriodService}.
     *
     * @param playerId Identifikátor hráče, pro kterého se dostupné zápasy vyhodnocují.
     * @return Seznam zápasů, pro které je hráč v termínu aktivní, převedených do DTO.
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
     * Vyhledává identifikátor hráče podle e-mailu uživatele.
     *
     * Hráč se načítá přes repository vrstvu. Pokud uživatel nemá žádného hráče, vyhazuje se {@link PlayerNotFoundException}.
     *
     * @param email E-mail uživatele, pro který se hráč vyhledává.
     * @return Identifikátor hráče navázaného na uživatele.
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
     * Nadcházející zápasy se načtou pro aktuální sezónu a omezí se podle typu hráče. Následně se filtrují pouze ty zápasy,
     * pro které je hráč v termínu aktivní. Do výstupu se doplní stav hráče v zápase a pořadové číslo zápasu v sezóně.
     *
     * @param playerId Identifikátor hráče, pro kterého se přehled sestavuje.
     * @return Seznam přehledů zápasů převedených do {@link MatchOverviewDTO}.
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
     * Vrací seznam nadcházejících zápasů pro konkrétního hráče.
     *
     * Nadcházející zápasy se načtou pro aktuální sezónu, omezí se podle typu hráče a následně se filtrují pouze ty zápasy,
     * pro které je hráč v termínu aktivní. Do výstupu se doplní pořadové číslo zápasu v sezóně.
     *
     * @param playerId Identifikátor hráče, pro kterého se zápasy načítají.
     * @return Seznam nadcházejících zápasů převedených do {@link MatchDTO}.
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
     * Vrací přehled proběhlých zápasů aktuální sezóny, kterých se hráč mohl účastnit.
     *
     * Zápasy se filtrují podle data vytvoření hráče a podle aktivity hráče v termínu zápasu. Pro vybranou sadu zápasů
     * se hromadně načtou registrace a pro každý zápas se odvodí stav hráče. Do výstupu se doplní pořadové číslo zápasu
     * v sezóně.
     *
     * @param playerId Identifikátor hráče, pro kterého se přehled sestavuje.
     * @return Seznam přehledů proběhlých zápasů převedených do {@link MatchOverviewDTO}.
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

    /**
     * Načítá hráče podle identifikátoru nebo vyhazuje výjimku při neexistenci.
     *
     * @param playerId Identifikátor hráče.
     * @return Načtená entita hráče.
     */
    private PlayerEntity findPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    /**
     * Načítá zápas podle identifikátoru nebo vyhazuje výjimku při neexistenci.
     *
     * @param matchId Identifikátor zápasu.
     * @return Načtená entita zápasu.
     */
    private MatchEntity findMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    /**
     * Načítá registraci hráče k zápasu nebo vyhazuje výjimku při neexistenci.
     *
     * @param playerId Identifikátor hráče.
     * @param matchId Identifikátor zápasu.
     * @return Načtená entita registrace.
     */
    private MatchRegistrationEntity findMatchRegistrationOrThrow(Long playerId, Long matchId) {
        return matchRegistrationRepository.findByPlayerIdAndMatchId(playerId, matchId)
                .orElseThrow(() -> new MatchRegistrationNotFoundException(playerId, matchId));
    }

    /**
     * Vrací aktuální čas.
     *
     * Metoda se používá pro sjednocení přístupu k času a pro usnadnění testování.
     *
     * @return Aktuální čas jako LocalDateTime.
     */
    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    // ======================
    // POMOCNÉ METODY – DTO MAPOVÁNÍ
    // ======================

    /**
     * Sestavuje základní {@link MatchOverviewDTO} pro daný zápas.
     *
     * DTO obsahuje základní informace o zápasu a počet hráčů se stavem REGISTERED včetně vypočtené ceny na registrovaného
     * hráče. Načtení registrací se deleguje do {@link MatchRegistrationService}.
     *
     * @param match Zápas, pro který se přehled sestavuje.
     * @return Přehled zápasu převedený do {@link MatchOverviewDTO}.
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

        // doplnění stavu zápasu a sezóny pro přehled
        dto.setMatchStatus(match.getMatchStatus());
        dto.setCancelReason(match.getCancelReason());
        if (match.getSeason() != null && match.getSeason().getId() != null) {
            dto.setSeasonId(match.getSeason().getId());
        }

        return dto;
    }

    /**
     * Sestavuje {@link MatchOverviewDTO} pro daný zápas v kontextu konkrétního hráče.
     *
     * K základním údajům o zápasu se doplní stav hráče v zápase odvozený z registrací. Pokud registrace hráče neexistuje,
     * použije se stav NO_RESPONSE.
     *
     * @param match Zápas, pro který se přehled sestavuje.
     * @param playerId Identifikátor hráče, jehož stav se do přehledu doplňuje.
     * @return Přehled zápasu převedený do {@link MatchOverviewDTO} včetně stavu hráče.
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
     * Vyhodnocuje, zda má uživatel roli ADMIN nebo MANAGER.
     *
     * @param auth Aktuální autentizace z bezpečnostního kontextu.
     * @return True, pokud má uživatel jednu z administrátorských rolí, jinak false.
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
     * Načítá všechny nadcházející zápasy v aktuální sezóně.
     *
     * Zápasy se vybírají podle data a času většího než aktuální okamžik a řadí se vzestupně podle data.
     *
     * @return Seznam nadcházejících zápasů v aktuální sezóně.
     */
    private List<MatchEntity> findUpcomingMatchesForCurrentSeason() {
        return matchRepository.findBySeasonIdAndDateTimeAfterOrderByDateTimeAsc(
                getCurrentSeasonIdOrActive(),
                now()
        );
    }

    /**
     * Načítá všechny proběhlé zápasy v aktuální sezóně.
     *
     * Zápasy se vybírají podle data a času menšího než aktuální okamžik a řadí se sestupně podle data.
     *
     * @return Seznam proběhlých zápasů v aktuální sezóně.
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
     * Pro typ VIP se vrací tři nejbližší zápasy, pro STANDARD dva nejbližší zápasy a pro BASIC jeden nejbližší zápas.
     *
     * @param upcomingAll Seznam všech nadcházejících zápasů.
     * @param type Typ hráče.
     * @return Omezený seznam zápasů podle pravidel typu hráče.
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
     * Vyhodnocuje, zda je hráč aktivní pro termín zápasu.
     *
     * Vyhodnocení aktivity se deleguje do {@link PlayerInactivityPeriodService}.
     *
     * @param player Hráč, jehož aktivita se vyhodnocuje.
     * @param dateTime Termín zápasu.
     * @return True, pokud je hráč v termínu zápasu aktivní, jinak false.
     */
    private boolean isPlayerActiveForMatch(PlayerEntity player, LocalDateTime dateTime) {
        return playerInactivityPeriodService.isActive(player, dateTime);
    }

    /**
     * Normalizuje stav registrace hráče pro použití v přehledech.
     *
     * Pokud je stav null nebo je neznámý, vrací se NO_RESPONSE. Podporované stavy se vracejí beze změny.
     *
     * @param status Stav registrace hráče.
     * @return Normalizovaný stav registrace pro použití v přehledech.
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
     * Ověřuje, že datum zápasu spadá do období aktivní sezóny.
     *
     * Kontrola se používá při vytváření a úpravě zápasu pro uživatele bez administrátorské role. Při porušení období
     * aktivní sezóny se vyhazuje {@link InvalidSeasonPeriodDateException}.
     *
     * @param dateTime Termín zápasu, který se ověřuje.
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
     * Vrací identifikátor sezóny, která se použije pro práci se zápasy.
     *
     * Primárně se používá sezóna uložená v {@link CurrentSeasonService}. Pokud není k dispozici, použije se globálně
     * aktivní sezóna ze {@link SeasonService}.
     *
     * @return Identifikátor aktuální nebo aktivní sezóny.
     */
    private Long getCurrentSeasonIdOrActive() {
        Long id = currentSeasonService.getCurrentSeasonIdOrDefault();
        if (id != null) {
            return id;
        }
        return seasonService.getActiveSeason().getId();
    }

    /**
     * Doplňuje pořadové číslo zápasu v sezóně do DTO výstupu.
     *
     * Pořadové číslo se doplňuje na základě mapy matchId na pořadí v sezóně. Mapování entity na DTO se předává
     * jako funkce, aby bylo možné metodu použít pro různé DTO implementující {@link NumberedMatchDTO}.
     *
     * @param matches Seznam zápasů, které se mapují do DTO.
     * @param mapper Funkce pro mapování {@link MatchEntity} na DTO.
     * @param matchNumberMap Mapa matchId na pořadové číslo v sezóně.
     * @return Seznam DTO s doplněným pořadovým číslem.
     * @param <D> Typ DTO implementující {@link NumberedMatchDTO}.
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
     * Sestavuje mapu matchId na pořadové číslo zápasu v sezóně.
     *
     * Pořadí zápasů se odvozuje z data a času zápasu řazeného vzestupně v rámci sezóny.
     *
     * @param seasonId Identifikátor sezóny, pro kterou se pořadí sestavuje.
     * @return Mapa matchId na pořadové číslo zápasu v sezóně.
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
     * Odesílá notifikace hráčům o změnách souvisejících se zápasem.
     *
     * Kontext představuje buď samotný zápas, nebo {@link MatchTimeChangeContext} v případě změny termínu. Na základě
     * stavu změny zápasu se určí typ notifikace a notifikace se odesílají hráčům s registrací ve stavu REGISTERED,
     * RESERVED nebo SUBSTITUTE. Odeslání se deleguje do {@link NotificationService}.
     *
     * @param context Kontext notifikace použitý pro sestavení obsahu zprávy.
     * @param matchStatus Stav změny zápasu, podle kterého se určuje typ notifikace.
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
                        || reg.getStatus() == PlayerMatchStatus.RESERVED
                        || reg.getStatus() == PlayerMatchStatus.SUBSTITUTE)
                .forEach(reg -> {
                    PlayerEntity player = reg.getPlayer();

                    if (matchStatus == MatchStatus.UPDATED) {
                        notificationService.notifyPlayer(
                                player,
                                NotificationType.MATCH_TIME_CHANGED,
                                context
                        );
                    }

                    if (matchStatus == MatchStatus.CANCELED) {
                        notificationService.notifyPlayer(
                                player,
                                NotificationType.MATCH_CANCELED,
                                match
                        );
                        logger.info("CANCEL notify: matchId={}, regs={}",
                                match.getId(),
                                registrations.stream().map(r -> r.getStatus().name()).toList()
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
     * Vrací identifikátor aktuálně přihlášeného uživatele, pokud je dostupný.
     *
     * Identifikátor se odvozuje z aktuální autentizace. Jako klíč se používá e-mail uživatele a dohledání se provádí
     * přes {@link AppUserRepository}. Pokud autentizace není dostupná nebo uživatel neexistuje, vrací se null.
     *
     * @return Identifikátor aktuálního uživatele, nebo null pokud uživatele nelze určit.
     */
    private Long getCurrentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        String email = auth.getName();
        return appUserRepository.findByEmail(email)
                .map(AppUserEntity::getId)
                .orElse(null);
    }

}