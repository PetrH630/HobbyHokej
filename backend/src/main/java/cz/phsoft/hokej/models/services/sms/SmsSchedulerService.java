package cz.phsoft.hokej.models.services.sms;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.services.MatchRegistrationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class SmsSchedulerService {

    private final MatchRepository matchRepository;
    private final MatchRegistrationRepository registrationRepository;
    private final SmsService smsService;
    private final SmsMessageBuilder smsMessageBuilder;
    private final MatchRegistrationService matchRegistrationService;

    public SmsSchedulerService(
            MatchRepository matchRepository,
            MatchRegistrationRepository registrationRepository,
            SmsService smsService,
            SmsMessageBuilder smsMessageBuilder,
            MatchRegistrationService matchRegistrationService) {

        this.matchRepository = matchRepository;
        this.registrationRepository = registrationRepository;
        this.smsService = smsService;
        this.smsMessageBuilder = smsMessageBuilder;
        this.matchRegistrationService = matchRegistrationService;
    }

    // Poslání SMS v den zápasu
    // Spustí se každý den v 12:30
    @Scheduled(cron = "0 30 12 * * *")
    @Transactional
    public void sendFinalSmsForTodayMatches() {

        LocalDate today = LocalDate.now();

        // najdeme všechny dnešní zápasy
        List<MatchEntity> todaysMatches = matchRepository.findAll().stream()
                .filter(m -> m.getDateTime().toLocalDate().isEqual(today))
                .toList();

        for (MatchEntity match : todaysMatches) {

            // načteme všechny registrace k danému zápasu (BEZ změn entit)
            List<MatchRegistrationEntity> registrations =
                    registrationRepository.findByMatchId(match.getId());

            for (MatchRegistrationEntity reg : registrations) {

                // použiješ svůj původní builder
                String smsMsg = smsMessageBuilder.buildMessageFinal(reg);

                try {
                    smsService.sendSms(reg.getPlayer().getPhoneNumber(), smsMsg);
                    System.out.println("Finální SMS poslána hráči "
                            + reg.getPlayer().getFullName() + ": " + smsMsg);

                } catch (Exception e) {
                    System.err.println("Chyba SMS pro hráče "
                            + reg.getPlayer().getFullName() + ": " + e.getMessage());
                }
            }
        }
    }

    // poslání SMS 3 dny před zápasem - noresponse player
    @Scheduled(cron = "0 40 14 * * *") // každý den ve 12:30
    @Transactional
    public void sendNoResponseSmsForMatchesIn3Days() {

        LocalDate targetDate = LocalDate.now().plusDays(3);

        // všechny zápasy, které jsou za 3 dny
        List<MatchEntity> matchesInThreeDays = matchRepository.findAll().stream()
                .filter(m -> m.getDateTime().toLocalDate().isEqual(targetDate))
                .toList();

        for (MatchEntity match : matchesInThreeDays) {

            // použijeme tvůj existující helper
            List<PlayerDTO> noResponsePlayers =
                    matchRegistrationService.getNoResponsePlayers(match.getId());

            for (PlayerDTO player : noResponsePlayers) {

                String smsMsg = smsMessageBuilder.buildMessageNoResponse(player, match);

                try {
                    smsService.sendSms(player.getPhoneNumber(), smsMsg);
                    System.out.println("NORESPONSE SMS poslána hráči "
                            + player.getFullName() + ": " + smsMsg);
                } catch (Exception e) {
                    System.err.println("Chyba NORESPONSE SMS pro hráče "
                            + player.getFullName() + ": " + e.getMessage());
                }
            }
        }
    }
}
