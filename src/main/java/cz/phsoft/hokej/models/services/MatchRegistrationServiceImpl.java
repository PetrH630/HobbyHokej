package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchRegistrationServiceImpl implements MatchRegistrationService {

    private final MatchRegistrationRepository registrationRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    public MatchRegistrationServiceImpl(MatchRegistrationRepository registrationRepository,
                                        MatchRepository matchRepository,
                                        PlayerRepository playerRepository) {
        this.registrationRepository = registrationRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
    }

    @Override
    public MatchRegistrationEntity registerPlayer(Long matchId, Long playerId) {
        return saveStatus(matchId, playerId, PlayerMatchStatus.REGISTERED, null, null);
    }

    @Override
    public MatchRegistrationEntity unregisterPlayer(Long matchId, Long playerId) {
        return saveStatus(matchId, playerId, PlayerMatchStatus.UNREGISTERED, null, null);
    }

    @Override
    public MatchRegistrationEntity excusePlayer(Long matchId, Long playerId, String note, String reason) {
        ExcuseReason excuseReason = ExcuseReason.valueOf(reason.toUpperCase());
        return saveStatus(matchId, playerId, PlayerMatchStatus.EXCUSED, excuseReason, note);
    }

    @Override
    public MatchRegistrationEntity getLastStatus(Long matchId, Long playerId) {
        return registrationRepository
                .findTopByPlayerIdAndMatchIdOrderByTimestampDesc(playerId, matchId)
                .orElse(null);
    }

    @Override
    public List<MatchRegistrationEntity> getRegistrationsForMatch(Long matchId) {
        return registrationRepository.findByMatchId(matchId);
    }

        // --- pomocná metoda ---
    private MatchRegistrationEntity saveStatus(Long matchId, Long playerId, PlayerMatchStatus status,
                                               ExcuseReason excuseReason, String note) {

        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        MatchRegistrationEntity registration = new MatchRegistrationEntity();
        registration.setMatch(match);
        registration.setPlayer(player);
        registration.setStatus(status);
        registration.setExcuseReason(excuseReason);
        registration.setExcuseNote(note);
        registration.setTimestamp(LocalDateTime.now());

        return registrationRepository.save(registration);
    }
    @Override
    public List<MatchRegistrationEntity> getAllRegistrations() {
        return registrationRepository.findAll();
    }

    @Override
    public List<MatchRegistrationEntity> getRegistrationsForPlayer(Long playerId) {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
        return registrationRepository.findByPlayer(player);
    }

}
