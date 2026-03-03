package cz.phsoft.hokej.notifications.controllers;

import cz.phsoft.hokej.registration.dto.NoResponseReminderPreviewDTO;
import cz.phsoft.hokej.notifications.services.MatchReminderScheduler;
import cz.phsoft.hokej.notifications.services.NoResponseReminderScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller pro manuální spuštění plánovačů připomínek zápasů.
 *
 * Controller umožňuje administrátorům a manažerům
 * ručně spustit plánované procesy odesílání připomínek
 * nebo zobrazit jejich náhled.
 */
@RestController
@RequestMapping("/api/admin/match-reminders")
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
public class MatchReminderAdminController {

    private static final Logger log = LoggerFactory.getLogger(MatchReminderAdminController.class);

    private final MatchReminderScheduler matchReminderScheduler;
    private final NoResponseReminderScheduler noResponseReminderScheduler;

    /**
     * Vytváří instanci controlleru pro administrátorské připomínky.
     *
     * @param matchReminderScheduler plánovač standardních připomínek
     * @param noResponseReminderScheduler plánovač připomínek pro NO_RESPONSE hráče
     */
    public MatchReminderAdminController(MatchReminderScheduler matchReminderScheduler,
                                        NoResponseReminderScheduler noResponseReminderScheduler) {
        this.matchReminderScheduler = matchReminderScheduler;
        this.noResponseReminderScheduler = noResponseReminderScheduler;
    }

    /**
     * Manuálně spustí plánovač MATCH_REMINDER.
     *
     * @return textová informace o spuštění procesu
     */
    @GetMapping("/run")
    public ResponseEntity<String> runMatchReminders() {
        log.info("Manuální spuštění MatchReminderScheduler");
        matchReminderScheduler.processMatchReminders();
        return ResponseEntity.ok("MatchReminderScheduler spuštěn.");
    }

    /**
     * Manuálně spustí plánovač NO_RESPONSE připomínek.
     *
     * @return textová informace o spuštění procesu
     */
    @GetMapping("/no-response/run")
    public ResponseEntity<String> runNoResponseReminders() {
        log.info("Manuální spuštění NoResponseReminderScheduler");
        noResponseReminderScheduler.processNoResponseReminders();
        return ResponseEntity.ok("NoResponseReminderScheduler spuštěn.");
    }

    /**
     * Vrací náhled NO_RESPONSE připomínek bez jejich odeslání.
     *
     * @return seznam hráčů a zápasů, kterým by byla připomínka odeslána
     */
    @GetMapping("/no-response/preview")
    public ResponseEntity<List<NoResponseReminderPreviewDTO>> previewNoResponseReminders() {
        log.info("Náhled NoResponseReminderScheduler");
        List<NoResponseReminderPreviewDTO> preview =
                noResponseReminderScheduler.previewNoResponseReminders();
        return ResponseEntity.ok(preview);
    }
}