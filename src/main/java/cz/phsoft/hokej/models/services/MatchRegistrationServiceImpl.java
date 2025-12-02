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
import jakarta.transaction.Transactional;
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

    // Veřejné metody
    // vytvoří registraci hráče na zápas
    @Override
    public MatchRegistrationEntity registerPlayer(Long matchId, Long playerId) {
        MatchRegistrationEntity reg = saveStatus(matchId, playerId, null, null, "user", null);
        recalcStatusesForMatch(matchId);
        return reg;
    }

    // vytvoří záznam o zrušení registrace hráče na zápas - součástí bude i důvod z enum excuse reason a možnost
    // vložit poznámku, user - odregistrovat může jen user, záznam zůstává v match_registration, předchozí záznamy o
    // registraci se přesunou do match_registration_history
    @Override
    public MatchRegistrationEntity unregisterPlayer(Long matchId, Long playerId, String note, String reason) {
        ExcuseReason excuseReason = ExcuseReason.valueOf(reason.toUpperCase());
        MatchRegistrationEntity reg = saveStatus(matchId, playerId, excuseReason, note,"user", PlayerMatchStatus.UNREGISTERED);
        recalcStatusesForMatch(matchId); // ← správné místo!
        return reg;
    }

    // vytvoří záznam o omluvení hráče na zápas - součástí bude i důvod z enum excuse reason a možnost
    // vložit poznámku, omluvit může jen user, záznam zůstává v match_registration, předchozí záznamy o
    // registraci se přesunou do match_registration_history
    @Override
    public MatchRegistrationEntity excusePlayer(Long matchId, Long playerId, String note, String reason) {
        ExcuseReason excuseReason = ExcuseReason.valueOf(reason.toUpperCase());
        MatchRegistrationEntity reg =saveStatus(matchId, playerId, excuseReason, note, "user", PlayerMatchStatus.EXCUSED);
        recalcStatusesForMatch(matchId);
        return reg;
    }

    // získá poslední status status u konkrétního zápasu konkrétního hráče. UŽ JE ZBYTEČNÉ - v tabulce
    // match_registration už je vždy k jednomu hráči a jednomu zápasu vždy jen jeden záznam, zbytek
    // je v match_registration_history
    @Override
    public MatchRegistrationEntity getLastStatus(Long matchId, Long playerId) {
        return registrationRepository
                .findTopByPlayerIdAndMatchIdOrderByTimestampDesc(playerId, matchId)
                .orElse(null);
    }

    // získá všechny poslední statusy hráču u konkrétního zápasu. UŽ JE ZBYTEČNÉ - v tabulce
    // match_registration už je vždy k jednomu hráči a jednomu zápasu vždy jen jeden záznam, zbytek
    // je v match_registration_history
    @Override
    public List<MatchRegistrationEntity> getLastStatusesForMatch(Long matchId) {
        return playerRepository.findAll().stream()
                .map(p -> getLastStatus(matchId, p.getId()))
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    // Získá všechny registrace k zápasu dle id zápasu z match_registration v podstatě nahrazuje původní
    // getLastStatusesForMatch
    @Override
    public List<MatchRegistrationEntity> getRegistrationsForMatch(Long matchId) {
        return registrationRepository.findByMatchId(matchId);
    }

    // Získá všechny registrace ke všem zápasum z match_registration
    @Override
    public List<MatchRegistrationEntity> getAllRegistrations() {
        return registrationRepository.findAll();
    }

    // získá všechny registrace hráče dle id z tabulky match_registration
    @Override
    public List<MatchRegistrationEntity> getRegistrationsForPlayer(Long playerId) {
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
        return registrationRepository.findByPlayer(player);
    }

    // Získá všechny hráče, kteří dle id zápasu neprovedli žádnou registraci, omluvu, ani odhlášení - kašlou na to
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
// CORE METHOD – validace + ukládání
// --------------------------------------------------
    // Transakce, aby nedošlo k odstranění záznamu před vytvořením nového
    @Transactional
    private MatchRegistrationEntity saveStatus(Long matchId,
                                               Long playerId,
                                               ExcuseReason excuseReason,
                                               String note,
                                               String actionBy,
                                               PlayerMatchStatus forcedStatus) {

        // vyhledá zápas dle id
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));
        // vyhledá hráče dle id
        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        PlayerMatchStatus status = (forcedStatus != null)
                ? forcedStatus
                : determineStatus(matchId, match);


        MatchRegistrationEntity current = registrationRepository
                .findTopByPlayerIdAndMatchIdOrderByTimestampDesc(playerId, matchId)
                .orElse(null);

        // pokud existuje záznam k hráči a zápasu s požadovaným statusem - nelze se 2x přihlásit na jeden zápas...
        if (current != null) {
            if (current.getStatus() == status) {
                throw new DuplicateRegistrationException(
                        "Registrace se stejným statusem již existuje pro hráče " +
                                player.getFullName() + " na zápas " + match.getDateTime()
                );
            } else {
                // Přesun starého záznamu do historie - match_registration_history - neexistuje stejný status
                MatchRegistrationHistoryEntity hist = new MatchRegistrationHistoryEntity();
                hist.setPlayer(current.getPlayer());
                hist.setMatch(current.getMatch());
                hist.setStatus(current.getStatus());
                hist.setExcuseReason(current.getExcuseReason());
                hist.setExcuseNote(current.getExcuseNote());
                hist.setCreatedBy(current.getCreatedBy());
                hist.setCreatedAt(current.getTimestamp());
                hist.setChangedAt(LocalDateTime.now());
                hist.setChangedBy("system");
                registrationHistoryRepository.save(hist);
                // smazáni přesunutého záznamu z match_registration
                registrationRepository.delete(current);
            }
        }


        String createdBy = "user".equalsIgnoreCase(actionBy) ? "user" : "system";

        MatchRegistrationEntity reg = new MatchRegistrationEntity();
        reg.setMatch(match);
        reg.setPlayer(player);
        reg.setStatus(status);
        reg.setExcuseReason(excuseReason);
        reg.setExcuseNote(note);
        reg.setTimestamp(LocalDateTime.now());
        reg.setCreatedBy(createdBy);

        return registrationRepository.save(reg);
    }

    // --------------------------------------------------
// Určí REGISTERED/RESERVED podle aktuální kapacity
// --------------------------------------------------
    private PlayerMatchStatus determineStatus(Long matchId, MatchEntity match) {
        long registeredCount = registrationRepository.findByMatchId(matchId).stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .count();

        return registeredCount < match.getMaxPlayers()
                ? PlayerMatchStatus.REGISTERED
                : PlayerMatchStatus.RESERVED;
    }

    // --------------------------------------------------
// PŘEPOČET STATUSŮ – jen pro REGISTERED / RESERVED
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

        // Pouze REGISTERED / RESERVED hráči
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
                // Přesun starého záznamu do historie
                MatchRegistrationHistoryEntity hist = new MatchRegistrationHistoryEntity();
                hist.setMatch(old.getMatch());
                hist.setPlayer(old.getPlayer());
                hist.setStatus(old.getStatus());
                hist.setExcuseReason(old.getExcuseReason());
                hist.setExcuseNote(old.getExcuseNote());
                hist.setCreatedBy(old.getCreatedBy());
                hist.setCreatedAt(old.getTimestamp());
                hist.setChangedAt(LocalDateTime.now());
                hist.setChangedBy("system");

                registrationHistoryRepository.save(hist);

                registrationRepository.delete(old);

                // Nový záznam
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
