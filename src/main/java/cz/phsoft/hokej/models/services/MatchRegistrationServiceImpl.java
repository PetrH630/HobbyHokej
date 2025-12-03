package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.JerseyColor;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.models.dto.mappers.MatchRegistrationMapper;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;

@Service
public class MatchRegistrationServiceImpl implements MatchRegistrationService {
    private final MatchRegistrationRepository registrationRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final MatchRegistrationMapper matchRegistrationMapper;

    public MatchRegistrationServiceImpl(
            MatchRegistrationRepository registrationRepository,
            MatchRepository matchRepository,
            PlayerRepository playerRepository,
            MatchRegistrationMapper matchRegistrationMapper) {

        this.registrationRepository = registrationRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.matchRegistrationMapper = matchRegistrationMapper;
    }


    // POMOCNÉ METODY - NO BOILER CODE
    // JE VOLNÉ MÍSTO
    private boolean isSlotAvailable(Long matchId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        long registeredCount = registrationRepository.findByMatchId(matchId).stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .count();

        return registeredCount < match.getMaxPlayers();
    }

    // VYTVOŘENÍ REGISTRACE
    private MatchRegistrationEntity createRegistrationWithSlotCheck(Long matchId, Long playerId,
                                                                    JerseyColor jerseyColor, String adminNote,
                                                                    String excuseReason, String note) {
        PlayerMatchStatus status;
        // Pokud jde o omluvení (excuse), použijeme EXCUSED - ještě musím vyzkoušet při unregistered, kde se také zadává důvod
        if (excuseReason != null) {
            status = PlayerMatchStatus.EXCUSED;
        } else {
            // Jinak zkontrolujeme dostupná místa
            status = isSlotAvailable(matchId) ? PlayerMatchStatus.REGISTERED : PlayerMatchStatus.RESERVED;
        }

        MatchRegistrationEntity registration = new MatchRegistrationEntity();

        registration.setMatch(matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Zápas nenalezen.")));
        registration.setPlayer(playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Hráč nenalezen.")));
        registration.setStatus(status);
        registration.setTimestamp(LocalDateTime.now());
        registration.setCreatedBy("user");

        if (jerseyColor != null) {
            registration.setJerseyColor(jerseyColor);
        }
        if (adminNote != null) {
            registration.setAdminNote(adminNote);
        }
        if (excuseReason != null) {
            registration.setExcuseReason(ExcuseReason.valueOf(excuseReason.toUpperCase()));
        }
        if (note != null) {
            registration.setExcuseNote(note);
        }
        return registrationRepository.save(registration);
    }


    @Override
    @Transactional
    public MatchRegistrationEntity registerPlayer(Long matchId, Long playerId, JerseyColor jerseyColor, String adminNote) {
        MatchRegistrationEntity registration = registrationRepository
                .findByPlayerIdAndMatchId(playerId, matchId)
                .orElse(null);

        if (registration == null) {
            // Žádný záznam -> vytvořit nový
            return createRegistrationWithSlotCheck(matchId, playerId, jerseyColor, adminNote, null, null);
        }

        // Pokud je status UNREGISTERED nebo EXCUSED, aktualizujeme záznam manuálně
        if (registration.getStatus() == PlayerMatchStatus.UNREGISTERED
                || registration.getStatus() == PlayerMatchStatus.EXCUSED) {

            PlayerMatchStatus newStatus = isSlotAvailable(matchId) ? PlayerMatchStatus.REGISTERED : PlayerMatchStatus.RESERVED;
            registration.setStatus(newStatus);

            // Zachovat původní jerseyColor, pokud nebyl zadán nový
            if (jerseyColor != null) {
                registration.setJerseyColor(jerseyColor);
            }

            // Při nové registraci nulujeme tyto hodnoty
            registration.setAdminNote(null);
            registration.setExcuseNote(null);
            registration.setExcuseReason(null);

            // Aktualizujeme timestamp jen při manuální registraci
            registration.setTimestamp(LocalDateTime.now());
            registration.setCreatedBy("user");

            return registrationRepository.save(registration);
        }

        // Pokud má hráč již aktivní registraci
        throw new RuntimeException("Hráč již má aktivní registraci.");
    }


    @Override
    @Transactional
    public MatchRegistrationEntity excusePlayer(Long matchId, Long playerId, String note, String reason) {
        if (registrationRepository.existsByPlayerIdAndMatchId(playerId, matchId)) {
            throw new RuntimeException("Hráč již má registraci k tomuto zápasu, použijte odhlášení (unregister).");
        }
        return createRegistrationWithSlotCheck(matchId, playerId, null, null, reason, note);
    }

    @Override
    @Transactional
    public MatchRegistrationEntity unregisterPlayer(Long matchId, Long playerId, String note, String reason) {
        // Najít existující registraci
        MatchRegistrationEntity registration = registrationRepository
                .findByPlayerIdAndMatchId(playerId, matchId)
                .orElseThrow(() -> new RuntimeException("Hráč nemá registraci k tomuto zápasu."));

        // Aktualizovat status na UNREGISTERED
        registration.setStatus(PlayerMatchStatus.UNREGISTERED);
        registration.setExcuseNote(note);
        if (reason != null && !reason.isBlank()) {
            registration.setExcuseReason(ExcuseReason.valueOf(reason.toUpperCase()));
        }
        registration.setTimestamp(LocalDateTime.now());
        registration.setCreatedBy("user");

        // Spustit přepočet statusů ostatních hráčů
        recalcStatusesForMatch(matchId);

        return registrationRepository.save(registration);
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
        return registrationRepository.findByPlayerId(playerId);
    }

    @Override
    public List<PlayerEntity> getNoResponsePlayers(Long matchId) {
        // Získat všechny hráče
        List<PlayerEntity> allPlayers = playerRepository.findAll();

        // Získat hráče, kteří mají registraci k zápasu
        List<Long> respondedPlayerIds = registrationRepository.findByMatchId(matchId).stream()
                .map(reg -> reg.getPlayer().getId())
                .toList();

        // Vrátit hráče, kteří nemají žádnou registraci k zápasu
        return allPlayers.stream()
                .filter(player -> !respondedPlayerIds.contains(player.getId()))
                .toList();
    }


    @Override
    @Transactional
    public void recalcStatusesForMatch(Long matchId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Zápas nenalezen."));

        int maxPlayers = match.getMaxPlayers();

        // Seznam registrací pro zápas, seřazený podle času vytvoření (starší nejdřív)
        List<MatchRegistrationEntity> registrations = registrationRepository.findByMatchId(matchId).stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED
                        || r.getStatus() == PlayerMatchStatus.RESERVED)
                .sorted((r1, r2) -> r1.getTimestamp().compareTo(r2.getTimestamp()))
                .toList();

        for (int i = 0; i < registrations.size(); i++) {
            MatchRegistrationEntity reg = registrations.get(i);

            if (i < maxPlayers) {
                // Pokud ještě místo zbývá, nastav REGISTERED
                if (reg.getStatus() != PlayerMatchStatus.REGISTERED) {
                    reg.setStatus(PlayerMatchStatus.REGISTERED);
                    reg.setCreatedBy("system");
                    registrationRepository.save(reg);
                }
            } else {
                // Přebyteční hráči mají status RESERVED
                if (reg.getStatus() != PlayerMatchStatus.RESERVED) {
                    reg.setStatus(PlayerMatchStatus.RESERVED);
                    reg.setCreatedBy("system");
                    registrationRepository.save(reg);
                }
            }
        }
    }
}
