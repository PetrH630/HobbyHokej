package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.demo.DemoModeOperationNotAllowedException;
import cz.phsoft.hokej.match.dto.MatchDTO;
import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.match.enums.MatchCancelReason;
import cz.phsoft.hokej.match.enums.MatchMode;
import cz.phsoft.hokej.match.enums.MatchStatus;
import cz.phsoft.hokej.match.exceptions.InvalidMatchDateTimeException;
import cz.phsoft.hokej.match.exceptions.InvalidMatchStatusException;
import cz.phsoft.hokej.match.exceptions.MatchNotFoundException;
import cz.phsoft.hokej.match.mappers.MatchMapper;
import cz.phsoft.hokej.match.repositories.MatchRepository;
import cz.phsoft.hokej.match.util.MatchModeLayoutUtil;
import cz.phsoft.hokej.notifications.enums.NotificationType;
import cz.phsoft.hokej.notifications.services.MatchTimeChangeContext;
import cz.phsoft.hokej.notifications.services.NotificationService;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.player.repositories.PlayerRepository;
import cz.phsoft.hokej.registration.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.registration.enums.PlayerPositionCategory;
import cz.phsoft.hokej.registration.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.season.exceptions.InvalidSeasonPeriodDateException;
import cz.phsoft.hokej.season.services.CurrentSeasonService;
import cz.phsoft.hokej.season.services.SeasonService;
import cz.phsoft.hokej.shared.dto.SuccessResponseDTO;
import cz.phsoft.hokej.user.entities.AppUserEntity;
import cz.phsoft.hokej.user.repositories.AppUserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import cz.phsoft.hokej.registration.util.PlayerPositionUtil;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementace změnové service vrstvy pro zápasy.
 *
 * Zajišťuje vytváření, úpravu, mazání a změny stavu zápasu.
 * Při úpravách provádí potřebné kontrolní mechanismy nad sezónou
 * a stavem zápasu a zajišťuje side-effects:
 * - přepočet kapacity přes MatchCapacityService,
 * - úpravu pozic hráčů při změně herního systému,
 * - odesílání notifikací hráčům o změně, zrušení a obnovení zápasu.
 */
@Service
public class MatchCommandServiceImpl implements MatchCommandService {

    private static final Logger logger = LoggerFactory.getLogger(MatchCommandServiceImpl.class);

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_MANAGER = "ROLE_MANAGER";

    @Value("${app.demo-mode:false}")
    private boolean isDemoMode;

    private final MatchRepository matchRepository;
    private final MatchRegistrationRepository matchRegistrationRepository;
    private final MatchMapper matchMapper;
    private final PlayerRepository playerRepository;
    private final SeasonService seasonService;
    private final CurrentSeasonService currentSeasonService;
    private final NotificationService notificationService;
    private final AppUserRepository appUserRepository;
    private final MatchCapacityService matchCapacityService;
    private final Clock clock;

    public MatchCommandServiceImpl(
            MatchRepository matchRepository,
            MatchRegistrationRepository matchRegistrationRepository,
            MatchMapper matchMapper,
            PlayerRepository playerRepository,
            SeasonService seasonService,
            CurrentSeasonService currentSeasonService,
            NotificationService notificationService,
            AppUserRepository appUserRepository,
            MatchCapacityService matchCapacityService,
            Clock clock
    ) {
        this.matchRepository = matchRepository;
        this.matchRegistrationRepository = matchRegistrationRepository;
        this.matchMapper = matchMapper;
        this.playerRepository = playerRepository;
        this.seasonService = seasonService;
        this.currentSeasonService = currentSeasonService;
        this.notificationService = notificationService;
        this.appUserRepository = appUserRepository;
        this.matchCapacityService = matchCapacityService;
        this.clock = clock;
    }

    // ======================
    // COMMANDS
    // ======================

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
        MatchMode oldMatchMode = entity.getMatchMode();

