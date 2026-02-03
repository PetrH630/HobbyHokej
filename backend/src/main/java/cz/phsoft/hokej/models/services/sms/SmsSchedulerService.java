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
 * Scheduler služba, která zajišťuje plánované odesílání SMS notifikací hráčům.
 *
 * Odpovědnost třídy je:
 * - vyhledávání zápasů podle data konání,
 * - určení cílových hráčů pro daný typ SMS,
 * - předání textu zpráv službě SmsService k odeslání.
 *
 * Třída se používá ve vrstvě služeb a navazuje na MatchRepository,
 * MatchRegistrationRepository, MatchRegistrationService a SmsMessageBuilder.
 */
@Service
public class SmsSchedulerService {

    /**
     * Repozitář zápasů, který se používá pro vyhledávání zápasů podle data konání.
     */
    private final MatchRepository matchRepository;

    /**
     * Repozitář registrací hráčů na zápasy, který se používá pro získání registrací
     * ke konkrétnímu zápasu.
     */
    private final MatchRegistrationRepository registrationRepository;

    /**
     * Služba pro odesílání SMS zpráv, která představuje technický vstupní bod
     * pro komunikaci s externím SMS providerem.
     */
    private final SmsService smsService;

    /**
     * Builder pro tvorbu textů SMS zpráv, který zajišťuje jednotný formát
     * a obsah SMS napříč aplikací.
     */
    private final SmsMessageBuilder smsMessageBuilder;

    /**
     * Služba s business logikou registrací hráčů na zápasy, která se používá
     * zejména pro zjištění hráčů bez reakce na pozvánku.
     */
    private final MatchRegistrationService matchRegistrationService;

    /**
     * Vytváří instanci scheduler služby a injektuje závislosti ze Spring kontextu.
     */
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
     * Metoda odesílá finální SMS všem hráčům registrovaným na zápasy,
     * které se konají v aktuální den.
     *
     * Metoda se plánuje pomocí Spring scheduleru a spouští se každý den ve 12:30
     * v časové zóně Europe/Prague. V rámci zpracování se vyhledají všechny dnešní zápasy,
     * načtou se registrace hráčů k těmto zápasům a pro každou registraci se vygeneruje
     * text finální SMS a předá se službě SmsService k odeslání.
     *
     * Metoda pracuje pouze s existujícími daty a nemění stav databáze, transakce
     * se používá pro zajištění konzistence při čtení.
     */
    @Scheduled(cron = "0 30 12 * * *", zone = "Europe/Prague")
    @Transactional
    public void sendFinalSmsForTodayMatches() {

        LocalDate today = LocalDate.now();

        // Zápasy, které se konají v aktuální den.
        List<MatchEntity> todaysMatches = matchRepository.findAll().stream()
                .filter(m -> m.getDateTime().toLocalDate().isEqual(today))
                .toList();

        for (MatchEntity match : todaysMatches) {

            // Registrace hráčů k danému zápasu.
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
                    // Chyba při odesílání jednomu hráči nesmí zastavit běh celé naplánované úlohy.
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
     * Metoda odesílá připomínkovou SMS hráčům, kteří mají zápas naplánovaný
     * za tři dny a dosud na zápas nijak nereagovali (stav NO_RESPONSE).
     *
     * Metoda se plánuje pomocí Spring scheduleru a spouští se každý den ve 12:30
     * v časové zóně Europe/Prague. V rámci zpracování se vyhledají zápasy,
     * které se konají přesně za tři dny od aktuálního data. Pro každý takový zápas
     * se využije logika služby MatchRegistrationService k získání hráčů bez reakce
     * a těmto hráčům se odešle připomínková SMS prostřednictvím SmsService.
     *
     * Metoda slouží k podpoře včasného plánování sestavy a k omezení situací,
     * kdy hráči zapomenou na blížící se zápas.
     */
    @Scheduled(cron = "0 30 12 * * *", zone = "Europe/Prague")
    @Transactional
    public void sendNoResponseSmsForMatchesIn3Days() {

        LocalDate targetDate = LocalDate.now().plusDays(3);

        // Zápasy, které se konají za tři dny.
        List<MatchEntity> matchesInThreeDays = matchRepository.findAll().stream()
                .filter(m -> m.getDateTime().toLocalDate().isEqual(targetDate))
                .toList();

        for (MatchEntity match : matchesInThreeDays) {

            // Hráči bez reakce se získávají pomocí MatchRegistrationService.
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
                    // Chyba při odesílání jednomu hráči nesmí zastavit běh celé naplánované úlohy.
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
