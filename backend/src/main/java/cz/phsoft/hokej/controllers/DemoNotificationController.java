package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.DemoNotificationsDTO;
import cz.phsoft.hokej.models.services.notification.DemoNotificationStore;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller, který se používá pro práci s demo notifikacemi.
 *
 * Tento controller je určen výhradně pro demo režim aplikace.
 * Umožňuje frontendové části načítat seznam odeslaných notifikací,
 * které byly zachyceny v rámci demo prostředí místo skutečného
 * odeslání prostřednictvím e-mailu nebo SMS.
 *
 * Po načtení jsou notifikace z úložiště automaticky odstraněny.
 * Práce s dočasným úložištěm notifikací se deleguje na {@link DemoNotificationStore}.
 */
@RestController
@RequestMapping("/api/demo/notifications")
public class DemoNotificationController {

    private final DemoNotificationStore demoNotificationStore;

    public DemoNotificationController(DemoNotificationStore demoNotificationStore) {
        this.demoNotificationStore = demoNotificationStore;
    }

    /**
     * Vrací všechny zachycené demo notifikace a následně je vymaže z úložiště.
     *
     * Endpoint slouží zejména pro frontendovou část aplikace,
     * která zobrazuje simulované odeslané e-maily a SMS zprávy
     * v rámci demo režimu.
     *
     * @return DTO obsahující seznam zachycených e-mailů a SMS zpráv
     */
    @GetMapping
    public ResponseEntity<DemoNotificationsDTO> getDemoNotifications() {
        DemoNotificationsDTO dto = demoNotificationStore.getAndClear();
        return ResponseEntity.ok(dto);
    }

    /**
     * Provádí vyčištění zachycených demo notifikací bez jejich vrácení.
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
