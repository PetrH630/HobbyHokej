package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.NoResponseReminderPreviewDTO;
import cz.phsoft.hokej.models.services.notification.MatchReminderScheduler;
import cz.phsoft.hokej.models.services.notification.NoResponseReminderScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin controller pro ruční spuštění plánovačů připomínek zápasů.
 *
 * Umožňuje:
 * - ručně spustit MatchReminderScheduler (MATCH_REMINDER pro REGISTERED hráče),
 * - ručně spustit NoResponseReminderScheduler (MATCH_REGISTRATION_NO_RESPONSE pro NO_RESPONSE hráče),
 * - zobrazit náhled, komu by se NO_RESPONSE připomínky poslaly, bez reálného odeslání.
 *
 * Endpoints jsou určeny pro adminy / manažery k testování a ladění.
 */
@RestController
@RequestMapping("/api/admin/match-reminders")
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
public class MatchReminderAdminController {

    private static final Logger log = LoggerFactory.getLogger(MatchReminderAdminController.class);

    private final MatchReminderScheduler matchReminderScheduler;
    private final NoResponseReminderScheduler noResponseReminderScheduler;

    public MatchReminderAdminController(MatchReminderScheduler matchReminderScheduler,
                                        NoResponseReminderScheduler noResponseReminderScheduler) {
        this.matchReminderScheduler = matchReminderScheduler;
        this.noResponseReminderScheduler = noResponseReminderScheduler;
    }

    /**
     * Ruční spuštění standardních připomínek MATCH_REMINDER
     * pro hráče se statusem REGISTERED.
     *
     * GET /api/admin/match-reminders/run
     */
    @GetMapping("/run")
    public ResponseEntity<String> runMatchReminders() {
        log.info("Manuální spuštění MatchReminderScheduler přes GET /api/admin/match-reminders/run");
        matchReminderScheduler.processMatchReminders();
        return ResponseEntity.ok("MatchReminderScheduler spuštěn.");
    }

    /**
     * Ruční spuštění připomínek pro hráče, kteří dosud nereagovali (NO_RESPONSE).
     *
     * GET /api/admin/match-reminders/no-response/run
     */
    @GetMapping("/no-response/run")
    public ResponseEntity<String> runNoResponseReminders() {
        log.info("Manuální spuštění NoResponseReminderScheduler přes GET /api/admin/match-reminders/no-response/run");
        noResponseReminderScheduler.processNoResponseReminders();
        return ResponseEntity.ok("NoResponseReminderScheduler spuštěn.");
    }

    /**
     * Náhled připomínek pro NO_RESPONSE hráče – nic se neodesílá.
     *
     * GET /api/admin/match-reminders/no-response/preview
     *
     * Vrací seznam hráčů a zápasů, kterým by se v aktuálním okamžiku
     * poslala NO_RESPONSE připomínka.
     */
    @GetMapping("/no-response/preview")
    public ResponseEntity<List<NoResponseReminderPreviewDTO>> previewNoResponseReminders() {
        log.info("Manuální náhled NoResponseReminderScheduler přes GET /api/admin/match-reminders/no-response/preview");
        List<NoResponseReminderPreviewDTO> preview = noResponseReminderScheduler.previewNoResponseReminders();
        return ResponseEntity.ok(preview);
    }
}