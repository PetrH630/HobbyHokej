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
import cz.phsoft.hokej.exceptions.DuplicateRegistrationException;
import cz.phsoft.hokej.exceptions.MatchNotFoundException;
import cz.phsoft.hokej.exceptions.PlayerNotFoundException;
import cz.phsoft.hokej.exceptions.RegistrationNotFoundException;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.mappers.MatchRegistrationMapper;
import cz.phsoft.hokej.models.dto.mappers.PlayerMapper;
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
    private final PlayerMapper playerMapper;
    private final SmsService smsService;
    private final SmsMessageBuilder smsMessageBuilder;

    public MatchRegistrationServiceImpl(
            MatchRegistrationRepository registrationRepository,
            MatchRepository matchRepository,
            PlayerRepository playerRepository,
            MatchRegistrationMapper matchRegistrationMapper,
            PlayerMapper playerMapper,
            SmsService smsService,
            SmsMessageBuilder smsMessageBuilder) {
        this.registrationRepository = registrationRepository;
        this.matchRepository = matchRepository;
        this.playerRepository = playerRepository;
        this.matchRegistrationMapper = matchRegistrationMapper;
        this.playerMapper = playerMapper;
        this.smsService = smsService;
        this.smsMessageBuilder = smsMessageBuilder;
    }

    private MatchEntity getMatchOrThrow(Long matchId) {
        return matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));
    }

    private PlayerEntity getPlayerOrThrow(Long playerId) {
        return playerRepository.findById(playerId)
                .orElseThrow(() -> new PlayerNotFoundException(playerId));
    }

    private boolean isSlotAvailable(Long matchId) {
        long registeredCount = registrationRepository.findByMatchId(matchId).stream()
                .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                .count();
        return registeredCount < getMatchOrThrow(matchId).getMaxPlayers();
    }

    private void sendSms(MatchRegistrationEntity registration, String message) {
        if (registration == null || registration.getPlayer() == null) return;
        try {
            smsService.sendSms(registration.getPlayer().getPhoneNumber(), message);
        } catch (Exception e) {
            System.err.println("Chyba SMS: " + e.getMessage());
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
            JerseyColor jerseyColor,
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

        if (unregister) {
            if (registration == null) throw new RegistrationNotFoundException(matchId, playerId);
            registration.setExcuseReason(null);
            newStatus = PlayerMatchStatus.UNREGISTERED;
        } else if (excuseReason != null) {
            if (registration != null && registration.getStatus() != PlayerMatchStatus.UNREGISTERED) {
                throw new DuplicateRegistrationException(matchId, playerId);
            }
            registration.setExcuseReason(excuseReason);
            newStatus = PlayerMatchStatus.EXCUSED;
        } else {
            newStatus = isSlotAvailable(matchId) ? PlayerMatchStatus.REGISTERED : PlayerMatchStatus.RESERVED;
            if (registration != null) registration.setExcuseReason(null);
        }

        if (registration == null) {
            registration = new MatchRegistrationEntity();
            registration.setMatch(match);
            registration.setPlayer(player);
        }

        registration.setStatus(newStatus);
        registration.setTimestamp(LocalDateTime.now());
        registration.setCreatedBy("user");

        if (jerseyColor != null) registration.setJerseyColor(jerseyColor);
        if (adminNote != null) registration.setAdminNote(adminNote);
        if (excuseReason != null) registration.setExcuseReason(excuseReason);

        registration = registrationRepository.save(registration);

        if (unregister) recalcStatusesForMatch(matchId);

        sendSms(registration, smsMessageBuilder.buildMessageRegistration(registration));

        // 🔥 mapping už zde
        return matchRegistrationMapper.toDTO(registration);
    }

    // -------------------- FETCH --------------------
    @Override
    public List<MatchRegistrationDTO> getRegistrationsForMatch(Long matchId) {
        return matchRegistrationMapper.toDTOList(registrationRepository.findByMatchId(matchId));
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
                .forEach(r -> sendSms(r, smsMessageBuilder.buildMessageFinal(r)));
    }

    public void sendNoResponseSmsForMatch(Long matchId) {
        var match = getMatchOrThrow(matchId);

        getNoResponsePlayers(matchId).forEach(player -> {
            String smsMsg = smsMessageBuilder.buildMessageNoResponse(player, match);

            try {
                smsService.sendSms(player.getPhoneNumber(), smsMsg);
            } catch (Exception e) {
                System.err.println("Chyba SMS pro hráče "
                        + player.getFullName() + ": " + e.getMessage());
            }
        });
    }

}
