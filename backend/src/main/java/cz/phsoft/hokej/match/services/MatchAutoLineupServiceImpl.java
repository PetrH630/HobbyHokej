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
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        List<MatchRegistrationEntity> registered =
                registrationRepository.findByMatchIdAndStatus(
                        matchId,
                        PlayerMatchStatus.REGISTERED
                );

        if (registered.isEmpty()) {
            return;
        }

        boolean changed = false;
        changed |= autoArrangeForTeam(match, Team.DARK, registered);
        changed |= autoArrangeForTeam(match, Team.LIGHT, registered);

        if (changed) {
            registrationRepository.saveAll(registered);
        }
    }

    private boolean autoArrangeForTeam(MatchEntity match,
                                       Team team,
                                       List<MatchRegistrationEntity> allRegistered) {

        Integer maxPlayers = match.getMaxPlayers();
        MatchMode mode = match.getMatchMode();

        if (maxPlayers == null || maxPlayers <= 0 || mode == null || team == null) {
            return false;
        }

        int slotsPerTeam = maxPlayers / 2;

        Map<PlayerPosition, Integer> perTeamCapacity =
                MatchModeLayoutUtil.buildPositionCapacityForMode(mode, slotsPerTeam);

        // Registrace pro daný tým
        List<MatchRegistrationEntity> teamRegs = allRegistered.stream()
                .filter(r -> r.getTeam() == team)
                .toList();

        if (teamRegs.isEmpty()) {
            return false;
        }

        // Aktuální obsazenost podle normalizePosition (positionInMatch / primary)
        Map<PlayerPosition, List<MatchRegistrationEntity>> regsByPosition =
                teamRegs.stream()
                        .collect(Collectors.groupingBy(
                                this::normalizePosition,
                                () -> new EnumMap<>(PlayerPosition.class),
                                Collectors.toList()
                        ));

        // Target pozice: ty, kde je kapacita > 0 a aktuálně tam nikdo nesedí
        List<PlayerPosition> targetPositions = perTeamCapacity.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue() > 0)
                .map(Map.Entry::getKey)
                .filter(pos -> {
                    List<MatchRegistrationEntity> list = regsByPosition.get(pos);
                    int occupied = (list == null) ? 0 : list.size();
                    return occupied == 0;
                })
                .toList();

        if (targetPositions.isEmpty()) {
            return false;
        }

        boolean changed = false;

        for (PlayerPosition target : targetPositions) {
            boolean filled = tryFillTargetPosition(
                    target,
                    teamRegs,
                    regsByPosition,
                    perTeamCapacity,
                    match,
                    team
            );
            if (filled) {
                changed = true;
            }
        }

        return changed;
    }

    private boolean tryFillTargetPosition(PlayerPosition targetPosition,
                                          List<MatchRegistrationEntity> teamRegs,
                                          Map<PlayerPosition, List<MatchRegistrationEntity>> regsByPosition,
                                          Map<PlayerPosition, Integer> perTeamCapacity, MatchEntity match,
                                          Team team) {

        MatchRegistrationEntity candidate = pickBestCandidateForTargetPosition(
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
                "AUTO_LINEUP matchId={}, team={}, playerId={}, playerName{}, playerSurname{}, from={}, to={}",
                match.getId(),
                team,
                candidate.getPlayer().getId(),
                candidate.getPlayer().getName(),
                candidate.getPlayer().getSurname(),
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

    /**
     * Kandidát je:
     *  - REGISTERED,
     *  - stejný tým (řešeno dřív),
     *  - stejná kategorie (FORWARD/DEFENSE/GOALIE),
     *  - donor pozice je buď:
     *      - bez definované kapacity (např. FORWARD v módu, kde se nepoužívá),
     *      - nebo má obsazenost > 1 (tj. po přesunu tam pořád někdo zůstane).
     *
     *  Preferuje se:
     *  - primary == targetPosition (score 2),
     *  - secondary == targetPosition (score 1),
     *  - ostatní (0),
     *  a pak nejpozději registrovaný (timestamp DESC).
     */
    private MatchRegistrationEntity pickBestCandidateForTargetPosition(
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

                    // GOALIE jen na GOALIE
                    if (PlayerPositionUtil.isGoalie(current)
                            && !PlayerPositionUtil.isGoalie(targetPosition)) {
                        return false;
                    }

                    // Stejná kategorie (FORWARD/DEFENSE/GOALIE)
                    var currentCat = PlayerPositionUtil.getCategory(current);
                    if (currentCat == null || currentCat != targetCategory) {
                        return false;
                    }

                    if (current == targetPosition) {
                        // už sedí na targetu, nemáme koho přesouvat
                        return false;
                    }

                    // Kolik lidí je na "source" pozici
                    List<MatchRegistrationEntity> list = regsByPosition.get(current);
                    int occupied = (list == null) ? 0 : list.size();

                    // Jaká je kapacita source pozice (může být null, pokud v módu není post definován)
                    Integer sourceCapacity = perTeamCapacity.get(current);

                    // Donor je povolen, pokud:
                    //  - sourceCapacity == null → globální FORWARD / "bez slotu",
                    //  - nebo má obsazenost > 1 → po přesunu tam pořád někdo zůstane.
                    if (sourceCapacity == null) {
                        return true;
                    }
                    return occupied > 1;
                })
                .sorted((a, b) -> {
                    int scoreA = preferenceScore(a, targetPosition);
                    int scoreB = preferenceScore(b, targetPosition);

                    if (scoreA != scoreB) {
                        return Integer.compare(scoreB, scoreA); // vyšší score první
                    }

                    // Nejmladší – nejpozději registrovaný jako první kandidát k přesunu
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

    /**
     * Pokud registrace nemá explicitní pozici, bere se primaryPosition hráče.
     * ANY se ignoruje.
     */
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