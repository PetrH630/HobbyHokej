package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.services.notification.DemoModeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller poskytující informaci o režimu aplikace (demo / produkční).
 *
 * Slouží frontendové části pro podmíněné zobrazení demo funkcionality.
 */
@RestController
@RequestMapping("/api/public")
public class AppModeController {

    private final DemoModeService demoModeService;

    public AppModeController(DemoModeService demoModeService) {
        this.demoModeService = demoModeService;
    }

    /**
     * Vrací informaci, zda je aplikace spuštěna v demo režimu.
     */
    @GetMapping("/app-mode")
    public Map<String, Object> getAppMode() {
        return Map.of(
                "demoMode", demoModeService.isDemoMode()
        );
    }
}
