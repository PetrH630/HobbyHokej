package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.match.enums.MatchMode;
import cz.phsoft.hokej.match.exceptions.MatchNotFoundException;
import cz.phsoft.hokej.match.repositories.MatchRepository;
import cz.phsoft.hokej.match.util.MatchModeLayoutUtil;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.PlayerPositionCategory;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.registration.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.registration.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.registration.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.registration.services.MatchRegistrationQueryService;
import cz.phsoft.hokej.registration.util.PlayerPositionUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Implementace MatchAllocationEngine.
 *
 * Třída centralizuje doménovou logiku přepočtu kapacity hráčů
 * a rozložení pozic v zápase.
 *
 * Zajišťuje:
 * - přepočet REGISTERED / RESERVED při změně kapacity,
 * - vyvážení týmů DARK a LIGHT,
 * - kontrolu a rebalance pozic podle MatchMode,
 * - povýšení hráčů při navýšení kapacity,
 * - úpravu pozic při změně herního módu.
 *
 * Operace jsou prováděny transakčně.
 */
@Service
public class MatchAllocationEngineImpl implements MatchAllocationEngine {

    private static final Logger log =
            LoggerFactory.getLogger(MatchAllocationEngineImpl.class);

    private final MatchRepository matchRepository;
    private final MatchRegistrationRepository registrationRepository;
    private final MatchRegistrationQueryService matchRegistrationQueryService;

    public MatchAllocationEngineImpl(
            MatchRepository matchRepository,
            MatchRegistrationRepository registrationRepository,
            MatchRegistrationQueryService matchRegistrationQueryService
    ) {
        this.matchRepository = matchRepository;
        this.registrationRepository = registrationRepository;
        this.matchRegistrationQueryService = matchRegistrationQueryService;
    }

