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
 * - najít zápasy, které se konají za N dní (typicky 3 dny),
 * - pro tyto zápasy najít "pozvané" hráče, kteří nemají žádnou registraci,
 * - zavolat NotificationService.notifyPlayer(..., MATCH_REGISTRATION_NO_RESPONSE, match).
 *
 * Třída neřeší:
 * - preferenční logiku (kanály, globální úrovně) – to řeší NotificationPreferencesService,
 * - další business logiku okolo změny stavu registrací.
 *
 * Stav NO_RESPONSE se zde **dopočítává** – neexistuje jako samostatná registrace
 * v databázi. Hráč je považován za NO_RESPONSE, pokud:
 * - je v množině "pozvaných" hráčů pro daný zápas,
 * - ale nemá k tomuto zápasu žádnou registraci (žádný z PlayerMatchStatus).
 */
@Service
public class NoResponseReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(NoResponseReminderScheduler.class);

    private final MatchRepository matchRepository;
    private final MatchRegistrationRepository matchRegistrationRepository;
    private final PlayerRepository playerRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    /**
     * Počet dní před zápasem, kdy se má připomínka NO_RESPONSE posílat.
     * Default: 3 – tedy "tři dny před zápasem".
     */
    private final int daysBeforeMatch;

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
     * Hlavní plánovací metoda – spouštěná CRONem.
     *
     * Typicky 1× denně (např. 17:00 Europe/Prague).
     * Pro zápasy, které se konají za daysBeforeMatch dní, najde hráče ve stavu NO_RESPONSE
     * (dopočítaném – hráč nemá žádnou registraci k zápasu) a pošle jim notifikaci
     * MATCH_REGISTRATION_NO_RESPONSE.
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
     * Metoda nic neodesílá, pouze vrátí seznam hráčů a zápasů,
     * kteří/é by byli v aktuálním okamžiku zasaženi plánovačem.
     *
     * Používá se v admin endpointu /preview.
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
     * - "Pozvaní" hráči jsou schválení hráči (PlayerStatus.APPROVED).
     * - Z registrací k zápasu se vezmou všichni hráči, kteří již reagovali
     *   (jakýmkoliv PlayerMatchStatus).
     * - NO_RESPONSE = pozvaní hráči, kteří nejsou mezi reagujícími.
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
     * Interní struktura, která drží dvojici (hráč, zápas).
     */
    private record Target(PlayerEntity player, MatchEntity match) {
    }
}