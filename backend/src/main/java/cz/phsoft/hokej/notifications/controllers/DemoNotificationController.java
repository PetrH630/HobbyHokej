package cz.phsoft.hokej.notifications.controllers;

import cz.phsoft.hokej.notifications.dto.DemoNotificationsDTO;
import cz.phsoft.hokej.notifications.services.DemoNotificationStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller pro práci se zachycenými notifikacemi v demo režimu.
 *
 * Controller je aktivní pouze pokud je nastavena vlastnost
 * app.demo-mode=true. Veškerá práce s úložištěm je delegována
 * do DemoNotificationStore.
 */
@RestController
@RequestMapping("/api/demo/notifications")
@ConditionalOnProperty(name = "app.demo-mode", havingValue = "true")
public class DemoNotificationController {

    private final DemoNotificationStore demoNotificationStore;

    /**
     * Vytváří instanci controlleru pro práci s demo notifikacemi.
     *
     * @param demoNotificationStore úložiště zachycených notifikací
     */
    public DemoNotificationController(DemoNotificationStore demoNotificationStore) {
        this.demoNotificationStore = demoNotificationStore;
    }

    /**
     * Vrací všechny zachycené demo notifikace a následně je vymaže.
     *
     * @return DTO obsahující seznam zachycených e-mailů a SMS zpráv
     */
    @GetMapping
    public ResponseEntity<DemoNotificationsDTO> getDemoNotifications() {
        DemoNotificationsDTO dto = demoNotificationStore.getAndClear();
        return ResponseEntity.ok(dto);
    }

    /**
     * Provede vyčištění zachycených demo notifikací.
     *
     * @return HTTP 204 No Content v případě úspěchu
     */
    @DeleteMapping
    public ResponseEntity<Void> clearDemoNotifications() {
        demoNotificationStore.getAndClear();
        return ResponseEntity.noContent().build();
    }
}