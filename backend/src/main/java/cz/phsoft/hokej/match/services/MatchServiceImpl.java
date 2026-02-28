package cz.phsoft.hokej.match.services;

import cz.phsoft.hokej.match.dto.MatchDTO;
import cz.phsoft.hokej.match.dto.MatchDetailDTO;
import cz.phsoft.hokej.match.dto.MatchOverviewDTO;
import cz.phsoft.hokej.match.enums.MatchCancelReason;
import cz.phsoft.hokej.shared.dto.SuccessResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Fasádní implementace service vrstvy pro práci se zápasy.
 *
 * Tato třída zachovává původní rozhraní {@link MatchService}, ale veškerou
 * skutečnou logiku deleguje do dvou specializovaných služeb:
 *
 * - {@link MatchQueryService} pro čtecí operace (seznamy zápasů, detail zápasu,
 *   přehledy pro hráče),
 * - {@link MatchCommandService} pro změnové operace (vytváření, úprava, mazání,
 *   zrušení a obnovení zápasu).
 *
 * Díky tomu zůstává API vůči controllerům stabilní, ale interně je logika
 * rozdělená podle principu CQRS (commands vs. queries).
 */
@Service
public class MatchServiceImpl implements MatchService {

    private final MatchQueryService matchQueryService;
    private final MatchCommandService matchCommandService;

    public MatchServiceImpl(
            MatchQueryService matchQueryService,
            MatchCommandService matchCommandService
    ) {
        this.matchQueryService = matchQueryService;
        this.matchCommandService = matchCommandService;
    }

    // ======================
    // ZÁKLADNÍ SEZNAMY ZÁPASŮ (READ)
    // ======================

    /**
     * {@inheritDoc}
     *
     * Deleguje se do {@link MatchQueryService#getAllMatches()}.
     */
    @Override
    public List<MatchDTO> getAllMatches() {
        return matchQueryService.getAllMatches();
    }

    /**
     * {@inheritDoc}
     *
     * Deleguje se do {@link MatchQueryService#getUpcomingMatches()}.
     */
    @Override
    public List<MatchDTO> getUpcomingMatches() {
        return matchQueryService.getUpcomingMatches();
    }

    /**
     * {@inheritDoc}
     *
     * Deleguje se do {@link MatchQueryService#getPastMatches()}.
     */
    @Override
    public List<MatchDTO> getPastMatches() {
        return matchQueryService.getPastMatches();
    }

    /**
     * {@inheritDoc}
     *
     * Deleguje se do {@link MatchQueryService#getNextMatch()}.
     */
    @Override
    public MatchDTO getNextMatch() {
        return matchQueryService.getNextMatch();
    }

    /**
     * {@inheritDoc}
     *
     * Deleguje se do {@link MatchQueryService#getMatchById(Long)}.
     */
    @Override
    public MatchDTO getMatchById(Long id) {
        return matchQueryService.getMatchById(id);
    }

    // ======================
    // COMMANDS – CREATE / UPDATE / DELETE / CANCEL / UN-CANCEL
    // ======================

    /**
     * {@inheritDoc}
     *
     * Deleguje se do {@link MatchCommandService#createMatch(MatchDTO)}.
     */
    @Override
    public MatchDTO createMatch(MatchDTO dto) {
        return matchCommandService.createMatch(dto);
    }

    /**
     * {@inheritDoc}
     *
     * Deleguje se do {@link MatchCommandService#updateMatch(Long, MatchDTO)}.
     */
    @Override
    public MatchDTO updateMatch(Long id, MatchDTO dto) {
        return matchCommandService.updateMatch(id, dto);
    }

    /**
     * {@inheritDoc}
     *
     * Deleguje se do {@link MatchCommandService#deleteMatch(Long)}.
     */
    @Override
    public SuccessResponseDTO deleteMatch(Long id) {
        return matchCommandService.deleteMatch(id);
    }

    /**
     * {@inheritDoc}
     *
     * Deleguje se do {@link MatchCommandService#cancelMatch(Long, MatchCancelReason)}.
     */
    @Override
    @Transactional
    public SuccessResponseDTO cancelMatch(Long matchId, MatchCancelReason reason) {
        return matchCommandService.cancelMatch(matchId, reason);
    }

    /**
     * {@inheritDoc}
     *
     * Deleguje se do {@link MatchCommandService#unCancelMatch(Long)}.
     */
    @Override
    @Transactional
    public SuccessResponseDTO unCancelMatch(Long matchId) {
        return matchCommandService.unCancelMatch(matchId);
    }

    // ======================
    // DETAIL ZÁPASU A PŘEHLEDY (READ)
    // ======================

    /**
     * {@inheritDoc}
     *
     * Deleguje se do {@link MatchQueryService#getMatchDetail(Long)}.
     */
    @Override
    public MatchDetailDTO getMatchDetail(Long id) {
        return matchQueryService.getMatchDetail(id);
    }

    /**
     * {@inheritDoc}
     *
     * Deleguje se do {@link MatchQueryService#getAvailableMatchesForPlayer(Long)}.
     */
    @Override
    public List<MatchDTO> getAvailableMatchesForPlayer(Long playerId) {
        return matchQueryService.getAvailableMatchesForPlayer(playerId);
    }

    /**
     * {@inheritDoc}
     *
     * Deleguje se do {@link MatchQueryService#getPlayerIdByEmail(String)}.
     */
    @Override
    public Long getPlayerIdByEmail(String email) {
        return matchQueryService.getPlayerIdByEmail(email);
    }

    /**
     * {@inheritDoc}
     *
     * Deleguje se do
     * {@link MatchQueryService#getUpcomingMatchesOverviewForPlayer(Long)}.
     */
    @Override
    public List<MatchOverviewDTO> getUpcomingMatchesOverviewForPlayer(Long playerId) {
        return matchQueryService.getUpcomingMatchesOverviewForPlayer(playerId);
    }

    /**
     * {@inheritDoc}
     *
     * Deleguje se do {@link MatchQueryService#getUpcomingMatchesForPlayer(Long)}.
     */
    @Override
    public List<MatchDTO> getUpcomingMatchesForPlayer(Long playerId) {
        return matchQueryService.getUpcomingMatchesForPlayer(playerId);
    }

    /**
     * {@inheritDoc}
     *
     * Deleguje se do {@link MatchQueryService#getAllPassedMatchesForPlayer(Long)}.
     */
    @Override
    public List<MatchOverviewDTO> getAllPassedMatchesForPlayer(Long playerId) {
        return matchQueryService.getAllPassedMatchesForPlayer(playerId);
    }
}