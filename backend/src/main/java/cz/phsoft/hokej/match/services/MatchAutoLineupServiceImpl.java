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
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementace služby pro automatické přeskupení první lajny.
 *
 * Algoritmus:
 * - pracuje pouze s hráči ve stavu REGISTERED,
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

    /**
     * Spustí automatické přeskupení první lajny pro oba týmy zápasu.
     *
     * Operace probíhá transakčně.
     *
     * @param matchId identifikátor zápasu
     */
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

    /**
     * Provede automatické přeskupení pro jeden konkrétní tým.
     *
     * @param match zápas
     * @param team tým DARK nebo LIGHT
     * @param allRegistered všichni REGISTERED hráči zápasu
     * @return true pokud došlo ke změně
     */
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

        List<MatchRegistrationEntity> teamRegs = allRegistered.stream()
                .filter(r -> r.getTeam() == team)
                .toList();

        if (teamRegs.isEmpty()) {
            return false;
        }

        Map<PlayerPosition, List<MatchRegistrationEntity>> regsByPosition =
                teamRegs.stream()
                        .collect(Collectors.groupingBy(
                                this::normalizePosition,
                                () -> new EnumMap<>(PlayerPosition.class),
                                Collectors.toList()
                        ));

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

    /**
     * Pokusí se obsadit konkrétní cílovou pozici vhodným kandidátem.
     */
    private boolean tryFillTargetPosition(PlayerPosition targetPosition,
                                          List<MatchRegistrationEntity> teamRegs,
                                          Map<PlayerPosition, List<MatchRegistrationEntity>> regsByPosition,
                                          Map<PlayerPosition, Integer> perTeamCapacity,
                                          MatchEntity match,
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
                "AUTO_LINEUP matchId={}, team={}, playerId={}, from={}, to={}",
                match.getId(),
                team,
                candidate.getPlayer().getId(),
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
     * Vybere nejvhodnějšího kandidáta pro cílovou pozici.
     *
     * Kritéria:
     * - stejná kategorie pozice,
     * - donor pozice má přebytek,
     * - preference primary/secondary,
     * - nejpozději registrovaný hráč.
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
     * Vrací efektivní pozici hráče v zápase.
     * Pokud není explicitně nastavena, použije se primaryPosition.
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