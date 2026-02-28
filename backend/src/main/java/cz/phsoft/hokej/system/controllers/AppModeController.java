package cz.phsoft.hokej.system.controllers;

import cz.phsoft.hokej.notifications.services.DemoModeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller, který se používá pro poskytování informací o aktuálním režimu aplikace.
 *
 * Slouží k informování frontendové části o tom, zda je aplikace spuštěna
 * v demo režimu nebo ve standardním produkčním režimu. Na základě této
 * informace může frontend podmíněně zobrazovat nebo omezovat určitou
 * funkcionalitu.
 *
 * Veškerá logika vyhodnocení režimu aplikace se deleguje na {@link DemoModeService}.
 */
@RestController
@RequestMapping("/api/public")
public class AppModeController {

    private final DemoModeService demoModeService;

    public AppModeController(DemoModeService demoModeService) {
        this.demoModeService = demoModeService;
    }

    /**
     * Vrací informaci o aktuálním režimu aplikace.
     *
     * Hodnota je určena zejména pro frontendovou část systému,
     * která na jejím základě upravuje chování uživatelského rozhraní.
     *
     * @return mapování obsahující příznak demo režimu aplikace
     */
    @GetMapping("/app-mode")
    public Map<String, Object> getAppMode() {
        return Map.of(
                "demoMode", demoModeService.isDemoMode()
        );
    }
}
