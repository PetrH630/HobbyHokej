package cz.phsoft.hokej.notifications.controllers;

import cz.phsoft.hokej.notifications.dto.DemoNotificationsDTO;
import cz.phsoft.hokej.notifications.services.DemoNotificationStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller, který se používá pro práci s demo notifikacemi.
 *
 * Controller je registrován pouze v demo režimu. Pokud demo režim není aktivní,
 * endpointy nejsou součástí aplikace a vrací se odpověď 404.
 *
 * Práce s dočasným úložištěm notifikací se deleguje na {@link DemoNotificationStore}.
 */
@RestController
@RequestMapping("/api/demo/notifications")
@ConditionalOnProperty(name = "app.demo-mode", havingValue = "true")
public class DemoNotificationController {

    private final DemoNotificationStore demoNotificationStore;

    public DemoNotificationController(DemoNotificationStore demoNotificationStore) {
        this.demoNotificationStore = demoNotificationStore;
    }

    /**
     * Vrací všechny zachycené demo notifikace a následně je vymaže z úložiště.
     *
     * Endpoint slouží zejména pro frontendovou část aplikace,
     * která zobrazuje simulované odeslané e-maily a SMS zprávy v demo režimu.
     *
     * @return DTO obsahující seznam zachycených e-mailů a SMS zpráv
     */
    @GetMapping
    public ResponseEntity<DemoNotificationsDTO> getDemoNotifications() {
        DemoNotificationsDTO dto = demoNotificationStore.getAndClear();
        return ResponseEntity.ok(dto);
    }

    /**
     * Provede vyčištění zachycených demo notifikací bez jejich vrácení.
     *
     * Endpoint umožňuje explicitní smazání obsahu úložiště
     * například při resetu demo prostředí.
     *
     * @return HTTP odpověď 204 No Content v případě úspěchu
     */
    @DeleteMapping
    public ResponseEntity<Void> clearDemoNotifications() {
        demoNotificationStore.getAndClear();
        return ResponseEntity.noContent().build();
    }
}
