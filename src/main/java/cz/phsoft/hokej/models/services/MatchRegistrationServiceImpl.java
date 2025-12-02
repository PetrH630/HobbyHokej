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
        MatchRegistrationEntity reg = saveStatus(matchId, playerId, null, null, "user", null);
        recalcStatusesForMatch(matchId); // po registraci přepočítat statusy
        return reg;
    }

    @Override
    public MatchRegistrationEntity unregisterPlayer(Long matchId, Long playerId, String note, String reason) {
        ExcuseReason excuseReason = ExcuseReason.valueOf(reason.toUpperCase());
        MatchRegistrationEntity reg = saveStatus(matchId, playerId, excuseReason, note, "user", PlayerMatchStatus.UNREGISTERED);
        recalcStatusesForMatch(matchId); // po odhlášení přepočítat statusy
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
        // Získáme všechny hráče
        List<PlayerEntity> allPlayers = playerRepository.findAll();
        // Vrátíme seznam posledních registrací pouze pro hráče, kteří mají registraci na daný zápas
        return allPlayers.stream()
                .map(player -> getLastStatus(matchId, player.getId()))
                .filter(status -> status != null) // ignorujeme hráče bez registrace
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

        PlayerMatchStatus status;
        if (forcedStatus != null) {
            status = forcedStatus;
        } else {
            long registeredCount = registrationRepository.findByMatchId(matchId).stream()
                    .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                    .count();

            status = registeredCount < match.getMaxPlayers()
                    ? PlayerMatchStatus.REGISTERED
                    : PlayerMatchStatus.RESERVED;
        }

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

    // --- přepočítání statusů pro všechny hráče zápasu ---
    public void recalcStatusesForMatch(Long matchId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        int maxPlayers = match.getMaxPlayers();

// Vezmeme poslední registrace všech hráčů pro zápas
        List<MatchRegistrationEntity> lastRegs = registrationRepository.findByMatchId(matchId).stream()
                .collect(Collectors.groupingBy(r -> r.getPlayer().getId(),
                        Collectors.collectingAndThen(
                                Collectors.maxBy((r1, r2) -> r1.getTimestamp().compareTo(r2.getTimestamp())),
                                opt -> opt.orElse(null)
                        )))
                .values().stream()
                .filter(r -> r != null)
                .collect(Collectors.toList());

// Oddělíme hráče, kteří jsou aktivně REGISTERED nebo RESERVED
        List<MatchRegistrationEntity> activePlayers = lastRegs.stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED
                        || r.getStatus() == PlayerMatchStatus.RESERVED)
                .sorted((r1, r2) -> r1.getTimestamp().compareTo(r2.getTimestamp()))
                .collect(Collectors.toList());

        for (int i = 0; i < activePlayers.size(); i++) {
            MatchRegistrationEntity oldReg = activePlayers.get(i);
            PlayerMatchStatus newStatus = i < maxPlayers ? PlayerMatchStatus.REGISTERED : PlayerMatchStatus.RESERVED;

            if (oldReg.getStatus() != newStatus) {
                MatchRegistrationEntity newReg = new MatchRegistrationEntity();
                newReg.setMatch(match);
                newReg.setPlayer(oldReg.getPlayer());
                newReg.setStatus(newStatus);
                newReg.setExcuseReason(null);
                newReg.setExcuseNote(null);
                newReg.setTimestamp(LocalDateTime.now());
                newReg.setCreatedBy("system");

                registrationRepository.save(newReg);

            }
        }
    }
}
