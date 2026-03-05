package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.match.enums.MatchMode;
import cz.phsoft.hokej.match.exceptions.MatchNotFoundException;
import cz.phsoft.hokej.match.repositories.MatchRepository;
import cz.phsoft.hokej.match.util.MatchModeLayoutUtil;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.registration.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.registration.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.registration.util.PlayerPositionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementace služby pro automatické přeskupení první lajny.
 *
 * Algoritmus:
 * - přeskupuje v rámci hráčů ve stavu REGISTERED,
 * - dokáže povýšit hráče z RESERVED do REGISTERED, pokud je potřeba doplnit sestavu,
 * - nemění tým hráče,
 * - respektuje kapacitu pozic dle MatchMode,
 * - preferuje primary a secondary pozici hráče,
 * - při konfliktu upřednostňuje nejpozději registrovaného hráče.
 */
@Service
public class MatchAutoLineupServiceImpl implements MatchAutoLineupService {

    private static final Logger logger =
            LoggerFactory.getLogger(MatchAutoLineupServiceImpl.class);

    private final MatchRepository matchRepository;
    private final MatchRegistrationRepository registrationRepository;

    public MatchAutoLineupServiceImpl(
            MatchRepository matchRepository,
            MatchRegistrationRepository registrationRepository
    ) {
        this.matchRepository = matchRepository;
        this.registrationRepository = registrationRepository;
    }

