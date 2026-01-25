package cz.phsoft.hokej.models.services.sms;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.data.repositories.MatchRepository;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.services.MatchRegistrationService;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduler služba pro automatické odesílání SMS notifikací hráčům.
 * <p>
 * Automaticky odesílá SMS hráčům podle času a stavu zápasů:
 * </p>
 * <ul>
 *     <li>v den zápasu („finální“ SMS s informacemi o zápasu),</li>
 *     <li>několik dní před zápasem hráčům, kteří dosud nereagovali.</li>
 * </ul>
 *
 * Význam v aplikaci:
 * <ul>
 *     <li>zajišťuje pravidelnou a konzistentní komunikaci s hráči,</li>
 *     <li>snižuje riziko zapomenutí zápasu,</li>
 *     <li>pomáhá organizátorům získat včasné reakce hráčů.</li>
 * </ul>
 *
 * Technické řešení:
 * <ul>
 *     <li>využívá Spring scheduler ({@link Scheduled}),</li>
 *     <li>používá {@link SmsService} – nezávislá na konkrétním SMS providerovi,</li>
 *     <li>texty SMS jsou generovány centrálně pomocí {@link SmsMessageBuilder}.</li>
 * </ul>
 *
 * Chování a odolnost:
 * <ul>
 *     <li>služba pracuje pouze se čtením dat (read-only),</li>
 *     <li>selhání odeslání SMS jednomu hráči nesmí ovlivnit ostatní,</li>
 *     <li>výjimky jsou zachyceny a nezastavují běh scheduleru.</li>
 * </ul>
 */
@Service
public class SmsSchedulerService {

    /**
     * Repozitář zápasů.
     * <p>
     * Slouží k vyhledávání zápasů podle data konání.
     * </p>
     */
    private final MatchRepository matchRepository;

    /**
     * Repozitář registrací hráčů na zápasy.
     * <p>
     * Umožňuje získat seznam hráčů registrovaných ke konkrétnímu zápasu.
     * </p>
     */
    private final MatchRegistrationRepository registrationRepository;

    /**
     * Service pro odesílání SMS zpráv.
     * <p>
     * Jedná se o jediný vstupní bod pro odesílání SMS v aplikaci.
     * </p>
     */
    private final SmsService smsService;

    /**
     * Builder pro jednotnou tvorbu textů SMS zpráv.
     * <p>
     * Zajišťuje konzistentní formát a obsah SMS napříč aplikací.
     * </p>
     */
    private final SmsMessageBuilder smsMessageBuilder;

    /**
     * Service s business logikou registrací hráčů na zápasy.
     * <p>
     * Používá se zejména pro zjištění hráčů bez reakce.
     * </p>
     */
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

    /**
     * Odešle „finální“ SMS všem hráčům registrovaným na zápasy,
     * které se konají v aktuální den.
     * <p>
     * Tato SMS slouží jako poslední připomenutí a shrnutí informací
     * o zápasu pro hráče, kteří jsou již registrováni.
     * </p>
     *
     * Obsah SMS typicky zahrnuje:
     * <ul>
     *     <li>datum a čas zápasu,</li>
     *     <li>místo konání,</li>
     *     <li>informaci o účasti konkrétního hráče.</li>
     * </ul>
     *
     * Spouštění:
     * <ul>
     *     <li>každý den ve 12:30.</li>
     * </ul>
     */
    @Scheduled(cron = "0 30 12 * * *")
    @Transactional
    public void sendFinalSmsForTodayMatches() {

        LocalDate today = LocalDate.now();

        // všechny zápasy, které se konají dnes
        List<MatchEntity> todaysMatches = matchRepository.findAll().stream()
                .filter(m -> m.getDateTime().toLocalDate().isEqual(today))
                .toList();

        for (MatchEntity match : todaysMatches) {

            // všechny registrace k danému zápasu
            List<MatchRegistrationEntity> registrations =
                    registrationRepository.findByMatchId(match.getId());

            for (MatchRegistrationEntity reg : registrations) {

                String smsMsg = smsMessageBuilder.buildMessageFinal(reg);

                try {
                    smsService.sendSms(
                            reg.getPlayer().getPhoneNumber(),
                            smsMsg
                    );

                    System.out.println(
                            "Finální SMS poslána hráči " +
                                    reg.getPlayer().getFullName()
                    );

                } catch (Exception e) {
                    // chyba jednoho hráče nesmí zastavit celý scheduler
                    System.err.println(
                            "Chyba SMS pro hráče " +
                                    reg.getPlayer().getFullName() +
                                    ": " + e.getMessage()
                    );
                }
            }
        }
    }

    /**
     * Odešle připomínkovou SMS hráčům, kteří:
     * <ul>
     *     <li>mají zápas za 3 dny,</li>
     *     <li>dosud na zápas nijak nereagovali (NO_RESPONSE).</li>
     * </ul>
     *
     * Smysl této SMS:
     * <ul>
     *     <li>upozornit hráče na blížící se zápas,</li>
     *     <li>motivovat je k reakci (účast / omluva),</li>
     *     <li>umožnit včasné plánování sestavy.</li>
     * </ul>
     *
     * Spouštění:
     * <ul>
     *     <li>každý den ve 14:40.</li>
     * </ul>
     */
    @Scheduled(cron = "0 40 14 * * *")
    @Transactional
    public void sendNoResponseSmsForMatchesIn3Days() {

        LocalDate targetDate = LocalDate.now().plusDays(3);

        // zápasy, které se konají za 3 dny
        List<MatchEntity> matchesInThreeDays = matchRepository.findAll().stream()
                .filter(m -> m.getDateTime().toLocalDate().isEqual(targetDate))
                .toList();

        for (MatchEntity match : matchesInThreeDays) {

            // využijeme existující logiku z MatchRegistrationService
            List<PlayerDTO> noResponsePlayers =
                    matchRegistrationService.getNoResponsePlayers(match.getId());

            for (PlayerDTO player : noResponsePlayers) {

                String smsMsg =
                        smsMessageBuilder.buildMessageNoResponse(player, match);

                try {
                    smsService.sendSms(
                            player.getPhoneNumber(),
                            smsMsg
                    );

                    System.out.println(
                            "NO_RESPONSE SMS poslána hráči " +
                                    player.getFullName()
                    );

                } catch (Exception e) {
                    System.err.println(
                            "Chyba NO_RESPONSE SMS pro hráče " +
                                    player.getFullName() +
                                    ": " + e.getMessage()
                    );
                }
            }
        }
    }
}
