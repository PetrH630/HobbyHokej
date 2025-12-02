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
import java.util.Set;
import java.util.stream.Collectors;

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
        return saveStatus(matchId, playerId, null, null, "user", null);
    }

    @Override
    public MatchRegistrationEntity unregisterPlayer(Long matchId, Long playerId) {
        return saveStatus(matchId, playerId, null, null, "user", PlayerMatchStatus.UNREGISTERED);
    }

    @Override
    public MatchRegistrationEntity excusePlayer(Long matchId, Long playerId, String note, String reason) {
        ExcuseReason excuseReason = ExcuseReason.valueOf(reason.toUpperCase());
        return saveStatus(matchId, playerId, excuseReason, note, "user", PlayerMatchStatus.EXCUSED);
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

    @Override
    public List<PlayerEntity> getNoResponsePlayers(Long matchId) {
        List<PlayerEntity> allPlayers = playerRepository.findAll();
        List<MatchRegistrationEntity> registrations = registrationRepository.findByMatchId(matchId);

        Set<Long> respondedPlayerIds = registrations.stream()
                .map(r -> r.getPlayer().getId())
                .collect(Collectors.toSet());

        return allPlayers.stream()
                .filter(p -> !respondedPlayerIds.contains(p.getId()))
                .toList();
    }

    // --- interní metoda pro ukládání statusu hráče ---
    private MatchRegistrationEntity saveStatus(Long matchId,
                                               Long playerId,
                                               ExcuseReason excuseReason,
                                               String note,
                                               String actionBy,
                                               PlayerMatchStatus forcedStatus) {

        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        // pokud je status předán, použije se
        PlayerMatchStatus status;
        if (forcedStatus != null) {
            status = forcedStatus;
        } else {
            // jinak rozhodni podle počtu registrovaných hráčů
            long registeredCount = registrationRepository.findByMatchId(matchId).stream()
                    .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                    .count();

            status = registeredCount < match.getMaxPlayers()
                    ? PlayerMatchStatus.REGISTERED
                    : PlayerMatchStatus.RESERVED;
        }

        // rozhodnutí, kdo vytvořil záznam
        String createdBy = "system";
        if ("user".equalsIgnoreCase(actionBy)) {
            createdBy = "user";
        }

        MatchRegistrationEntity registration = new MatchRegistrationEntity();
        registration.setMatch(match);
        registration.setPlayer(player);
        registration.setStatus(status);
        registration.setExcuseReason(excuseReason);
        registration.setExcuseNote(note);
        registration.setTimestamp(LocalDateTime.now());
        registration.setCreatedBy(createdBy);

        return registrationRepository.save(registration);
    }


}
