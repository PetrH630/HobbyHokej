package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.registration.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.match.enums.MatchStatus;
import cz.phsoft.hokej.notifications.enums.NotificationType;
import cz.phsoft.hokej.player.enums.PlayerStatus;
import cz.phsoft.hokej.registration.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.match.repositories.MatchRepository;
import cz.phsoft.hokej.player.repositories.PlayerRepository;
import cz.phsoft.hokej.registration.dto.NoResponseReminderPreviewDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Plánovač pro připomínky hráčům, kteří dosud nereagovali (NO_RESPONSE).
 *
 * Odpovědnosti:
 * - najít zápasy, které se konají za definovaný počet dní,
 * - pro tyto zápasy identifikovat schválené hráče bez registrace,
 * - odeslat těmto hráčům notifikaci MATCH_REGISTRATION_NO_RESPONSE
 *   prostřednictvím NotificationService.
 *
 * Třída neřeší:
 * - preferenční logiku kanálů a globálních úrovní, která je v gesci
 *   NotificationPreferencesService,
 * - změny stavů registrací nebo další business logiku kolem registrací.
 *
 * Stav NO_RESPONSE se zde dopočítává. Neexistuje jako samostatný
 * PlayerMatchStatus v databázi. Hráč je považován za NO_RESPONSE, pokud:
 * - je v množině "pozvaných" hráčů pro daný zápas (schválený hráč),
 * - nemá k tomuto zápasu žádnou registraci v žádném ze stavů.
 */
