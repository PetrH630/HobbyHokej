package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.Team;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.exceptions.*;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.mappers.MatchRegistrationMapper;
import cz.phsoft.hokej.models.dto.mappers.PlayerMapper;
import cz.phsoft.hokej.models.services.sms.SmsMessageBuilder;
import cz.phsoft.hokej.models.services.sms.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.phsoft.hokej.data.enums.NotificationType;
import cz.phsoft.hokej.models.services.NotificationService;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class MatchRegistrationServiceImpl implements MatchRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(MatchRegistrationServiceImpl.class);

    private final MatchRegistrationRepository registrationRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final MatchRegistrationMapper matchRegistrationMapper;
    private final PlayerMapper playerMapper;
    private final SmsService smsService;
    private final SmsMessageBuilder smsMessageBuilder;
    private final NotificationService notificationService;

    public MatchRegistrationServiceImpl(
            MatchRegistrationRepository registrationRepository,
            MatchRepository matchRepository,
            PlayerRepository playerRepository,
            MatchRegistrationMapper matchRegistrationMapper,
            PlayerMapper playerMapper,
            SmsService smsService,
            SmsMessageBuilder smsMessageBuilder,
            NotificationService notificationService
    ) {
        this.registrationRepository = registrationRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.matchRegistrationMapper = matchRegistrationMapper;
        this.playerMapper = playerMapper;
        this.smsService = smsService;
        this.smsMessageBuilder = smsMessageBuilder;
        this.notificationService = notificationService;
    }

    private MatchEntity getMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    private PlayerEntity getPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    private boolean isSlotAvailable(MatchEntity match) {
        long registeredCount = registrationRepository.countByMatchIdAndStatus(match.getId(), PlayerMatchStatus.REGISTERED);
        return registeredCount < match.getMaxPlayers();
    }

    private void sendSms(MatchRegistrationEntity registration, String message) {
        if (registration == null || registration.getPlayer() == null) {
            return;
        }

        try {
            smsService.sendSms(registration.getPlayer().getPhoneNumber(), message);
        } catch (Exception e) {
            log.error(
                    "Chyba při odesílání SMS pro registraci {}: {}",
                    registration.getId(),
                    e.getMessage(),
                    e
            );
        }
    }

    private MatchRegistrationEntity updateRegistrationStatus(
            MatchRegistrationEntity registration, PlayerMatchStatus status, String updatedBy, boolean updateTimestamp) {

        registration.setStatus(PlayerMatchStatus.valueOf(status.name()));
        registration.setCreatedBy(updatedBy);
        if (updateTimestamp) {
            registration.setTimestamp(LocalDateTime.now());
        }
        return registrationRepository.saveAndFlush(registration);
    }

    // -------------------- REGISTRATION --------------------
    @Transactional
    @Override
    public MatchRegistrationDTO upsertRegistration(
            Long matchId,
            Long playerId,
            Team team,
            String adminNote,
            ExcuseReason excuseReason,
            String excuseNote,
            boolean unregister) {

        MatchEntity match = getMatchOrThrow(matchId);
        PlayerEntity player = getPlayerOrThrow(playerId);

        MatchRegistrationEntity registration = registrationRepository
                .findByPlayerIdAndMatchId(playerId, matchId)
                .orElse(null);

        PlayerMatchStatus newStatus;

        // UNREGISTER: lze pouze když hráč má aktuálně REGISTERED
        if (unregister) {
            if (registration == null || registration.getStatus() != PlayerMatchStatus.REGISTERED) {
                throw new RegistrationNotFoundException(matchId, playerId);
            }
            registration.setExcuseReason(null);
            newStatus = PlayerMatchStatus.UNREGISTERED;

            // EXCUSE: lze vytvořit pouze pokud hráč NEMÁ status REGISTERED
        } else if (excuseReason != null) {
            if (registration != null && registration.getStatus() == PlayerMatchStatus.REGISTERED) {
                throw new DuplicateRegistrationException(matchId, playerId);
            }
            // pokud neexistuje, vytvoříme pozici; pokud existuje a není REGISTERED, povolíme EXCUSED
            if (registration == null) {
                registration = new MatchRegistrationEntity();
                registration.setMatch(match);
                registration.setPlayer(player);
            }
            registration.setExcuseReason(excuseReason);
            registration.setExcuseNote(excuseNote);
            newStatus = PlayerMatchStatus.EXCUSED;

            // REGISTER / RESERVE: lze vytvořit pokud hráč NEMÁ status REGISTERED (tedy i když má EXCUSED)
        } else {
            // pokud už je registrován, nepovolíme duplicitu
            if (registration != null && registration.getStatus() == PlayerMatchStatus.REGISTERED) {
                throw new DuplicateRegistrationException(matchId, playerId);
            }

            newStatus = isSlotAvailable(match) ? PlayerMatchStatus.REGISTERED : PlayerMatchStatus.RESERVED;

            if (registration == null) {
                registration = new MatchRegistrationEntity();
                registration.setMatch(match);
                registration.setPlayer(player);
            } else {
                // při přechodu na register/reserve zrušíme případnou výmluvu
                registration.setExcuseReason(null);
                registration.setExcuseNote(null);
            }
        }

        registration.setStatus(newStatus);
        registration.setTimestamp(LocalDateTime.now());
        registration.setCreatedBy("user");

        if (team != null) registration.setTeam(team);
        if (adminNote != null) registration.setAdminNote(adminNote);
        // excuseReason už jsme nastavili výše (pokud to byl EXCUSED případ)
        if (excuseReason != null) registration.setExcuseReason(excuseReason);

        registration = registrationRepository.save(registration);

        if (unregister) {
            recalcStatusesForMatch(matchId);
        }

        // starší verze notifikace
        // sendSms(registration, smsMessageBuilder.buildMessageRegistration(registration));

        // Odesílání notifikace - sms, email
        NotificationType notificationType = switch (newStatus) {
            case REGISTERED -> NotificationType.PLAYER_REGISTERED;
            case UNREGISTERED -> NotificationType.PLAYER_UNREGISTERED;
            case EXCUSED -> NotificationType.PLAYER_EXCUSED;
            case RESERVED -> NotificationType.PLAYER_RESERVED;
            default -> null;
        };

        if (notificationType != null) {
            notificationService.notifyPlayer(player, notificationType, registration);
        }

        return matchRegistrationMapper.toDTO(registration);
    }

    // -------------------- FETCH --------------------
    @Override
    public List<MatchRegistrationDTO> getRegistrationsForMatch(Long matchId) {
        return matchRegistrationMapper.toDTOList(registrationRepository.findByMatchId(matchId));
    }

    @Override
    public List<MatchRegistrationDTO> getRegistrationsForMatches(List<Long> matchIds) {
        if (matchIds == null || matchIds.isEmpty()) {
            return List.of();
        }

        return matchRegistrationMapper.toDTOList(
                registrationRepository.findByMatchIdIn(matchIds)
        );
    }

    @Override
    public List<MatchRegistrationDTO> getAllRegistrations() {
        return matchRegistrationMapper.toDTOList(registrationRepository.findAll());
    }

    @Override
    public List<MatchRegistrationDTO> getRegistrationsForPlayer(Long playerId) {
        return matchRegistrationMapper.toDTOList(registrationRepository.findByPlayerId(playerId));
    }

    @Override
    public List<PlayerDTO> getNoResponsePlayers(Long matchId) {
        List<Long> responded = registrationRepository.findByMatchId(matchId).stream()
                .map(r -> r.getPlayer().getId())
                .toList();

        List<PlayerEntity> noResponsePlayers = playerRepository.findAll().stream()
                .filter(p -> !responded.contains(p.getId()))
                .toList();

        return noResponsePlayers.stream()
                .map(playerMapper::toDTO)
                .toList();
    }

    // -------------------- RECALC --------------------
    @Override
    @Transactional
    public void recalcStatusesForMatch(Long matchId) {
        MatchEntity match = getMatchOrThrow(matchId);
        int maxPlayers = match.getMaxPlayers();

        List<MatchRegistrationEntity> regs = registrationRepository.findByMatchId(matchId).stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED
                        || r.getStatus() == PlayerMatchStatus.RESERVED)
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .toList();

        for (int i = 0; i < regs.size(); i++) {
            MatchRegistrationEntity reg = regs.get(i);
            PlayerMatchStatus newStatus = (i < maxPlayers) ? PlayerMatchStatus.REGISTERED : PlayerMatchStatus.RESERVED;
            if (reg.getStatus() != newStatus) updateRegistrationStatus(reg, newStatus, "system", false);
        }
    }

    // -------------------- SMS --------------------
    @Transactional
    public void sendSmsToRegisteredPlayers(Long matchId) {
        registrationRepository.findByMatchId(matchId).stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .forEach(r -> {
                    // NEW – respektuj preferenci hráče pro SMS
                    var player = r.getPlayer();
                    var ns = player.getNotificationSettings();
                    if (ns != null && ns.isSmsEnabled()) {
                        sendSms(r, smsMessageBuilder.buildMessageFinal(r));
                    }
                });
    }

    public void sendNoResponseSmsForMatch(Long matchId) {
        var match = getMatchOrThrow(matchId);

        getNoResponsePlayers(matchId).forEach(player -> {
            String smsMsg = smsMessageBuilder.buildMessageNoResponse(player, match);

            try {
                // respektuj notifyBySms v PlayerDTO (mapované z NotificationSettings)
                if (player.isNotifyBySms()) {
                    smsService.sendSms(player.getPhoneNumber(), smsMsg);
                }
            } catch (Exception e) {
                // CHANGED: žádný System.err, jen strukturovaný log se stacktrace
                log.error(
                        "Chyba při odeslání SMS hráči {} ({}) pro zápas {}: {}",
                        player.getFullName(),
                        player.getPhoneNumber(),
                        matchId,
                        e.getMessage(),
                        e
                );
            }
        });
    }

    @Override
    @Transactional
    public MatchRegistrationDTO updateStatus(Long matchId, Long playerId, PlayerMatchStatus status) {

        MatchEntity match = getMatchOrThrow(matchId);
        PlayerEntity player = getPlayerOrThrow(playerId);

        if (status == PlayerMatchStatus.NO_EXCUSED) {
            throw new InvalidPlayerStatusException(
                    "Status NO_EXCUSED musí být nastaven přes speciální endpoint /logiku."
            );
        }

        MatchRegistrationEntity registration = registrationRepository
                .findByPlayerIdAndMatchId(playerId, matchId)
                .orElseThrow(() -> new RegistrationNotFoundException(matchId, playerId));

        // můžeme případně řešit další pravidla, tady jen prostý update
        MatchRegistrationEntity updated =
                updateRegistrationStatus(registration, status, "admin", true);

        return matchRegistrationMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public MatchRegistrationDTO markNoExcused(
            Long matchId,
            Long playerId,
            String adminNote
    ) {

        MatchEntity match = getMatchOrThrow(matchId);
        PlayerEntity player = getPlayerOrThrow(playerId);

        if (match.getDateTime().isAfter(LocalDateTime.now())) {
            throw new InvalidPlayerStatusException(
                    "Status NO_EXCUSED lze nastavit pouze u již proběhlého zápasu."
            );
        }

        MatchRegistrationEntity registration = registrationRepository
                .findByPlayerIdAndMatchId(playerId, matchId)
                .orElseThrow(() -> new RegistrationNotFoundException(matchId, playerId));

        if (registration.getStatus() != PlayerMatchStatus.REGISTERED) {
            throw new InvalidPlayerStatusException(
                    "Status NO_EXCUSED lze nastavit pouze z registrace REGISTERED."
            );
        }

        // doménová logika NO_EXCUSED
        registration.setExcuseReason(null);
        registration.setExcuseNote(null);

        // adminNote – default fallback
        if (adminNote == null || adminNote.isBlank()) {
            registration.setAdminNote("Nedostavil se bez omluvy");
        } else {
            registration.setAdminNote(adminNote);
        }

        MatchRegistrationEntity updated =
                updateRegistrationStatus(
                        registration,
                        PlayerMatchStatus.NO_EXCUSED,
                        "admin",
                        true
                );

        return matchRegistrationMapper.toDTO(updated);
    }


}