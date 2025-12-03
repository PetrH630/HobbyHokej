package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.enums.JerseyColor;
import cz.phsoft.hokej.data.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.DuplicateRegistrationException;
import jakarta.transaction.Transactional;
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

    public MatchRegistrationServiceImpl(
            MatchRegistrationRepository registrationRepository,
            MatchRepository matchRepository,
            PlayerRepository playerRepository) {

        this.registrationRepository = registrationRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
    }

    // -----------------------------------
    // Veřejné metody
    // -----------------------------------

    @Override
    @Transactional
    public MatchRegistrationEntity registerPlayer(Long matchId, Long playerId, JerseyColor jerseyColor, String adminNote) {
        MatchRegistrationEntity current = registrationRepository
                .findTopByPlayerIdAndMatchIdOrderByTimestampDesc(playerId, matchId)
                .orElse(null);

        if (current != null) {
            throw new DuplicateRegistrationException(
                    "Hráč již má registraci k zápasu"
            );
        }

        return saveStatus(matchId, playerId, null, null, PlayerMatchStatus.REGISTERED, jerseyColor, adminNote);
    }

    @Override
    @Transactional
    public MatchRegistrationEntity unregisterPlayer(Long matchId, Long playerId, String note, String reason) {
        MatchRegistrationEntity current = registrationRepository
                .findTopByPlayerIdAndMatchIdOrderByTimestampDesc(playerId, matchId)
                .orElseThrow(() -> new RuntimeException("Hráč nemá registraci k zápasu"));

        current.setStatus(PlayerMatchStatus.UNREGISTERED);
        current.setExcuseNote(note);
        current.setExcuseReason(ExcuseReason.valueOf(reason.toUpperCase()));
        current.setTimestamp(LocalDateTime.now());
        return registrationRepository.save(current);

    }

    @Override
    @Transactional
    public MatchRegistrationEntity excusePlayer(Long matchId, Long playerId, String note, String reason) {
        MatchRegistrationEntity current = registrationRepository
                .findTopByPlayerIdAndMatchIdOrderByTimestampDesc(playerId, matchId)
                .orElse(null);

        if (current != null) {
            throw new DuplicateRegistrationException(
                    "Hráč již má registraci k zápasu, nelze omluvit"
            );
        }

        return saveStatus(matchId, playerId, ExcuseReason.valueOf(reason.toUpperCase()), note, PlayerMatchStatus.EXCUSED, null, null);
    }

    // -----------------------------------
    // Získání registrací
    // -----------------------------------

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

    // -----------------------------------
    // CORE METHOD – validace + ukládání
    // -----------------------------------

    @Transactional
    private MatchRegistrationEntity saveStatus(Long matchId,
                                               Long playerId,
                                               ExcuseReason excuseReason,
                                               String note,
                                               PlayerMatchStatus forcedStatus,
                                               JerseyColor jerseyColor,
                                               String adminNote) {

        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        PlayerMatchStatus status = (forcedStatus != null)
                ? forcedStatus
                : determineStatus(matchId, match);

        MatchRegistrationEntity reg = new MatchRegistrationEntity();
        reg.setMatch(match);
        reg.setPlayer(player);
        reg.setStatus(status);
        reg.setExcuseReason(excuseReason);
        reg.setExcuseNote(note);
        reg.setJerseyColor(jerseyColor);
        reg.setAdminNote(adminNote);
        reg.setTimestamp(LocalDateTime.now());
        reg.setCreatedBy("user");

        return registrationRepository.save(reg);
    }

    // -----------------------------------
    // Automatický status REGISTERED / RESERVED
    // -----------------------------------
    private PlayerMatchStatus determineStatus(Long matchId, MatchEntity match) {
        long registeredCount = registrationRepository.findByMatchId(matchId).stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .count();
        return registeredCount < match.getMaxPlayers()
                ? PlayerMatchStatus.REGISTERED
                : PlayerMatchStatus.RESERVED;
    }

    // -----------------------------------
    // PŘEPOČET STATUSŮ – jen REGISTERED / RESERVED
    // -----------------------------------
    @Transactional
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

        List<MatchRegistrationEntity> active = latest.values().stream()
                .filter(r -> r != null)
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED
                        || r.getStatus() == PlayerMatchStatus.RESERVED)
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .toList();

        for (int i = 0; i < active.size(); i++) {
            PlayerMatchStatus newStatus = (i < maxPlayers) ? PlayerMatchStatus.REGISTERED : PlayerMatchStatus.RESERVED;
            MatchRegistrationEntity old = active.get(i);

            if (old.getStatus() != newStatus) {
                old.setStatus(newStatus);
                old.setTimestamp(LocalDateTime.now());
                old.setCreatedBy("system");
                registrationRepository.save(old);
            }
        }
    }
}
