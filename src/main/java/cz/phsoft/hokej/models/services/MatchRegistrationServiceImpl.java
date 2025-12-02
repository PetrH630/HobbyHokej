package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationHistoryEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.repositories.MatchRegistrationHistoryRepository;
import cz.phsoft.hokej.data.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.DuplicateRegistrationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchRegistrationServiceImpl implements MatchRegistrationService {

    private final MatchRegistrationRepository registrationRepository;
    private final MatchRegistrationHistoryRepository registrationHistoryRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;

    public MatchRegistrationServiceImpl(
            MatchRegistrationRepository registrationRepository,
            MatchRegistrationHistoryRepository registrationHistoryRepository,
            MatchRepository matchRepository,
            PlayerRepository playerRepository) {

        this.registrationRepository = registrationRepository;
        this.registrationHistoryRepository = registrationHistoryRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
    }

    // --------------------------------------------------
    // PUBLIC SERVICE METHODS (čisté, bez try/catch)
    // --------------------------------------------------

    @Override
    public MatchRegistrationEntity registerPlayer(Long matchId, Long playerId) {
        MatchRegistrationEntity reg =
                saveStatus(matchId, playerId, null, null, "user", null);

        recalcStatusesForMatch(matchId);

        return reg;
    }

    @Override
    public MatchRegistrationEntity unregisterPlayer(Long matchId, Long playerId, String note, String reason) {

        ExcuseReason excuseReason = ExcuseReason.valueOf(reason.toUpperCase());

        MatchRegistrationEntity reg =
                saveStatus(matchId, playerId, excuseReason, note, "user", PlayerMatchStatus.UNREGISTERED);

        recalcStatusesForMatch(matchId);

        return reg;
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
    public List<MatchRegistrationEntity> getLastStatusesForMatch(Long matchId) {

        return playerRepository.findAll().stream()
                .map(p -> getLastStatus(matchId, p.getId()))
                .filter(r -> r != null)
                .collect(Collectors.toList());
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

        Set<Long> respondedIds = registrationRepository.findByMatchId(matchId).stream()
                .map(r -> r.getPlayer().getId())
                .collect(Collectors.toSet());

        return allPlayers.stream()
                .filter(p -> !respondedIds.contains(p.getId()))
                .collect(Collectors.toList());
    }


    // --------------------------------------------------
    // CORE METHOD – jediné místo validací + ukládání
    // --------------------------------------------------

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

        // Při registraci určujeme REGISTERED / RESERVED
        PlayerMatchStatus status = (forcedStatus != null)
                ? forcedStatus
                : determineStatus(matchId, match);

        // --- Ochrana proti duplicitnímu zápisu ---
        boolean exists = registrationRepository
                .findByPlayerIdAndMatchId(playerId, matchId)
                .stream()
                .anyMatch(r -> r.getStatus() == status);

        if (exists) {
            throw new DuplicateRegistrationException(
                    "Registrace se stejným statusem již existuje pro hráče " +
                            player.getFullName() + " na zápas " + match.getDateTime()
            );
        }

        String createdBy = "user".equalsIgnoreCase(actionBy) ? "user" : "system";

        // --- Uložíme registraci ---
        MatchRegistrationEntity reg = new MatchRegistrationEntity();
        reg.setMatch(match);
        reg.setPlayer(player);
        reg.setStatus(status);
        reg.setExcuseReason(excuseReason);
        reg.setExcuseNote(note);
        reg.setTimestamp(LocalDateTime.now());
        reg.setCreatedBy(createdBy);

        reg = registrationRepository.save(reg);

        // --- Historie ---
        MatchRegistrationHistoryEntity hist = new MatchRegistrationHistoryEntity();
        hist.setRegistration(reg);
        hist.setStatus(status);
        hist.setExcuseReason(excuseReason);
        hist.setExcuseNote(note);
        hist.setChangedAt(LocalDateTime.now());
        hist.setChangedBy(createdBy);

        registrationHistoryRepository.save(hist);

        return reg;
    }


    // Určí REGISTERED/RESERVED podle počtu hráčů
    private PlayerMatchStatus determineStatus(Long matchId, MatchEntity match) {
        long registeredCount = registrationRepository.findByMatchId(matchId).stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .count();

        return registeredCount < match.getMaxPlayers()
                ? PlayerMatchStatus.REGISTERED
                : PlayerMatchStatus.RESERVED;
    }


    // --------------------------------------------------
    // PŘEPOČET STATUSŮ – pokud někdo odpadne/změní se pořadí
    // --------------------------------------------------

    public void recalcStatusesForMatch(Long matchId) {

        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        int maxPlayers = match.getMaxPlayers();

        var latest = registrationRepository.findByMatchId(matchId).stream()
                .collect(Collectors.groupingBy(r -> r.getPlayer().getId(),
                        Collectors.collectingAndThen(
                                Collectors.maxBy((a, b) -> a.getTimestamp().compareTo(b.getTimestamp())),
                                opt -> opt.orElse(null)
                        )));

        // Aktivní statusy (REGISTERED/RESERVED)
        List<MatchRegistrationEntity> active = latest.values().stream()
                .filter(r -> r != null)
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED
                        || r.getStatus() == PlayerMatchStatus.RESERVED)
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .toList();

        // Nové statusy dle pořadí
        for (int i = 0; i < active.size(); i++) {

            PlayerMatchStatus newStatus =
                    (i < maxPlayers) ? PlayerMatchStatus.REGISTERED : PlayerMatchStatus.RESERVED;

            MatchRegistrationEntity old = active.get(i);

            if (old.getStatus() != newStatus) {

                MatchRegistrationEntity reg = new MatchRegistrationEntity();
                reg.setMatch(match);
                reg.setPlayer(old.getPlayer());
                reg.setStatus(newStatus);
                reg.setTimestamp(LocalDateTime.now());
                reg.setCreatedBy("system");

                registrationRepository.save(reg);
            }
        }
    }
}
