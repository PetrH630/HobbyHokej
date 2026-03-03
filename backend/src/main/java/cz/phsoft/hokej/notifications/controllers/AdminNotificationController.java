package cz.phsoft.hokej.notifications.controllers;

import cz.phsoft.hokej.notifications.dto.DemoNotificationsDTO;
import cz.phsoft.hokej.notifications.dto.SpecialNotificationTargetDTO;
import cz.phsoft.hokej.notifications.dto.SpecialNotificationRequestDTO;
import cz.phsoft.hokej.notifications.services.DemoModeService;
import cz.phsoft.hokej.notifications.services.DemoNotificationStore;
import cz.phsoft.hokej.notifications.services.SpecialNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller pro administrátorské odesílání speciálních notifikací.
 *
 * Controller umožňuje vytváření a odesílání speciálních zpráv
 * vybraným uživatelům a hráčům. Veškerá aplikační logika je
 * delegována do SpecialNotificationService.
 *
 * V případě aktivního demo režimu se fyzické odeslání e-mailů
 * a SMS neprovádí a zachycené zprávy jsou vráceny klientovi.
 */
@RestController
@RequestMapping("/api/notifications/admin")
public class AdminNotificationController {

    private static final Logger log = LoggerFactory.getLogger(AdminNotificationController.class);

    private final SpecialNotificationService specialNotificationService;
    private final DemoModeService demoModeService;
    private final DemoNotificationStore demoNotificationStore;

    /**
     * Vytváří instanci controlleru pro administrátorské notifikace.
     *
     * @param specialNotificationService služba pro odesílání speciálních notifikací
     * @param demoModeService služba určující, zda je aktivní demo režim
     * @param demoNotificationStore dočasné úložiště zachycených notifikací v demo režimu
     */
    public AdminNotificationController(SpecialNotificationService specialNotificationService,
                                       DemoModeService demoModeService,
                                       DemoNotificationStore demoNotificationStore) {
        this.specialNotificationService = specialNotificationService;
        this.demoModeService = demoModeService;
        this.demoNotificationStore = demoNotificationStore;
    }

    /**
     * Vytvoří a odešle speciální notifikaci vybraným příjemcům.
     *
     * Zpráva je uložena jako in-app notifikace typu SPECIAL_MESSAGE.
     * Podle příznaků v requestu může být odeslána také e-mailem a SMS.
     * Odeslání je delegováno do SpecialNotificationService.
     *
     * V demo režimu se fyzické odeslání neprovádí a vrací se
     * zachycené e-maily a SMS zprávy.
     *
     * @param request DTO obsahující text zprávy a seznam cílových příjemců
     * @return DemoNotificationsDTO v demo režimu nebo HTTP 204 v produkčním režimu
     */
    @PostMapping("/special")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> createSpecialNotification(
            @RequestBody SpecialNotificationRequestDTO request) {

        log.info("Admin vytváří speciální zprávu pro {} cílů",
                request.getTargets() != null ? request.getTargets().size() : 0);

        specialNotificationService.sendSpecialNotification(request);

        if (demoModeService.isDemoMode()) {
            DemoNotificationsDTO demoData = demoNotificationStore.getAndClear();
            log.debug("DEMO MODE: vráceno {} e-mailů a {} SMS",
                    demoData.getEmails().size(),
                    demoData.getSms().size());
            return ResponseEntity.ok(demoData);
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Vrací seznam dostupných příjemců pro speciální notifikaci.
     *
     * Data jsou získána ze SpecialNotificationService.
     *
     * @return seznam dostupných příjemců ve formě DTO
     */
    @GetMapping("/special/targets")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<SpecialNotificationTargetDTO>> getSpecialNotificationTargets() {
        return ResponseEntity.ok(specialNotificationService.getSpecialNotificationTargets());
    }
}