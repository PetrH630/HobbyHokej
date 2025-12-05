package cz.phsoft.hokej.models.services.sms;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.enums.PlayerMatchStatus;
import cz.phsoft.hokej.data.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class SmsSchedulerService {

    private final MatchRepository matchRepository;
    private final MatchRegistrationRepository registrationRepository;
    private final SmsService smsService;
    private final SmsMessageBuilder smsMessageBuilder;

    public SmsSchedulerService(MatchRepository matchRepository,
                               MatchRegistrationRepository registrationRepository,
                               SmsService smsService,
                               SmsMessageBuilder smsMessageBuilder) {
        this.matchRepository = matchRepository;
        this.registrationRepository = registrationRepository;
        this.smsService = smsService;
        this.smsMessageBuilder = smsMessageBuilder;
    }

    // Spustí se každý den v 17:59
    @Scheduled(cron = "0 59 17 * * *") // sekunda, minuta, hodina, den, měsíc, den v týdnu
    @Transactional
    public void sendFinalSmsForTodayMatches() {
        LocalDateTime today = LocalDateTime.now();

        List<MatchEntity> todaysMatches = matchRepository.findAll().stream()
                .filter(m -> m.getDateTime().toLocalDate().isEqual(today.toLocalDate()))
                .toList();

        for (MatchEntity match : todaysMatches) {
            List<MatchRegistrationEntity> registeredPlayers = registrationRepository
                    .findByMatchId(match.getId()).stream()
                    .filter(r -> r.getStatus() == PlayerMatchStatus.REGISTERED)
                    .toList();

            for (MatchRegistrationEntity reg : registeredPlayers) {
                String smsMsg = smsMessageBuilder.buildMessageFinal(reg);
                try {
                    smsService.sendSms(reg.getPlayer().getPhoneNumber(), smsMsg);
                    System.out.println("Finální SMS odeslána hráči " + reg.getPlayer().getFullName() + ": " + smsMsg);
                } catch (Exception e) {
                    System.err.println("Chyba SMS pro hráče " + reg.getPlayer().getFullName() + ": " + e.getMessage());
                }
            }
        }
    }
}