@Service
public class NoResponseReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(NoResponseReminderScheduler.class);

    /**
     * Repository pro práci se zápasy.
     *
     * Používá se k nalezení zápasů v cílovém datu, pro které se
     * mají připomínky NO_RESPONSE vyhodnocovat.
     */
    private final MatchRepository matchRepository;

    /**
     * Repository pro práci s registracemi hráčů na zápasy.
     *
     * Používá se pro zjištění, kteří hráči již na daný zápas reagovali
     * jakýmkoliv stavem PlayerMatchStatus.
     */
    private final MatchRegistrationRepository matchRegistrationRepository;

    /**
     * Repository pro práci s hráči.
     *
     * Používá se k určení množiny "pozvaných" hráčů. V aktuální implementaci
     * jde o všechny schválené hráče (PlayerStatus.APPROVED).
     */
    private final PlayerRepository playerRepository;

    /**
     * Služba pro odesílání notifikací hráčům.
     *
     * Zajišťuje volání notifikačních kanálů a respektování nastavení
     * preferencí NotificationPreferencesService.
     */
    private final NotificationService notificationService;

    /**
     * Hodiny poskytující aktuální datum a čas.
     *
     * Umožňují deterministické testování a přesné výpočty cílového
     * data zápasů.
     */
    private final Clock clock;

    /**
     * Počet dní před zápasem, kdy se má připomínka NO_RESPONSE posílat.
     * Defaultní hodnota je 3, což odpovídá scénáři "tři dny před zápasem".
     */
    private final int daysBeforeMatch;

    /**
     * Vytváří instanci plánovače připomínek pro hráče bez reakce.
     *
     * Konfigurační hodnota daysBeforeMatch se načítá z application properties,
     * s výchozí hodnotou 3, pokud není explicitně nastavena.
     *
     * @param matchRepository             repository pro práci se zápasy
     * @param matchRegistrationRepository repository pro práci s registracemi na zápasy
     * @param playerRepository            repository pro práci s hráči
     * @param notificationService         služba pro odesílání notifikací hráčům
     * @param clock                       hodiny používané pro výpočet aktuálního data
     * @param daysBeforeMatch             počet dní před zápasem, kdy se připomínky posílají
     */
    public NoResponseReminderScheduler(MatchRepository matchRepository,
                                       MatchRegistrationRepository matchRegistrationRepository,
                                       PlayerRepository playerRepository,
                                       NotificationService notificationService,
                                       Clock clock,
                                       @Value("${app.notifications.no-response.days-before:3}")
                                       int daysBeforeMatch) {
        this.matchRepository = matchRepository;
        this.matchRegistrationRepository = matchRegistrationRepository;
        this.playerRepository = playerRepository;
        this.notificationService = notificationService;
        this.clock = clock;
        this.daysBeforeMatch = daysBeforeMatch;
    }

    /**
     * Hlavní plánovací metoda – spouštěná pomocí CRON výrazu.
     *
     * Typicky je spouštěna jednou denně (například 17:00 Europe/Prague).
     * Pro zápasy, které se konají za daysBeforeMatch dní, vyhodnotí
     * množinu hráčů ve stavu NO_RESPONSE a těmto hráčům odešle
     * notifikaci MATCH_REGISTRATION_NO_RESPONSE.
     *
     * Metoda:
     * - zjistí aktuální cílové kombinace (hráč, zápas) voláním findTargets,
     * - pro každou kombinaci zavolá NotificationService.notifyPlayer.
     *
     * Konkrétní kanály a reálné doručení jsou řízeny NotificationPreferencesService.
     */
    @Scheduled(cron = "${app.notifications.no-response.cron:0 00 17 * * *}",
            zone = "${app.notifications.no-response.zone:Europe/Prague}")
    @Transactional
    public void processNoResponseReminders() {

        log.debug("NoResponseReminderScheduler: start processNoResponseReminders(), daysBefore={}", daysBeforeMatch);

        List<Target> targets = findTargets();

        if (targets.isEmpty()) {
            log.debug("NoResponseReminderScheduler: žádné NO_RESPONSE cíle pro připomenutí.");
            return;
        }

        for (Target target : targets) {
            PlayerEntity player = target.player();
            MatchEntity match = target.match();

            log.info(
                    "NoResponseReminderScheduler: posílá se MATCH_REGISTRATION_NO_RESPONSE " +
                            "playerId={} matchId={} ({} dní před zápasem)",
                    player.getId(), match.getId(), daysBeforeMatch
            );

            // NotificationService + NotificationPreferencesService rozhodnou,
            // jaké kanály se reálně použijí (email/SMS/in-app).
            notificationService.notifyPlayer(player, NotificationType.MATCH_REGISTRATION_NO_RESPONSE, match);
        }
    }

    /**
     * Náhled cílových hráčů pro NO_RESPONSE připomínky.
     *
     * Metoda neodesílá žádné notifikace. Pouze vrací strukturovaný
     * seznam hráčů a zápasů, kteří by byli v aktuálním okamžiku
     * zasaženi plánovačem processNoResponseReminders.
     *
     * Výsledný seznam se používá v administračním endpointu
     * pro kontrolu a audit.
     *
     * @return seznam DTO objektů popisujících plánované NO_RESPONSE připomínky
     */
    @Transactional(readOnly = true)
    public List<NoResponseReminderPreviewDTO> previewNoResponseReminders() {

        log.debug("NoResponseReminderScheduler: previewNoResponseReminders() – generuje se náhled.");

        List<Target> targets = findTargets();
        List<NoResponseReminderPreviewDTO> result = new ArrayList<>();

        for (Target t : targets) {
            PlayerEntity player = t.player();
            MatchEntity match = t.match();

            String fullName = player.getFullName() != null
                    ? player.getFullName()
                    : (player.getName() + " " + player.getSurname());

            String phone = player.getPhoneNumber();

            result.add(new NoResponseReminderPreviewDTO(
                    match.getId(),
                    match.getDateTime(),
                    player.getId(),
                    fullName,
                    phone
            ));
        }

        return result;
    }

    /**
     * Najde všechny kombinace (hráč, zápas), pro které má být
     * v aktuálním okamžiku poslána NO_RESPONSE připomínka.
     *
     * Logika NO_RESPONSE:
     * - pozvaní hráči jsou aktuálně všichni schválení hráči (PlayerStatus.APPROVED),
     * - pro daný zápas se z registrací odečtou všichni hráči, kteří již reagovali,
     * - za NO_RESPONSE jsou považováni hráči, kteří jsou mezi pozvanými,
     *   ale nejsou mezi reagujícími.
     *
     * Metoda:
     * - určí cílové datum zápasů (today + daysBeforeMatch),
     * - vyfiltruje zápasy na cílové datum, které nejsou zrušené,
     * - načte schválené hráče,
     * - pro každý zápas zjistí reagující hráče z registrací
     *   a dopočítá množinu NO_RESPONSE.
     *
     * @return seznam interních dvojic Target reprezentujících hráče a jejich zápasy
     */
    private List<Target> findTargets() {

        LocalDate today = LocalDate.now(clock);
        LocalDate targetDate = today.plusDays(daysBeforeMatch);

        log.debug("NoResponseReminderScheduler: hledám zápasy na datum {}", targetDate);

        // Jednodušší varianta – filtr přes findAll().
        // Pokud bude zápasů hodně, lze doplnit do MatchRepository speciální dotaz
        // findByDateTimeBetween(startOfDay, endOfDay).
        List<MatchEntity> matchesOnTargetDate = matchRepository.findAll().stream()
                .filter(m -> m.getDateTime() != null
                        && m.getDateTime().toLocalDate().isEqual(targetDate))
                .filter(m -> m.getMatchStatus() != MatchStatus.CANCELED)
                .toList();

        List<Target> result = new ArrayList<>();

        if (matchesOnTargetDate.isEmpty()) {
            log.debug("NoResponseReminderScheduler: na datum {} nejsou žádné aktivní zápasy.", targetDate);
            return result;
        }

        // 1) Určení "pozvaných" hráčů.
        // Zatím: všichni schválení hráči. Pokud se v budoucnu bude používat
        // jiný model (např. skupiny / týmy podle zápasu), lze logiku zapouzdřit
        // do samostatné metody / služby.
        List<PlayerEntity> invitedPlayers = playerRepository.findAll().stream()
                .filter(p -> p.getPlayerStatus() == PlayerStatus.APPROVED)
                .toList();

        if (invitedPlayers.isEmpty()) {
            log.debug("NoResponseReminderScheduler: žádní schválení hráči, není komu posílat NO_RESPONSE.");
            return result;
        }

        for (MatchEntity match : matchesOnTargetDate) {

            LocalDateTime dt = match.getDateTime();
            log.debug("NoResponseReminderScheduler: zpracovává se zápas id={} dateTime={}",
                    match.getId(), dt);

            // 2) Všechny registrace k danému zápasu – jakýkoliv PlayerMatchStatus.
            // POZOR: je nutné mít v MatchRegistrationRepository metodu
            // List<MatchRegistrationEntity> findByMatchId(Long matchId);
            List<MatchRegistrationEntity> registrations =
                    matchRegistrationRepository.findByMatchId(match.getId());

            // Množina hráčů, kteří již reagovali (mají nějakou registraci).
            Set<Long> respondedPlayerIds = new HashSet<>();
            for (MatchRegistrationEntity reg : registrations) {
                PlayerEntity p = reg.getPlayer();
                if (p != null && p.getId() != null) {
                    respondedPlayerIds.add(p.getId());
                }
            }

            int beforeCount = result.size();

            // 3) NO_RESPONSE = pozvaní hráči, kteří nejsou v respondedPlayerIds.
            for (PlayerEntity invited : invitedPlayers) {
                if (invited.getId() == null) {
                    continue;
                }
                if (!respondedPlayerIds.contains(invited.getId())) {
                    result.add(new Target(invited, match));
                }
            }

            int added = result.size() - beforeCount;
            log.debug("NoResponseReminderScheduler: zápas {} – nalezeno {} hráčů s NO_RESPONSE.",
                    match.getId(), added);
        }

        log.debug("NoResponseReminderScheduler: findTargets() našel {} cílů.", result.size());
        return result;
    }

    /**
     * Interní struktura, která drží dvojici hráč a zápas.
     *
     * Slouží jako transportní objekt v rámci plánovače pro předání
     * kombinací, pro které se mají odeslat NO_RESPONSE připomínky
     * nebo které se zobrazují v náhledu.
     *
     * @param player hráč, jemuž má být připomínka odeslána
     * @param match  zápas, ke kterému se stav NO_RESPONSE vztahuje
     */
    private record Target(PlayerEntity player, MatchEntity match) {
    }
}