    /**
     * Provede globální přepočet registrací pro daný zápas.
     *
     * Logika:
     * - rozdělí kapacitu mezi brankáře a hráče v poli,
     * - vybalancuje týmy podle cílové kapacity,
     * - nastaví status REGISTERED / RESERVED,
     * - provede rebalance pozic podle kapacity postů.
     *
     * @param matchId identifikátor zápasu
     */
    @Override
    @Transactional
    public void recomputeForMatch(Long matchId) {

        if (matchId == null) {
            return;
        }

        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));

        Integer maxPlayersObj = match.getMaxPlayers();
        if (maxPlayersObj == null || maxPlayersObj <= 0) {
            log.debug("MatchAllocationEngine: matchId={} – maxPlayers je null nebo <= 0, přepočet se neprovede", matchId);
            return;
        }

        int maxPlayers = maxPlayersObj;

        int goalieSlots = getGoalieSlotsForMatch(match);
        goalieSlots = Math.max(0, Math.min(goalieSlots, maxPlayers));

        int skaterCapacity = maxPlayers - goalieSlots;

        List<MatchRegistrationEntity> allRegistered = registrationRepository
                .findByMatchId(matchId).stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .sorted(Comparator.comparing(MatchRegistrationEntity::getTimestamp))
                .toList();

        if (allRegistered.isEmpty()) {
            return;
        }

        List<MatchRegistrationEntity> goalies = allRegistered.stream()
                .filter(this::isGoalieRegistration)
                .toList();

        List<MatchRegistrationEntity> skaters = allRegistered.stream()
                .filter(r -> !isGoalieRegistration(r))
                .toList();

        int keptGoalies = 0;
        for (MatchRegistrationEntity goalieReg : goalies) {
            if (keptGoalies < goalieSlots) {
                updateRegistrationStatus(goalieReg, PlayerMatchStatus.REGISTERED);
                keptGoalies++;
            } else {
                updateRegistrationStatus(goalieReg, PlayerMatchStatus.RESERVED);
            }
        }

        int desiredTotal = skaterCapacity;

        long currentDark = skaters.stream()
                .filter(r -> r.getTeam() == Team.DARK)
                .count();

        long currentLight = skaters.stream()
                .filter(r -> r.getTeam() == Team.LIGHT)
                .count();

        int targetDark;
        int targetLight;

        if (desiredTotal % 2 == 0) {
            targetDark = desiredTotal / 2;
            targetLight = desiredTotal / 2;
        } else {
            if (currentDark >= currentLight) {
                targetDark = desiredTotal / 2 + 1;
                targetLight = desiredTotal - targetDark;
            } else {
                targetLight = desiredTotal / 2 + 1;
                targetDark = desiredTotal - targetLight;
            }
        }

        int dark = 0;
        int light = 0;

        for (MatchRegistrationEntity reg : skaters) {

            Team team = reg.getTeam();
            boolean movable = canAutoMoveTeam(reg);

            if (team == null) {
                if (dark < targetDark) {
                    reg.setTeam(Team.DARK);
                    updateRegistrationStatus(reg, PlayerMatchStatus.REGISTERED);
                    dark++;
                } else if (light < targetLight) {
                    reg.setTeam(Team.LIGHT);
                    updateRegistrationStatus(reg, PlayerMatchStatus.REGISTERED);
                    light++;
                } else {
                    updateRegistrationStatus(reg, PlayerMatchStatus.RESERVED);
                }
                continue;
            }

            if (team == Team.DARK) {
                if (dark < targetDark) {
                    updateRegistrationStatus(reg, PlayerMatchStatus.REGISTERED);
                    dark++;
                } else if (movable && light < targetLight) {
                    reg.setTeam(Team.LIGHT);
                    updateRegistrationStatus(reg, PlayerMatchStatus.REGISTERED);
                    light++;
                } else {
                    updateRegistrationStatus(reg, PlayerMatchStatus.RESERVED);
                }
            } else if (team == Team.LIGHT) {
                if (light < targetLight) {
                    updateRegistrationStatus(reg, PlayerMatchStatus.REGISTERED);
                    light++;
                } else if (movable && dark < targetDark) {
                    reg.setTeam(Team.DARK);
                    updateRegistrationStatus(reg, PlayerMatchStatus.REGISTERED);
                    dark++;
                } else {
                    updateRegistrationStatus(reg, PlayerMatchStatus.RESERVED);
                }
            } else {
                updateRegistrationStatus(reg, PlayerMatchStatus.RESERVED);
            }
        }

        rebalancePositionsForMatch(match);
    }

    /**
     * Zpracuje navýšení kapacity zápasu.
     *
     * Nové sloty rozdělí mezi týmy a pokusí se povýšit
     * hráče ze stavu RESERVED na REGISTERED.
     */
    @Override
    @Transactional
    public void handleCapacityIncrease(MatchEntity match, int totalNewSlots) {

        if (match == null || totalNewSlots <= 0) {
            return;
        }

        Long matchId = match.getId();

        List<MatchRegistrationDTO> regs =
                matchRegistrationQueryService.getRegistrationsForMatch(matchId);

        int registeredDark = (int) regs.stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .filter(r -> r.getTeam() == Team.DARK)
                .count();

        int registeredLight = (int) regs.stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .filter(r -> r.getTeam() == Team.LIGHT)
                .count();

        int baseSlots = totalNewSlots / 2;
        int extra = totalNewSlots % 2;

        int darkSlots = baseSlots;
        int lightSlots = baseSlots;

        if (extra > 0) {
            if (registeredDark <= registeredLight) {
                darkSlots++;
            } else {
                lightSlots++;
            }
        }

        if (darkSlots > 0) {
            promoteReservedCandidatesForCapacityIncreaseInternal(match, Team.DARK, darkSlots);
        }

        if (lightSlots > 0) {
            promoteReservedCandidatesForCapacityIncreaseInternal(match, Team.LIGHT, lightSlots);
        }
    }

    /**
     * Zpracuje změnu herního módu.
     *
     * Opraví neplatné pozice a následně provede rebalance
     * kapacity pozic v rámci týmů.
     */
    @Override
    @Transactional
    public void handleMatchModeChange(MatchEntity match, MatchMode oldMatchMode) {

        if (match == null) {
            return;
        }

        MatchMode newMode = match.getMatchMode();
        if (newMode == null || newMode == oldMatchMode) {
            return;
        }

        List<PlayerPosition> icePositions =
                MatchModeLayoutUtil.getIcePositionsForMode(newMode);

        if (icePositions == null || icePositions.isEmpty()) {
            return;
        }

        Set<PlayerPosition> allowedPositions =
                new LinkedHashSet<>(icePositions);

        List<MatchRegistrationEntity> registrations =
                registrationRepository.findByMatchId(match.getId());

        if (registrations.isEmpty()) {
            return;
        }

        boolean changed = false;

        for (MatchRegistrationEntity reg : registrations) {

            if (reg.getStatus() != PlayerMatchStatus.REGISTERED
                    && reg.getStatus() != PlayerMatchStatus.RESERVED) {
                continue;
            }

            PlayerPosition current = reg.getPositionInMatch();
            if (current == null || current == PlayerPosition.ANY) {
                continue;
            }

            if (allowedPositions.contains(current)) {
                continue;
            }

            PlayerEntity player = reg.getPlayer();
            if (player == null) {
                continue;
            }

            var settings = player.getSettings();
            boolean canChangePosition =
                    settings != null && settings.isPossibleChangePlayerPosition();

            PlayerPosition primary = player.getPrimaryPosition();
            PlayerPosition secondary = player.getSecondaryPosition();

            PlayerPosition newPosition = resolvePositionForMatchModeChange(
                    current,
                    primary,
                    secondary,
                    allowedPositions,
                    canChangePosition
            );

            if (newPosition != null && newPosition != current) {
                reg.setPositionInMatch(newPosition);
                changed = true;
            }
        }

        boolean rebalanced =
                rebalancePositionsWithinTeams(match, registrations);

        if (changed || rebalanced) {
            registrationRepository.saveAll(registrations);
        }
    }

    // === Helper metody ===

    private void updateRegistrationStatus(MatchRegistrationEntity registration,
                                          PlayerMatchStatus status) {

        if (registration == null) {
            return;
        }

        if (registration.getStatus() == status) {
            return;
        }

        registration.setStatus(status);
        registration.setCreatedBy("system");
        registrationRepository.saveAndFlush(registration);
    }

    private boolean isGoalieRegistration(MatchRegistrationEntity registration) {
        if (registration == null || registration.getPlayer() == null) {
            return false;
        }

        PlayerPosition position = registration.getPositionInMatch();
        if (position == null) {
            position = registration.getPlayer().getPrimaryPosition();
        }

        return PlayerPositionUtil.isGoalie(position);
    }

    private int getGoalieSlotsForMatch(MatchEntity match) {
        MatchMode mode = match.getMatchMode();
        if (mode == null) {
            return 0;
        }

        return switch (mode) {
            case THREE_ON_THREE_WITH_GOALIE,
                 FOUR_ON_FOUR_WITH_GOALIE,
                 FIVE_ON_FIVE_WITH_GOALIE -> 2;
            default -> 0;
        };
    }

    private boolean canAutoMoveTeam(MatchRegistrationEntity registration) {
        if (registration == null || registration.getPlayer() == null) {
            return false;
        }

        var settings = registration.getPlayer().getSettings();
        return settings != null && settings.isPossibleMoveToAnotherTeam();
    }

    /**
     * Rebalance REGISTERED/RESERVED podle kapacity postů
     * (používá se po přepočtu kapacity).
     */
    private void rebalancePositionsForMatch(MatchEntity match) {
        Integer maxPlayersObj = match.getMaxPlayers();
        MatchMode mode = match.getMatchMode();

        if (maxPlayersObj == null || maxPlayersObj <= 0 || mode == null) {
            return;
        }

        int maxPlayers = maxPlayersObj;
        int slotsPerTeam = maxPlayers / 2;

        // Kapacita pozic pro JEDEN tým
        Map<PlayerPosition, Integer> perTeamCapacity =
                MatchModeLayoutUtil.buildPositionCapacityForMode(mode, slotsPerTeam);

        // Pro každý tým zvlášť (předpokládám jen DARK / LIGHT)
        rebalancePositionsForTeam(match, Team.DARK, perTeamCapacity);
        rebalancePositionsForTeam(match, Team.LIGHT, perTeamCapacity);
    }

    private void rebalancePositionsForTeam(MatchEntity match,
                                           Team team,
                                           Map<PlayerPosition, Integer> perTeamCapacity) {

        if (team == null) {
            return;
        }

        // Všechny registrace daného zápasu a týmu, které mohou být na ledě / v náhradnících
        List<MatchRegistrationEntity> teamRegs = registrationRepository
                .findByMatchId(match.getId()).stream()
                .filter(r -> r.getTeam() == team)
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED
                        || r.getStatus() == PlayerMatchStatus.RESERVED)
                // GOALIE řešíme zvlášť přes goalieSlots, tady je vynecháme
                .filter(r -> !isGoalieRegistration(r))
                .toList();

        if (teamRegs.isEmpty()) {
            return;
        }

        for (Map.Entry<PlayerPosition, Integer> entry : perTeamCapacity.entrySet()) {
            PlayerPosition position = entry.getKey();
            int capacity = entry.getValue();

            if (capacity <= 0) {
                continue;
            }

            // Registrace pro danou pozici v tomto týmu
            List<MatchRegistrationEntity> regsForPosition = teamRegs.stream()
                    .filter(r -> r.getPositionInMatch() == position)
                    .toList();

            if (regsForPosition.isEmpty()) {
                continue;
            }

            // Seřadit podle timestamp (nejdřív přihlášení má přednost)
            regsForPosition = regsForPosition.stream()
                    .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                    .toList();

            for (int i = 0; i < regsForPosition.size(); i++) {
                MatchRegistrationEntity reg = regsForPosition.get(i);

                if (i < capacity) {
                    // Ve slotu – musí být REGISTERED
                    if (reg.getStatus() != PlayerMatchStatus.REGISTERED) {
                        updateRegistrationStatus(reg, PlayerMatchStatus.REGISTERED);
                    }
                } else {
                    // Nad kapacitu – náhradník
                    if (reg.getStatus() != PlayerMatchStatus.RESERVED) {
                        updateRegistrationStatus(reg, PlayerMatchStatus.RESERVED);
                    }
                }
            }
        }
    }

    /**
     * Rebalance pozic v rámci týmů po změně módu (nemění status,
     * jen positionInMatch u REGISTERED hráčů).
     *
     * @return true, pokud došlo k nějakým změnám.
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

        // maxPlayers je celkem pro oba týmy - kapacita pro jeden tým
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

            // Rozdělení podle aktuální pozice
            var regsByPosition = new EnumMap<PlayerPosition, List<MatchRegistrationEntity>>(PlayerPosition.class);

            for (MatchRegistrationEntity reg : teamRegs) {
                PlayerPosition pos = reg.getPositionInMatch();
                if (pos == null || pos == PlayerPosition.ANY) {
                    continue;
                }
                regsByPosition
                        .computeIfAbsent(pos, p -> new ArrayList<>())
                        .add(reg);
            }

            // Zjistíme, kde je plno a kde je volno
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
                continue;
            }

            // dosadíme hráče s ANY/null na volné posty
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

            // Z přecpaných pozic přesuneme část hráčů na volné pozice
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

                var sorted = new ArrayList<>(regsAtPos);
                sorted.sort(Comparator.comparing(MatchRegistrationEntity::getTimestamp).reversed());

                for (MatchRegistrationEntity reg : sorted) {
                    if (toMove <= 0 || freeSlots.isEmpty()) {
                        break;
                    }

                    PlayerEntity player = reg.getPlayer();
                    boolean canChangePosition = player != null
                            && player.getSettings() != null
                            && player.getSettings().isPossibleChangePlayerPosition();

                    PlayerPosition target =
                            pickTargetForRebalance(fromPosition, freeSlots, canChangePosition);

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
     * Mapping pozice při změně módu na jinou platnou pozici.
     */
    private PlayerPosition resolvePositionForMatchModeChange(
            PlayerPosition currentPosition,
            PlayerPosition primary,
            PlayerPosition secondary,
            Set<PlayerPosition> allowedPositions,
            boolean canChangePosition
    ) {
        if (currentPosition == null || currentPosition == PlayerPosition.ANY) {
            return null;
        }

        // Stará pozice je v novém módu platná - nic neměníme
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
            // Preferovat primary ve stejné kategorii
            if (isCandidatePosition(primary, allowedPositions, currentCategory)) {
                return primary;
            }

            // Pak secondary ve stejné kategorii
            if (isCandidatePosition(secondary, allowedPositions, currentCategory)) {
                return secondary;
            }

            // První pozice ve stejné kategorii
            PlayerPosition sameCategoryTarget = allowedPositions.stream()
                    .filter(p -> getPositionCategory(p) == currentCategory)
                    .findFirst()
                    .orElse(null);

            if (sameCategoryTarget != null) {
                return sameCategoryTarget;
            }

            // Pokud hráč nechce měnit kategorii, konec
            if (!canChangePosition) {
                return null;
            }

            // Jinak libovolná první allowed pozice
            return allowedPositions.stream().findFirst().orElse(null);
        }

        // V allowedPositions není žádná pozice stejné kategorie

        // Pokud primary je v allowedPositions, použít ho
        if (primary != null && primary != PlayerPosition.ANY && allowedPositions.contains(primary)) {
            return primary;
        }

        // Secondary v allowedPositions
        if (secondary != null && secondary != PlayerPosition.ANY && allowedPositions.contains(secondary)) {
            return secondary;
        }

        // První allowed pozice (fallback)
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
     * Cíl pro hráče s ANY/null pozicí při rebalance po změně módu.
     */
    private PlayerPosition pickTargetForFlexiblePlayer(MatchRegistrationEntity reg,
                                                       Map<PlayerPosition, Integer> freeSlots) {
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
     * Cíl pro hráče z přecpaného postu při rebalance po změně módu.
     */
    private PlayerPosition pickTargetForRebalance(PlayerPosition currentPosition,
                                                  Map<PlayerPosition, Integer> freeSlots,
                                                  boolean canChangePosition) {

        if (currentPosition == null || freeSlots == null || freeSlots.isEmpty()) {
            return null;
        }

        // Neřešíme automaticky přesuny brankářů
        if (PlayerPositionUtil.isGoalie(currentPosition)) {
            return null;
        }

        var currentCategory = PlayerPositionUtil.getCategory(currentPosition);

        // Zkusíme najít volný slot ve stejné kategorii (obrana/útok)
        if (currentCategory != null) {
            PlayerPosition sameCategoryTarget = freeSlots.keySet().stream()
                    .filter(pos -> currentCategory == PlayerPositionUtil.getCategory(pos))
                    .findFirst()
                    .orElse(null);

            if (sameCategoryTarget != null) {
                return sameCategoryTarget;
            }
        }

        // Pokud hráč nechce přechod mezi kategoriemi, končíme
        if (!canChangePosition) {
            return null;
        }

        // Jinak může jít i do jiné kategorie – vezmeme první volnou
        return freeSlots.keySet().stream().findFirst().orElse(null);
    }

    /**
     * Interní povýšení hráčů z RESERVED na REGISTERED při navýšení kapacity
     * pro daný tým (bez vazby na MatchRegistrationCommandService).
     */
    private void promoteReservedCandidatesForCapacityIncreaseInternal(
            MatchEntity match,
            Team targetTeam,
            int slotsCount
    ) {
        if (slotsCount <= 0 || match == null || targetTeam == null) {
            return;
        }

        Long matchId = match.getId();

        long registeredCount = registrationRepository
                .countByMatchIdAndStatus(matchId, PlayerMatchStatus.REGISTERED);

        int maxPlayers = match.getMaxPlayers();
        int remainingSlotsToFill = Math.min(slotsCount, maxPlayers - (int) registeredCount);
        if (remainingSlotsToFill <= 0) {
            return;
        }

        List<MatchRegistrationEntity> reserved = registrationRepository
                .findByMatchIdAndStatus(matchId, PlayerMatchStatus.RESERVED)
                .stream()
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .toList();

        for (MatchRegistrationEntity candidate : reserved) {
            if (remainingSlotsToFill <= 0) {
                break;
            }

            boolean promoted = tryPromoteCandidateForCapacityIncrease(match, candidate, targetTeam);
            if (promoted) {
                remainingSlotsToFill--;
            }
        }
    }

    /**
     * Povýšení jednoho kandidáta z RESERVED na REGISTERED při navýšení kapacity.
     * - respektuje možné přesuny mezi týmy (settings.isPossibleMoveToAnotherTeam),
     * - nemění jeho pozici (zůstává na current/primary),
     * - kontroluje kapacitu konkrétní pozice pro daný tým.
     */
    private boolean tryPromoteCandidateForCapacityIncrease(
            MatchEntity match,
            MatchRegistrationEntity candidate,
            Team requestedTeam
    ) {
        if (candidate == null || candidate.getPlayer() == null || match == null) {
            return false;
        }

        PlayerEntity player = candidate.getPlayer();
        var settings = player.getSettings();

        boolean canMoveTeam =
                settings != null && settings.isPossibleMoveToAnotherTeam();

        Team currentTeam = candidate.getTeam();
        PlayerPosition currentPositionInMatch = candidate.getPositionInMatch();
        PlayerPosition primaryPosition = player.getPrimaryPosition();

        PlayerPosition effectiveCurrentPosition =
                (currentPositionInMatch != null) ? currentPositionInMatch : primaryPosition;

        // Cílový tým – buď zůstane, nebo se přesune, pokud to dovolí nastavení
        Team targetTeam;
        if (requestedTeam == null || currentTeam == requestedTeam) {
            targetTeam = currentTeam;
        } else {
            if (!canMoveTeam) {
                return false;
            }
            targetTeam = requestedTeam;
        }

        // Cílová pozice – při navýšení kapacity ji neměníme
        PlayerPosition targetPosition = effectiveCurrentPosition;

        // Kontrola kapacity pozice – pokud není volno, kandidát se NEpovýší.
        if (!isPositionSlotAvailableForTeam(match, targetTeam, targetPosition)) {
            return false;
        }

        // Provést změnu týmu/pozice a statusu na REGISTERED
        candidate.setTeam(targetTeam);
        candidate.setPositionInMatch(targetPosition);
        updateRegistrationStatus(candidate, PlayerMatchStatus.REGISTERED);

        // Notifikace zde engine neposílá – řeší se ve vyšších vrstvách, pokud je potřeba.
        return true;
    }

    /**
     * Ověřuje, zda je pro daný zápas, tým a pozici ještě volný slot
     * podle MatchModeLayoutUtil a aktuálně REGISTERED hráčů.
     *
     * Kontroluje se:
     * - jen pro konkrétní pozice (null/ANY = bez omezení),
     * - jen pro stav REGISTERED (RESERVED pozice neblokují kapacitu).
     */
    private boolean isPositionSlotAvailableForTeam(MatchEntity match,
                                                   Team team,
                                                   PlayerPosition positionInMatch) {

        // Pokud nemáme konkrétní pozici nebo tým, kapacitu neomezujeme.
        if (positionInMatch == null || positionInMatch == PlayerPosition.ANY || team == null) {
            return true;
        }

        Integer maxPlayers = match.getMaxPlayers();
        MatchMode mode = match.getMatchMode();

        // Není definovaná celková kapacita nebo mód zápasu – neomezujeme.
        if (maxPlayers == null || maxPlayers <= 0 || mode == null) {
            return true;
        }

        // maxPlayers je pro oba týmy.
        int slotsPerTeam = maxPlayers / 2;

        Map<PlayerPosition, Integer> perTeamCapacity =
                MatchModeLayoutUtil.buildPositionCapacityForMode(mode, slotsPerTeam);

        Integer positionCapacity = perTeamCapacity.get(positionInMatch);

        // Pro tuto pozici není definovaná kapacita – bereme jako neomezenou.
        if (positionCapacity == null || positionCapacity <= 0) {
            return true;
        }

        // Spočítáme obsazenost této pozice v daném týmu mezi REGISTERED hráči.
        List<MatchRegistrationEntity> registered = registrationRepository
                .findByMatchIdAndStatus(match.getId(), PlayerMatchStatus.REGISTERED);

        long occupied = registered.stream()
                .filter(r -> r.getTeam() == team)
                .map(MatchRegistrationEntity::getPositionInMatch)
                .filter(Objects::nonNull)
                .filter(pos -> pos != PlayerPosition.ANY)
                .filter(pos -> pos == positionInMatch)
                .count();

        return occupied < positionCapacity;
    }
}