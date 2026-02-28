package cz.phsoft.hokej.notifications.services;

import cz.phsoft.hokej.match.entities.MatchEntity;
import cz.phsoft.hokej.registration.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.player.entities.PlayerEntity;
import cz.phsoft.hokej.match.enums.MatchStatus;
import cz.phsoft.hokej.notifications.enums.NotificationType;
import cz.phsoft.hokej.registration.enums.PlayerMatchStatus;
import cz.phsoft.hokej.registration.repositories.MatchRegistrationRepository;
import cz.phsoft.hokej.match.repositories.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Plánovač pro odesílání připomínek zápasů (MATCH_REMINDER).
 *
 * Odpovědnosti:
 * - periodicky procházet budoucí zápasy v definovaném časovém okně,
 * - pro každý zápas najít přihlášené hráče (REGISTERED),
 * - v okamžiku, kdy se zápas blíží na definovaný počet hodin, zavolat
 *   NotificationService.notifyPlayer(..., MATCH_REMINDER, match).
 *
 * Třída NEŘEŠÍ:
 * - uživatelská nastavení (notifyReminders, kanály, globální úrovně),
 *   to je plně v režii NotificationPreferencesService,
 * - sestavení textů e-mailů / SMS (řeší EmailMessageBuilder / SmsMessageBuilder).
 *
 * Deduplikace připomínek se řeší příznakem reminderAlreadySent v
 * MatchRegistrationEntity a navazující historii.
 *
 * Plánovač předpokládá, že je v aplikaci povoleno plánování pomocí
 * anotace @EnableScheduling v konfigurační třídě.
 */
