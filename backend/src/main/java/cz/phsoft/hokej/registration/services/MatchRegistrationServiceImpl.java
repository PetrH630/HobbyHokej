package cz.phsoft.hokej.registration.services;

import cz.phsoft.hokej.registration.enums.ExcuseReason;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.player.enums.PlayerPosition;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.registration.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.player.dto.PlayerDTO;
import cz.phsoft.hokej.registration.dto.MatchRegistrationRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service vrstva pro orchestraci nad registracemi hráčů na zápasy.
 *
 * Příkazové (write) operace jsou delegovány do {@link MatchRegistrationCommandService}.
 * Čtecí operace jsou delegovány do {@link MatchRegistrationQueryService}.
 *
 * Slouží jako jednotné rozhraní pro kontrolery, které tak nemusí znát
 * rozdělení na command/query služby.
 */
@Service
public class MatchRegistrationServiceImpl implements MatchRegistrationService {

    private final MatchRegistrationCommandService commandService;
    private final MatchRegistrationQueryService matchRegistrationQueryService;

    public MatchRegistrationServiceImpl(
            MatchRegistrationCommandService commandService,
            MatchRegistrationQueryService matchRegistrationQueryService
    ) {
        this.commandService = commandService;
        this.matchRegistrationQueryService = matchRegistrationQueryService;
    }

    // ==========================================
    // PŘÍKAZOVÉ OPERACE – DELEGACE DO COMMAND SERVICE
    // ==========================================

    @Override
    @Transactional
    public MatchRegistrationDTO upsertRegistration(Long playerId, MatchRegistrationRequest request) {
        return commandService.upsertRegistration(playerId, request);
    }

    @Override
    @Transactional
    public MatchRegistrationDTO markNoExcused(Long matchId,
                                              Long playerId,
                                              String adminNote) {
        return commandService.markNoExcused(matchId, playerId, adminNote);
    }

    @Override
    @Transactional
    public MatchRegistrationDTO cancelNoExcused(Long matchId,
                                                Long playerId,
                                                ExcuseReason excuseReason,
                                                String excuseNote) {
        return commandService.cancelNoExcused(matchId, playerId, excuseReason, excuseNote);
    }

    @Override
    @Transactional
    public MatchRegistrationDTO changeRegistrationTeam(Long playerId,
                                                       Long matchId) {
        return commandService.changeRegistrationTeam(playerId, matchId);
    }

    @Override
    @Transactional
    public MatchRegistrationDTO changeRegistrationPosition(Long playerId,
                                                           Long matchId,
                                                           PlayerPosition positionInMatch) {
        return commandService.changeRegistrationPosition(playerId, matchId, positionInMatch);
    }

    @Override
    @Transactional
    public MatchRegistrationDTO updateStatus(Long matchId,
                                             Long playerId,
                                             PlayerMatchStatus status) {
        return commandService.updateStatus(matchId, playerId, status);
    }

    @Override
    @Transactional
    public void recalcStatusesForMatch(Long matchId) {
        commandService.recalcStatusesForMatch(matchId);
    }

    @Override
    @Transactional
    public void promoteReservedCandidatesForCapacityIncrease(Long matchId,
                                                             Team freedTeam,
                                                             PlayerPosition freedPosition,
                                                             int slotsCount) {
        commandService.promoteReservedCandidatesForCapacityIncrease(
                matchId,
                freedTeam,
                freedPosition,
                slotsCount
        );
    }

    @Transactional
    public void sendSmsToRegisteredPlayers(Long matchId) {
        commandService.sendSmsToRegisteredPlayers(matchId);
    }

    // ======================================
    // ČTECÍ OPERACE – DELEGACE DO QUERY SERVICE
    // ======================================

    @Override
    public List<MatchRegistrationDTO> getRegistrationsForMatch(Long matchId) {
        return matchRegistrationQueryService.getRegistrationsForMatch(matchId);
    }

    @Override
    public List<MatchRegistrationDTO> getRegistrationsForMatches(List<Long> matchIds) {
        return matchRegistrationQueryService.getRegistrationsForMatches(matchIds);
    }

    @Override
    public List<MatchRegistrationDTO> getAllRegistrations() {
        return matchRegistrationQueryService.getAllRegistrations();
    }

    @Override
    public List<MatchRegistrationDTO> getRegistrationsForPlayer(Long playerId) {
        return matchRegistrationQueryService.getRegistrationsForPlayer(playerId);
    }

    @Override
    public List<PlayerDTO> getNoResponsePlayers(Long matchId) {
        return matchRegistrationQueryService.getNoResponsePlayers(matchId);
    }
}