    @Override
    @Transactional
    public void autoArrangeStartingLineup(Long matchId) {

        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));

        Integer maxPlayers = match.getMaxPlayers();
        MatchMode mode = match.getMatchMode();

        if (maxPlayers == null || maxPlayers <= 0 || mode == null) {
            return;
        }

        // DŮLEŽITÉ: načti i RESERVED, aby šlo povyšovat do sestavy.
        List<MatchRegistrationEntity> regs =
                registrationRepository.findByMatchIdAndStatusIn(
                        matchId,
                        List.of(PlayerMatchStatus.REGISTERED, PlayerMatchStatus.RESERVED)
                );

        if (regs.isEmpty()) {
            return;
        }

        boolean changed = false;
        changed |= autoArrangeForTeam(match, Team.DARK, regs);
        changed |= autoArrangeForTeam(match, Team.LIGHT, regs);

        if (changed) {
            registrationRepository.saveAll(regs);
        }
    }

    private boolean autoArrangeForTeam(MatchEntity match,
                                       Team team,
                                       List<MatchRegistrationEntity> allRegs) {

        Integer maxPlayers = match.getMaxPlayers();
        MatchMode mode = match.getMatchMode();

        if (maxPlayers == null || maxPlayers <= 0 || mode == null || team == null) {
            return false;
        }

        int slotsPerTeam = maxPlayers / 2;

        Map<PlayerPosition, Integer> perTeamCapacity =
                MatchModeLayoutUtil.buildPositionCapacityForMode(mode, slotsPerTeam);

        List<MatchRegistrationEntity> teamRegs = allRegs.stream()
                .filter(r -> r.getTeam() == team)
                .toList();

        if (teamRegs.isEmpty()) {
            return false;
        }

        // Mapujeme jen REGISTERED do “obsazenosti” pozic (to je sestava).
        Map<PlayerPosition, List<MatchRegistrationEntity>> registeredByPosition =
                teamRegs.stream()
                        .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                        .collect(Collectors.groupingBy(
                                this::normalizePosition,
                                () -> new EnumMap<>(PlayerPosition.class),
                                Collectors.toList()
                        ));

        // 1) Nejprve zkus přeskupit v rámci REGISTERED (tvoje původní logika).
        List<PlayerPosition> targetPositions = perTeamCapacity.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue() > 0)
                .map(Map.Entry::getKey)
                .filter(pos -> {
                    List<MatchRegistrationEntity> list = registeredByPosition.get(pos);
                    int occupied = (list == null) ? 0 : list.size();
                    return occupied == 0;
                })
                .toList();

        boolean changed = false;

        for (PlayerPosition target : targetPositions) {
            boolean filled = tryFillTargetPositionFromRegistered(
                    target,
                    teamRegs,
                    registeredByPosition,
                    perTeamCapacity,
                    match,
                    team
            );
            if (filled) {
                changed = true;
            }
        }

        // 2) Pokud pozice pořád prázdné, doplň je z RESERVED → REGISTERED.
        //    Doplňujeme jen do kapacity dané pozice.
        for (var entry : perTeamCapacity.entrySet()) {
            PlayerPosition pos = entry.getKey();
            Integer cap = entry.getValue();
            if (cap == null || cap <= 0) {
                continue;
            }

            int occupied = registeredByPosition.get(pos) == null ? 0 : registeredByPosition.get(pos).size();
            while (occupied < cap) {
                MatchRegistrationEntity promoted = pickBestReserveCandidateForTargetPosition(pos, teamRegs);
                if (promoted == null) {
                    break;
                }

                logger.info(
                        "AUTO_LINEUP_PROMOTE matchId={}, team={}, playerId={}, fromStatus={}, toStatus={}, toPosition={}",
                        match.getId(),
                        team,
                        promoted.getPlayer() == null ? null : promoted.getPlayer().getId(),
                        promoted.getStatus(),
                        PlayerMatchStatus.REGISTERED,
                        pos
                );

                promoted.setStatus(PlayerMatchStatus.REGISTERED);
                promoted.setPositionInMatch(pos);

                registeredByPosition
                        .computeIfAbsent(pos, p -> new java.util.ArrayList<>())
                        .add(promoted);

                changed = true;
                occupied++;
            }
        }

        return changed;
    }

    private boolean tryFillTargetPositionFromRegistered(PlayerPosition targetPosition,
                                                        List<MatchRegistrationEntity> teamRegs,
                                                        Map<PlayerPosition, List<MatchRegistrationEntity>> regsByPosition,
                                                        Map<PlayerPosition, Integer> perTeamCapacity,
                                                        MatchEntity match,
                                                        Team team) {

        MatchRegistrationEntity candidate = pickBestCandidateForTargetPositionFromRegistered(
                targetPosition,
                teamRegs,
                regsByPosition,
                perTeamCapacity
        );

        if (candidate == null) {
            return false;
        }

        PlayerPosition oldPos = normalizePosition(candidate);

        logger.info(
                "AUTO_LINEUP matchId={}, team={}, playerId={}, from={}, to={}",
                match.getId(),
                team,
                candidate.getPlayer() == null ? null : candidate.getPlayer().getId(),
                oldPos,
                targetPosition
        );

        if (oldPos != null) {
            List<MatchRegistrationEntity> fromList = regsByPosition.get(oldPos);
            if (fromList != null) {
                fromList.remove(candidate);
            }
        }

        candidate.setPositionInMatch(targetPosition);
        regsByPosition
                .computeIfAbsent(targetPosition, p -> new java.util.ArrayList<>())
                .add(candidate);

        return true;
    }

    private MatchRegistrationEntity pickBestCandidateForTargetPositionFromRegistered(
            PlayerPosition targetPosition,
            List<MatchRegistrationEntity> teamRegs,
            Map<PlayerPosition, List<MatchRegistrationEntity>> regsByPosition,
            Map<PlayerPosition, Integer> perTeamCapacity
    ) {

        if (targetPosition == null) {
            return null;
        }

        var targetCategory = PlayerPositionUtil.getCategory(targetPosition);
        if (targetCategory == null) {
            return null;
        }

        return teamRegs.stream()
                .filter(reg -> reg.getStatus() == PlayerMatchStatus.REGISTERED)
                .filter(reg -> {
                    PlayerPosition current = normalizePosition(reg);
                    if (current == null) {
                        return false;
                    }

                    if (PlayerPositionUtil.isGoalie(current)
                            && !PlayerPositionUtil.isGoalie(targetPosition)) {
                        return false;
                    }

                    var currentCat = PlayerPositionUtil.getCategory(current);
                    if (currentCat == null || currentCat != targetCategory) {
                        return false;
                    }

                    if (current == targetPosition) {
                        return false;
                    }

                    List<MatchRegistrationEntity> list = regsByPosition.get(current);
                    int occupied = (list == null) ? 0 : list.size();

                    Integer sourceCapacity = perTeamCapacity.get(current);

                    if (sourceCapacity == null) {
                        return true;
                    }

                    return occupied > 1;
                })
                .sorted((a, b) -> {
                    int scoreA = preferenceScore(a, targetPosition);
                    int scoreB = preferenceScore(b, targetPosition);

                    if (scoreA != scoreB) {
                        return Integer.compare(scoreB, scoreA);
                    }

                    // bezpečnější při null
                    if (a.getTimestamp() == null && b.getTimestamp() == null) return 0;
                    if (a.getTimestamp() == null) return 1;
                    if (b.getTimestamp() == null) return -1;

                    return b.getTimestamp().compareTo(a.getTimestamp());
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * Vybere kandidáta z RESERVED pro cílovou pozici.
     *
     * Kritéria:
     * - status musí být RESERVED,
     * - musí dávat smysl kategorie (např. útočník/obránce/brankář),
     * - preferuje primary/secondary,
     * - při konfliktu upřednostňuje nejpozději registrovaného hráče.
     */
    private MatchRegistrationEntity pickBestReserveCandidateForTargetPosition(
            PlayerPosition targetPosition,
            List<MatchRegistrationEntity> teamRegs
    ) {

        if (targetPosition == null) {
            return null;
        }

        var targetCategory = PlayerPositionUtil.getCategory(targetPosition);
        if (targetCategory == null) {
            return null;
        }

        return teamRegs.stream()
                .filter(reg -> reg.getStatus() == PlayerMatchStatus.RESERVED)
                .filter(reg -> {
                    PlayerPosition base = normalizePosition(reg);
                    if (base == null) {
                        return false;
                    }

                    if (PlayerPositionUtil.isGoalie(base) && !PlayerPositionUtil.isGoalie(targetPosition)) {
                        return false;
                    }

                    var baseCat = PlayerPositionUtil.getCategory(base);
                    return baseCat != null && baseCat == targetCategory;
                })
                .sorted((a, b) -> {
                    int scoreA = preferenceScore(a, targetPosition);
                    int scoreB = preferenceScore(b, targetPosition);

                    if (scoreA != scoreB) {
                        return Integer.compare(scoreB, scoreA);
                    }

                    if (a.getTimestamp() == null && b.getTimestamp() == null) return 0;
                    if (a.getTimestamp() == null) return 1;
                    if (b.getTimestamp() == null) return -1;

                    return b.getTimestamp().compareTo(a.getTimestamp());
                })
                .findFirst()
                .orElse(null);
    }

    private int preferenceScore(MatchRegistrationEntity reg, PlayerPosition targetPosition) {
        if (reg == null || reg.getPlayer() == null || targetPosition == null) {
            return 0;
        }
        PlayerEntity p = reg.getPlayer();
        PlayerPosition primary = p.getPrimaryPosition();
        PlayerPosition secondary = p.getSecondaryPosition();

        if (primary == targetPosition) {
            return 2;
        }
        if (secondary == targetPosition) {
            return 1;
        }
        return 0;
    }

    private PlayerPosition normalizePosition(MatchRegistrationEntity reg) {
        if (reg == null) {
            return null;
        }
        PlayerPosition pos = reg.getPositionInMatch();
        if (pos != null && pos != PlayerPosition.ANY) {
            return pos;
        }
        PlayerEntity p = reg.getPlayer();
        if (p == null) {
            return null;
        }
        PlayerPosition primary = p.getPrimaryPosition();
        return (primary != null && primary != PlayerPosition.ANY) ? primary : null;
    }
}