@Service
public class MatchReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(MatchReminderScheduler.class);

    private final MatchRepository matchRepository;
    private final MatchRegistrationRepository matchRegistrationRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    /**
     * Maximální horizont, ve kterém se hledají zápasy pro připomínky.
     * Hodnota udává počet hodin od aktuálního času směrem do budoucnosti.
     *
     * Příklad: 48 znamená, že plánovač zpracuje všechny zápasy v
     * následujících 48 hodinách.
     */
    private final int horizonHours;

    /**
     * Globální hodnota, kolik hodin před začátkem zápasu
     * se má připomínka posílat.
     *
     * Konkrétní kanály a to, zda hráč připomínky chce (notifyReminders),
     * rozhoduje NotificationPreferencesService.
     */
    private final int reminderHoursBefore;

    /**
     * Velikost časového okna (v minutách), ve kterém se připomínka
     * považuje za „aktuální“.
     *
     * Příklad: pokud je reminderHoursBefore = 24 a toleranceMinutes = 5,
     * pak se připomínka odešle v intervalu
     * <24 h - 5 min, 24 h> před začátkem zápasu.
     *
     * Tato tolerance slouží k tomu, aby plánovač běžící např. každých 5 minut
     * nepřeskočil přesný čas.
     */
    private final int toleranceMinutes;

    public MatchReminderScheduler(MatchRepository matchRepository,
                                  MatchRegistrationRepository matchRegistrationRepository,
                                  NotificationService notificationService,
                                  Clock clock,
                                  @Value("${app.notifications.reminder.horizon-hours:48}")
                                  int horizonHours,
                                  @Value("${app.notifications.reminder.hours-before:24}")
                                  int reminderHoursBefore,
                                  @Value("${app.notifications.reminder.tolerance-minutes:5}")
                                  int toleranceMinutes) {
        this.matchRepository = matchRepository;
        this.matchRegistrationRepository = matchRegistrationRepository;
        this.notificationService = notificationService;
        this.clock = clock;
        this.horizonHours = horizonHours;
        this.reminderHoursBefore = reminderHoursBefore;
        this.toleranceMinutes = toleranceMinutes;
    }

    /**
     * Hlavní plánovací metoda.
     *
     * Metoda se spouští periodicky podle cron výrazu nebo fixedDelay.
     * Četnost spouštění by měla odpovídat hodnotě toleranceMinutes tak,
     * aby nedocházelo k opakovanému odesílání připomínek.
     *
     * V implementaci se:
     * - načtou všechny budoucí zápasy v horizontu horizonHours,
     * - odfiltrují se zrušené zápasy,
     * - pro každý zápas se najdou registrace se statusem REGISTERED,
     *   u kterých ještě nebyl odeslán reminder,
     * - pro každého přihlášeného hráče se vyhodnotí, zda nastal čas připomínky,
     *   a případně se odešle notifikace MATCH_REMINDER.
     *
     * Konkrétní to, zda a jak se notifikace doručí (email/SMS),
     * určuje NotificationPreferencesService.
     */
    @Scheduled(fixedDelayString = "${app.notifications.reminder.fixed-delay-ms:300000}")
    @Transactional // musí být R/W kvůli nastavení reminderAlreadySent = true
    public void processMatchReminders() {

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime windowEnd = now.plusHours(horizonHours);

        log.debug("MatchReminderScheduler: start, now={}, windowEnd={}", now, windowEnd);

        // Předpokládá se existence metody v MatchRepository:
        //   List<MatchEntity> findByDateTimeBetween(LocalDateTime from, LocalDateTime to);
        List<MatchEntity> upcomingMatches =
                matchRepository.findByDateTimeBetween(now, windowEnd);

        if (upcomingMatches.isEmpty()) {
            log.debug("MatchReminderScheduler: žádné budoucí zápasy v horizontu {} h", horizonHours);
            return;
        }

        for (MatchEntity match : upcomingMatches) {

            // Zrušené zápasy se pro připomínky ignorují.
            if (match.getMatchStatus() == MatchStatus.CANCELED) {
                log.debug("MatchReminderScheduler: zápas {} je zrušen, přeskočeno", match.getId());
                continue;
            }

            processMatch(match, now);
        }
    }

    /**
     * Zpracuje jeden konkrétní zápas.
     *
     * Provede:
     * - ověření, zda je zápas v reminder okně,
     * - načtení registrací se statusem REGISTERED, u kterých ještě
     *   nebyl odeslán reminder,
     * - pro každou registraci odeslání notifikace MATCH_REMINDER
     *   a nastavení příznaku reminderAlreadySent = true.
     *
     * Nastavení hráče (notifyReminders, kanály) neřeší – rozhoduje
     * o nich NotificationPreferencesService uvnitř NotificationService.
     *
     * @param match zpracovávaný zápas
     * @param now   aktuální čas v okamžiku běhu plánovače
     */
    private void processMatch(MatchEntity match, LocalDateTime now) {

        LocalDateTime matchDateTime = match.getDateTime();
        if (matchDateTime == null) {
            log.debug("MatchReminderScheduler: zápas {} nemá nastavené datum/čas, přeskočeno", match.getId());
            return;
        }

        // Pokud zápas není v okně pro připomínku, nemá smysl cokoliv dál řešit.
        if (!shouldSendReminder(now, matchDateTime)) {
            return;
        }

        // Najdou se pouze registrace REGISTERED, u kterých ještě reminder neodešel.
        List<MatchRegistrationEntity> registrations =
                matchRegistrationRepository.findByMatchIdAndStatusAndReminderAlreadySentFalse(
                        match.getId(),
                        PlayerMatchStatus.REGISTERED
                );

        if (registrations.isEmpty()) {
            log.debug("MatchReminderScheduler: zápas {} nemá žádné REGISTERED hráče bez odeslaného reminderu, přeskočeno",
                    match.getId());
            return;
        }

        for (MatchRegistrationEntity registration : registrations) {

            PlayerEntity player = registration.getPlayer();
            if (player == null) {
                continue;
            }

            log.info(
                    "MatchReminderScheduler: volá se MATCH_REMINDER pro playerId={} matchId={} ({} h před začátkem)",
                    player.getId(), match.getId(), reminderHoursBefore
            );

            // Odeslání notifikace – NotificationService se postará o kanály (email/SMS)
            // podle NotificationPreferencesService a nastavení uživatele/hráče.
            notificationService.notifyPlayer(player, NotificationType.MATCH_REMINDER, match);

            // Deduplikace: označíme, že pro tuto registraci již reminder odešel.
            registration.setReminderAlreadySent(true);
            // Explicitní save není nutný, entita je spravovaná v rámci @Transactional
            // a při commit se změny propíší přes JPA dirty checking.
        }
    }

    /**
     * Rozhodne, zda má být v daném okamžiku odeslána připomínka pro zápas.
     *
     * Připomínka se odešle, pokud:
     * - rozdíl mezi aktuálním časem a časem zápasu je kladný
     *   (zápas je v budoucnosti),
     * - čas do začátku zápasu je menší nebo rovný reminderHoursBefore,
     * - zároveň je větší než reminderHoursBefore mínus toleranceMinutes.
     *
     * Tím se vytvoří úzké okno, ve kterém je připomínka aktivní. Pokud
     * plánovač běží např. každých 5 minut a toleranceMinutes = 5,
     * připomínka se odešle maximálně jednou.
     *
     * @param now           aktuální čas
     * @param matchDateTime datum a čas zápasu
     * @return true, pokud má být připomínka odeslána
     */
    private boolean shouldSendReminder(LocalDateTime now,
                                       LocalDateTime matchDateTime) {

        if (matchDateTime.isBefore(now)) {
            // Zápas je v minulosti – nemá smysl odesílat.
            return false;
        }

        Duration diff = Duration.between(now, matchDateTime);
        long diffMinutes = diff.toMinutes();
        long reminderMinutes = reminderHoursBefore * 60L;

        long lowerBound = reminderMinutes - toleranceMinutes;
        long upperBound = reminderMinutes;

        // ochrana proti záporným dolním mezím
        if (lowerBound < 0) {
            lowerBound = 0;
        }

        boolean inWindow = diffMinutes <= upperBound && diffMinutes > lowerBound;

        if (inWindow) {
            log.debug(
                    "MatchReminderScheduler: zápas za {} minut, okno ({}, {}] minut – připomínka ANO",
                    diffMinutes, lowerBound, upperBound
            );
        } else {
            log.trace(
                    "MatchReminderScheduler: zápas za {} minut, mimo okno ({}, {}] minut – připomínka NE",
                    diffMinutes, lowerBound, upperBound
            );
        }

        return inWindow;
    }
}