        matchMapper.updateEntity(dto, entity);
        logger.info("UPDATE matchId={}, oldMode={}, newModeAfterMap={}",
                id, oldMatchMode, entity.getMatchMode());

        Long currentUserId = getCurrentUserIdOrNull();
        entity.setLastModifiedByUserId(currentUserId);

        if (!isAdminOrManager) {
            validateMatchDateInActiveSeason(entity.getDateTime());
        }

        if (entity.getDateTime() != null
                && entity.getDateTime().isBefore(now())) {
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

        boolean matchModeChanged =
                !java.util.Objects.equals(entity.getMatchMode(), oldMatchMode);

        if (maxPlayersChanged || dateTimeChanged || locationChanged || priceChanged || matchModeChanged) {
            entity.setMatchStatus(MatchStatus.UPDATED);
        }

        MatchEntity saved = matchRepository.save(entity);

        if (matchModeChanged) {
            adjustPlayerPositionsForMatchModeChange(saved, oldMatchMode);
        }

        if (maxPlayersChanged) {
            matchCapacityService.handleCapacityChange(saved, oldMaxPlayers);
        }

        if (dateTimeChanged) {
            MatchTimeChangeContext ctx = new MatchTimeChangeContext(saved, oldDateTime);
            notifyPlayersAboutMatchChanges(ctx, MatchStatus.UPDATED);
        }

        return matchMapper.toDTO(saved);
    }

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
                now().toString()
        );
    }

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
                now().toString()
        );
    }

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
                now().toString()
        );
    }

    // ======================
    // HELPERY – NOTIFIKACE, SEZÓNA, UŽIVATEL
    // ======================

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

    private MatchEntity findMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

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

    private Long getCurrentSeasonIdOrActive() {
        Long id = currentSeasonService.getCurrentSeasonIdOrDefault();
        if (id != null) {
            return id;
        }
        return seasonService.getActiveSeason().getId();
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    // ======================
    // ZMĚNA HERNÍHO SYSTÉMU – POZICE
    // ======================

    private void adjustPlayerPositionsForMatchModeChange(MatchEntity match, MatchMode oldMatchMode) {
        MatchMode newMode = match.getMatchMode();
        if (newMode == null || newMode == oldMatchMode) {
            return;
        }

        // Povolené pozice v novém režimu (stejné pro oba týmy)
        List<PlayerPosition> icePositions = MatchModeLayoutUtil.getIcePositionsForMode(newMode);
        if (icePositions == null || icePositions.isEmpty()) {
            return;
        }
        Set<PlayerPosition> allowedPositions = new LinkedHashSet<>(icePositions);

        // Registrace pro daný zápas – budeme upravovat jen REGISTERED / RESERVED
        List<MatchRegistrationEntity> registrations =
                matchRegistrationRepository.findByMatchId(match.getId());
        if (registrations.isEmpty()) {
            return;
        }

        boolean changed = false;

        // 1) Původní logika – jenom přemapování neplatných pozic
        for (MatchRegistrationEntity reg : registrations) {
            PlayerMatchStatus status = reg.getStatus();
            if (status != PlayerMatchStatus.REGISTERED
                    && status != PlayerMatchStatus.RESERVED) {
                continue;
            }

            PlayerPosition currentPosition = reg.getPositionInMatch();
            if (currentPosition == null || currentPosition == PlayerPosition.ANY) {
                continue;
            }

            // Pokud je stávající pozice v novém režimu platná, není třeba nic měnit
            if (allowedPositions.contains(currentPosition)) {
                continue;
            }

            PlayerEntity player = reg.getPlayer();
            if (player == null) {
                continue;
            }

            var settings = player.getSettings();
            boolean canCrossCategory = settings != null && settings.isPossibleChangePlayerPosition();

            PlayerPosition primary = player.getPrimaryPosition();
            PlayerPosition secondary = player.getSecondaryPosition();

            PlayerPosition newPosition = resolvePositionForMatchModeChange(
                    currentPosition,
                    primary,
                    secondary,
                    allowedPositions,
                    canCrossCategory
            );

            if (newPosition != null && newPosition != currentPosition) {
                reg.setPositionInMatch(newPosition);
                changed = true;
            }
        }

        // 2) NOVÉ – vybalancování pozic v rámci týmů podle kapacity postů
        boolean rebalanced = rebalancePositionsWithinTeams(match, registrations);
        if (rebalanced) {
            changed = true;
        }

        if (changed) {
            matchRegistrationRepository.saveAll(registrations);
        }
    }

    private PlayerPosition resolvePositionForMatchModeChange(
            PlayerPosition currentPosition,
            PlayerPosition primary,
            PlayerPosition secondary,
            Set<PlayerPosition> allowedPositions,
            boolean canCrossCategory
    ) {
        if (currentPosition == null || currentPosition == PlayerPosition.ANY) {
            return null;
        }

        if (allowedPositions.contains(currentPosition)) {
            return null;
        }

        PlayerPositionCategory currentCategory = getPositionCategory(currentPosition);
        if (currentCategory == null) {
            return null;
        }

        boolean hasSameCategory = allowedPositions.stream()
                .anyMatch(p -> getPositionCategory(p) == currentCategory);

        if (hasSameCategory) {
            if (isCandidatePosition(primary, allowedPositions, currentCategory)) {
                return primary;
            }

            if (isCandidatePosition(secondary, allowedPositions, currentCategory)) {
                return secondary;
            }

            PlayerPosition sameCategoryTarget = allowedPositions.stream()
                    .filter(p -> getPositionCategory(p) == currentCategory)
                    .findFirst()
                    .orElse(null);

            if (sameCategoryTarget != null) {
                return sameCategoryTarget;
            }

            if (!canCrossCategory) {
                return null;
            }

            return allowedPositions.stream().findFirst().orElse(null);
        }

        if (primary != null && primary != PlayerPosition.ANY && allowedPositions.contains(primary)) {
            return primary;
        }

        if (secondary != null && secondary != PlayerPosition.ANY && allowedPositions.contains(secondary)) {
            return secondary;
        }

        return allowedPositions.stream().findFirst().orElse(null);
    }

    private boolean isCandidatePosition(
            PlayerPosition candidate,
            Set<PlayerPosition> allowedPositions,
            PlayerPositionCategory requiredCategory
    ) {
        if (candidate == null || candidate == PlayerPosition.ANY) {
            return false;
        }
        if (!allowedPositions.contains(candidate)) {
            return false;
        }
        PlayerPositionCategory cat = getPositionCategory(candidate);
        return cat == requiredCategory;
    }

    private PlayerPositionCategory getPositionCategory(PlayerPosition position) {
        return PlayerPositionUtil.getCategory(position);
    }

    /**
     * Po změně herního režimu přerozdělí pozice hráčů v rámci týmů tak,
     * aby co nejlépe odpovídaly nové kapacitě postů (MatchMode + maxPlayers).
     *
     * - pracuje pouze s hráči ve stavu REGISTERED,
     * - nemění tým ani status, pouze positionInMatch,
     * - respektuje PlayerSettings.isPossibleChangePlayerPosition
     *   při přechodu mezi obranou a útokem,
     * - goalies necháváme na pokoji (kapacitu brankářů řídí MatchModeLayoutUtil).
     *
     * @param match          Zápas po změně režimu.
     * @param registrations  Všechny registrace k danému zápasu.
     * @return true, pokud došlo k nějaké změně pozic, jinak false.
     */
    private boolean rebalancePositionsWithinTeams(MatchEntity match,
                                                  List<MatchRegistrationEntity> registrations) {
        if (match == null || registrations == null || registrations.isEmpty()) {
            return false;
        }

        Integer maxPlayersObj = match.getMaxPlayers();
        MatchMode mode = match.getMatchMode();

        if (maxPlayersObj == null || maxPlayersObj <= 0 || mode == null) {
            return false;
        }

        // maxPlayers je celkem pro oba týmy → kapacita pro jeden tým
        int slotsPerTeam = maxPlayersObj / 2;
        var perTeamCapacity =
                MatchModeLayoutUtil.buildPositionCapacityForMode(mode, slotsPerTeam);

        if (perTeamCapacity == null || perTeamCapacity.isEmpty()) {
            return false;
        }

        boolean changed = false;

        // Přerozdělujeme zvlášť pro DARK a LIGHT
        for (Team team : Team.values()) {
            if (team == null) {
                continue;
            }

            // REGISTERED hráči v daném týmu
            List<MatchRegistrationEntity> teamRegs = registrations.stream()
                    .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                    .filter(r -> team.equals(r.getTeam()))
                    .toList();

            if (teamRegs.isEmpty()) {
                continue;
            }

            // 1) Rozdělení podle aktuální pozice
            //    (null/ANY se ignorují – použijeme je až jako "flex" kandidáty)
            var regsByPosition = new EnumMap<PlayerPosition, List<MatchRegistrationEntity>>(PlayerPosition.class);

            for (MatchRegistrationEntity reg : teamRegs) {
                PlayerPosition pos = reg.getPositionInMatch();
                if (pos == null || pos == PlayerPosition.ANY) {
                    continue;
                }
                regsByPosition
                        .computeIfAbsent(pos, p -> new java.util.ArrayList<>())
                        .add(reg);
            }

            // 2) Zjistíme, kde je plno a kde je volno
            var freeSlots = new EnumMap<PlayerPosition, Integer>(PlayerPosition.class);
            var overload = new EnumMap<PlayerPosition, Integer>(PlayerPosition.class);

            perTeamCapacity.forEach((position, capacity) -> {
                int occupied = regsByPosition.getOrDefault(position, List.of()).size();
                if (occupied < capacity) {
                    freeSlots.put(position, capacity - occupied);
                } else if (occupied > capacity) {
                    overload.put(position, occupied - capacity);
                }
            });

            if (freeSlots.isEmpty() && overload.isEmpty()) {
                continue; // nic k řešení v tomto týmu
            }

            // 3) Nejdřív dosadíme hráče s ANY/null na volné posty
            for (MatchRegistrationEntity reg : teamRegs) {
                PlayerPosition pos = reg.getPositionInMatch();
                if (pos != null && pos != PlayerPosition.ANY) {
                    continue;
                }

                PlayerPosition target = pickTargetForFlexiblePlayer(reg, freeSlots);
                if (target == null) {
                    continue;
                }

                reg.setPositionInMatch(target);
                changed = true;

                int remaining = freeSlots.getOrDefault(target, 0) - 1;
                if (remaining > 0) {
                    freeSlots.put(target, remaining);
                } else {
                    freeSlots.remove(target);
                }

                if (freeSlots.isEmpty()) {
                    break;
                }
            }

            if (freeSlots.isEmpty() || overload.isEmpty()) {
                continue;
            }

            // 4) Z přecpaných pozic přesuneme část hráčů na volné pozice
            for (var entry : overload.entrySet()) {
                PlayerPosition fromPosition = entry.getKey();
                int toMove = entry.getValue();

                // Goalies nechceme automaticky přesouvat
                if (PlayerPositionUtil.isGoalie(fromPosition)) {
                    continue;
                }

                List<MatchRegistrationEntity> regsAtPos =
                        regsByPosition.getOrDefault(fromPosition, List.of());

                if (regsAtPos.isEmpty() || toMove <= 0) {
                    continue;
                }

                // Od konce (nejpozdější registrace) – ti jsou "nejméně stabilní"
                var sorted = new java.util.ArrayList<>(regsAtPos);
                sorted.sort(java.util.Comparator.comparing(MatchRegistrationEntity::getTimestamp).reversed());

                for (MatchRegistrationEntity reg : sorted) {
                    if (toMove <= 0 || freeSlots.isEmpty()) {
                        break;
                    }

                    PlayerEntity player = reg.getPlayer();
                    boolean canCrossCategory = player != null
                            && player.getSettings() != null
                            && player.getSettings().isPossibleChangePlayerPosition();

                    PlayerPosition target =
                            pickTargetForRebalance(fromPosition, freeSlots, canCrossCategory);

                    if (target == null) {
                        continue;
                    }

                    // Změníme pozici v rámci stejného týmu, status zůstává REGISTERED
                    reg.setPositionInMatch(target);
                    changed = true;
                    toMove--;

                    int remaining = freeSlots.getOrDefault(target, 0) - 1;
                    if (remaining > 0) {
                        freeSlots.put(target, remaining);
                    } else {
                        freeSlots.remove(target);
                    }
                }
            }
        }

        return changed;
    }

    /**
     * Vybere vhodnou cílovou pozici pro hráče, který neměl dosud
     * přiřazenou konkrétní pozici (positionInMatch = null / ANY).
     *
     * Preferuje se:
     * - primaryPosition hráče, pokud má na dané pozici volný slot,
     * - secondaryPosition, pokud existuje a má volný slot,
     * - jinak libovolná pozice, kde je volno.
     */
    private PlayerPosition pickTargetForFlexiblePlayer(MatchRegistrationEntity reg,
                                                       java.util.Map<PlayerPosition, Integer> freeSlots) {
        if (freeSlots == null || freeSlots.isEmpty() || reg == null) {
            return null;
        }

        PlayerEntity player = reg.getPlayer();
        if (player != null) {
            PlayerPosition primary = player.getPrimaryPosition();
            if (primary != null && freeSlots.containsKey(primary)) {
                return primary;
            }

            PlayerPosition secondary = player.getSecondaryPosition();
            if (secondary != null && freeSlots.containsKey(secondary)) {
                return secondary;
            }
        }

        // fallback – první dostupná pozice s volnem
        return freeSlots.keySet().stream().findFirst().orElse(null);
    }
    /**
     * Vybere cílovou pozici pro hráče z přecpaného postu.
     *
     * Preferuje:
     * - pozice ve stejné kategorii (obrana/útok),
     * - pouze pokud má hráč povolen přechod mezi kategoriemi,
     *   použije i jinou kategorii.
     *
     * Brankářské pozice se vyhodnocují konzervativně – přechody
     * z/do GOALIE řeší jiná logika.
     */
    private PlayerPosition pickTargetForRebalance(PlayerPosition currentPosition,
                                                  java.util.Map<PlayerPosition, Integer> freeSlots,
                                                  boolean canCrossCategory) {

        if (currentPosition == null || freeSlots == null || freeSlots.isEmpty()) {
            return null;
        }

        // Neřešíme automaticky přesuny brankářů
        if (PlayerPositionUtil.isGoalie(currentPosition)) {
            return null;
        }

        var currentCategory = PlayerPositionUtil.getCategory(currentPosition);

        // 1) Zkusíme najít volný slot ve stejné kategorii (obrana/útok)
        if (currentCategory != null) {
            PlayerPosition sameCategoryTarget = freeSlots.keySet().stream()
                    .filter(pos -> currentCategory == PlayerPositionUtil.getCategory(pos))
                    .findFirst()
                    .orElse(null);

            if (sameCategoryTarget != null) {
                return sameCategoryTarget;
            }
        }

        // 2) Pokud hráč nechce přechod mezi kategoriemi, končíme
        if (!canCrossCategory) {
            return null;
        }

        // 3) Jinak může jít i do jiné kategorie – vezmeme první volnou
        return freeSlots.keySet().stream().findFirst().orElse(null);
    }
}