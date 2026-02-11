package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.DemoNotificationsDTO;
import cz.phsoft.hokej.models.services.notification.DemoNotificationStore;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller pro práci s demo notifikacemi.
 *
 * Slouží výhradně pro DEMO režim aplikace.
 * Umožňuje frontendové části aplikace načíst
 * seznam odeslaných notifikací (e-mailů a SMS),
 * které byly zachyceny místo reálného odeslání.
 *
 * Po načtení se notifikace automaticky vyčistí.
 */
@RestController
@RequestMapping("/api/demo/notifications")
public class DemoNotificationController {

    private final DemoNotificationStore demoNotificationStore;

    public DemoNotificationController(DemoNotificationStore demoNotificationStore) {
        this.demoNotificationStore = demoNotificationStore;
    }

    /**
     * Vrátí všechny zachycené demo notifikace
     * a následně je vymaže z paměti.
     *
     * @return DTO obsahující seznam e-mailů a SMS
     */
    @GetMapping
    public ResponseEntity<DemoNotificationsDTO> getDemoNotifications() {
        DemoNotificationsDTO dto = demoNotificationStore.getAndClear();
        return ResponseEntity.ok(dto);
    }

    /**
     * Umožňuje ručně vyčistit demo notifikace
     * bez jejich načtení.
     */
    @DeleteMapping
    public ResponseEntity<Void> clearDemoNotifications() {
        demoNotificationStore.getAndClear();
        return ResponseEntity.noContent().build();
    }
}
