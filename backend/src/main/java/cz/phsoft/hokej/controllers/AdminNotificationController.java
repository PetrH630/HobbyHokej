package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.DemoNotificationsDTO;
import cz.phsoft.hokej.models.dto.SpecialNotificationTargetDTO;
import cz.phsoft.hokej.models.dto.requests.SpecialNotificationRequestDTO;
import cz.phsoft.hokej.models.services.notification.DemoModeService;
import cz.phsoft.hokej.models.services.notification.DemoNotificationStore;
import cz.phsoft.hokej.models.services.notification.SpecialNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller pro administrátorské notifikace.
 */
@RestController
@RequestMapping("/api/notifications/admin")
public class AdminNotificationController {

    private static final Logger log = LoggerFactory.getLogger(AdminNotificationController.class);

    private final SpecialNotificationService specialNotificationService;
    private final DemoModeService demoModeService;
    private final DemoNotificationStore demoNotificationStore;

    public AdminNotificationController(SpecialNotificationService specialNotificationService,
                                       DemoModeService demoModeService,
                                       DemoNotificationStore demoNotificationStore) {
        this.specialNotificationService = specialNotificationService;
        this.demoModeService = demoModeService;
        this.demoNotificationStore = demoNotificationStore;
    }

    /**
     * Vytvoří speciální zprávu pro vybrané uživatele a hráče.
     *
     * Zpráva se uloží jako in-app notifikace typu SPECIAL_MESSAGE
     * a podle příznaků v requestu se odešle také e-mailem a SMS.
     *
     * V DEMO režimu se e-maily a SMS fyzicky neodesílají.
     * Místo toho se vrací jako DemoNotificationsDTO.
     *
     * @param request definice zprávy a seznam příjemců
     * @return
     *   - DemoNotificationsDTO v demo režimu
     *   - HTTP 204 v produkčním režimu
     */
    @PostMapping("/special")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> createSpecialNotification(
            @RequestBody SpecialNotificationRequestDTO request) {

        log.info("Admin vytváří speciální zprávu pro {} cílů",
                request.getTargets() != null ? request.getTargets().size() : 0);

        specialNotificationService.sendSpecialNotification(request);

        // DEMO režim – vrátíme zachycené e-maily a SMS
        if (demoModeService.isDemoMode()) {
            DemoNotificationsDTO demoData = demoNotificationStore.getAndClear();
            log.debug("DEMO MODE: vráceno {} e-mailů a {} SMS",
                    demoData.getEmails().size(),
                    demoData.getSms().size());
            return ResponseEntity.ok(demoData);
        }

        // PRODUKCE – beze změny
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/special/targets")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<SpecialNotificationTargetDTO>> getSpecialNotificationTargets() {
        return ResponseEntity.ok(specialNotificationService.getSpecialNotificationTargets());
    }
}