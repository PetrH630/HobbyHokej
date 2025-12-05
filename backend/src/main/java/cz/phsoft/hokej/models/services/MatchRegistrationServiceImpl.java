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
import cz.phsoft.hokej.models.services.sms.SmsMessageBuilder;

import cz.phsoft.hokej.models.services.sms.SmsService;
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
    private final SmsService smsService;
    private final SmsMessageBuilder smsMessageBuilder;


    public MatchRegistrationServiceImpl(
            MatchRegistrationRepository registrationRepository,
            MatchRepository matchRepository,
            PlayerRepository playerRepository,
            MatchRegistrationMapper matchRegistrationMapper,
            SmsService smsService,
            SmsMessageBuilder smsMessageBuilder)  {

        this.registrationRepository = registrationRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.matchRegistrationMapper = matchRegistrationMapper;
        this.smsService = smsService;
        this.smsMessageBuilder = smsMessageBuilder;
    }

    // HELPERS
    private boolean isSlotAvailable(Long matchId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        long registeredCount = registrationRepository.findByMatchId(matchId).stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .count();

        return registeredCount < match.getMaxPlayers();
    }


    // CREATE REGISTRATION
    private MatchRegistrationEntity createRegistrationWithSlotCheck(Long matchId, Long playerId,
                                                                    JerseyColor jerseyColor, String adminNote,
                                                                    String excuseReason, String note) {

        PlayerMatchStatus status;

        if (excuseReason != null) {
            status = PlayerMatchStatus.EXCUSED;
        } else {
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

        if (jerseyColor != null) registration.setJerseyColor(jerseyColor);
        if (adminNote != null) registration.setAdminNote(adminNote);
        if (excuseReason != null) registration.setExcuseReason(ExcuseReason.valueOf(excuseReason.toUpperCase()));
        if (note != null) registration.setExcuseNote(note);

        return registrationRepository.save(registration);
    }


    // REGISTER PLAYER
    @Override
    @Transactional
    public MatchRegistrationEntity registerPlayer(Long matchId, Long playerId, JerseyColor jerseyColor, String adminNote) {

        MatchRegistrationEntity registration = registrationRepository
                .findByPlayerIdAndMatchId(playerId, matchId)
                .orElse(null);

        if (registration == null) {

            MatchRegistrationEntity registered = createRegistrationWithSlotCheck(
                    matchId, playerId, jerseyColor, adminNote, null, null
            );

            sendRegistrationSms(registered);
            return registered;
        }

        if (registration.getStatus() == PlayerMatchStatus.UNREGISTERED
                || registration.getStatus() == PlayerMatchStatus.EXCUSED) {

            PlayerMatchStatus newStatus =
                    isSlotAvailable(matchId) ? PlayerMatchStatus.REGISTERED : PlayerMatchStatus.RESERVED;

            registration.setStatus(newStatus);

            if (jerseyColor != null) registration.setJerseyColor(jerseyColor);

            registration.setAdminNote(null);
            registration.setExcuseNote(null);
            registration.setExcuseReason(null);

            registration.setTimestamp(LocalDateTime.now());
            registration.setCreatedBy("user");

            MatchRegistrationEntity created = registrationRepository.save(registration);

            sendRegistrationSms(created);
            return created;
        }

        throw new RuntimeException("Hráč již má aktivní registraci.");
    }


    // EXCUSE PLAYER
    @Override
    @Transactional
    public MatchRegistrationEntity excusePlayer(Long matchId, Long playerId, String note, String reason) {

        if (registrationRepository.existsByPlayerIdAndMatchId(playerId, matchId)) {
            throw new RuntimeException("Hráč již má registraci, použijte odhlášení.");
        }

        MatchRegistrationEntity excused = createRegistrationWithSlotCheck(
                matchId, playerId, null, null, reason, note
        );

        sendRegistrationSms(excused);
        return excused;
    }


    // UNREGISTER PLAYER
    @Override
    @Transactional
    public MatchRegistrationEntity unregisterPlayer(Long matchId, Long playerId, String note, String reason) {

        MatchRegistrationEntity registration = registrationRepository
                .findByPlayerIdAndMatchId(playerId, matchId)
                .orElseThrow(() -> new RuntimeException("Hráč nemá registraci."));

        registration.setStatus(PlayerMatchStatus.UNREGISTERED);
        registration.setExcuseNote(note);

        if (reason != null && !reason.isBlank()) {
            registration.setExcuseReason(ExcuseReason.valueOf(reason.toUpperCase()));
        }

        registration.setTimestamp(LocalDateTime.now());
        registration.setCreatedBy("user");

        recalcStatusesForMatch(matchId);

        MatchRegistrationEntity unregistered = registrationRepository.save(registration);

        sendRegistrationSms(unregistered);
        return unregistered;
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
        List<PlayerEntity> allPlayers = playerRepository.findAll();

        List<Long> responded = registrationRepository.findByMatchId(matchId).stream()
                .map(reg -> reg.getPlayer().getId())
                .toList();

        return allPlayers.stream()
                .filter(p -> !responded.contains(p.getId()))
                .toList();
    }


    // RECALC
    @Override
    @Transactional
    public void recalcStatusesForMatch(Long matchId) {

        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Zápas nenalezen."));

        int maxPlayers = match.getMaxPlayers();

        List<MatchRegistrationEntity> registrations = registrationRepository.findByMatchId(matchId).stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED
                        || r.getStatus() == PlayerMatchStatus.RESERVED)
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .toList();

        for (int i = 0; i < registrations.size(); i++) {

            MatchRegistrationEntity reg = registrations.get(i);

            if (i < maxPlayers) {
                if (reg.getStatus() != PlayerMatchStatus.REGISTERED) {
                    reg.setStatus(PlayerMatchStatus.REGISTERED);
                    reg.setCreatedBy("system");
                    registrationRepository.save(reg);
                }
            } else {
                if (reg.getStatus() != PlayerMatchStatus.RESERVED) {
                    reg.setStatus(PlayerMatchStatus.RESERVED);
                    reg.setCreatedBy("system");
                    registrationRepository.save(reg);
                }
            }
        }
    }


    // SEND SMS -------------------------------------------------------------
    private void sendRegistrationSms(MatchRegistrationEntity registration) {
        if (registration == null || registration.getMatch() == null || registration.getPlayer() == null) {
            return;
        }

        String smsMsg = smsMessageBuilder.buildMessageRegistration(registration);

        try {
            smsService.sendSms(registration.getPlayer().getPhoneNumber(), smsMsg);
            System.out.println("SMS odeslána hráči " + registration.getPlayer().getFullName() + ": " + smsMsg);
        } catch (Exception e) {
            System.err.println("Chyba SMS: " + e.getMessage());
        }
    }

    public void sendNoResponseSmsForMatch(Long matchId) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Zápas nenalezen."));

        List<PlayerEntity> noResponsePlayers = getNoResponsePlayers(matchId);

        for (PlayerEntity player : noResponsePlayers) {
            String smsMsg = smsMessageBuilder.buildMessageNoResponse(player, match);

            try {
                smsService.sendSms(player.getPhoneNumber(), smsMsg);
                System.out.println("SMS odeslána hráči " + player.getFullName() + ": " + smsMsg);
            } catch (Exception e) {
                System.err.println("Chyba SMS pro hráče " + player.getFullName() + ": " + e.getMessage());
            }
        }
    }

    @Transactional
    public void sendSmsToRegisteredPlayers(Long matchId) {
        // načtení zápasu
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Zápas nenalezen."));

        // získání všech registrací pro zápas, které mají status REGISTERED
        List<MatchRegistrationEntity> registeredPlayers = registrationRepository
                .findByMatchId(matchId).stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .toList();

        // posílání SMS
        for (MatchRegistrationEntity registration : registeredPlayers) {
            PlayerEntity player = registration.getPlayer();
            String smsMsg = smsMessageBuilder.buildMessageFinal(registration);

            try {
                smsService.sendSms(player.getPhoneNumber(), smsMsg);
                System.out.println("SMS odeslána hráči " + player.getFullName() + ": " + smsMsg);
            } catch (Exception e) {
                System.err.println("Chyba SMS pro hráče " + player.getFullName() + ": " + e.getMessage());
            }
        }
    }



}
