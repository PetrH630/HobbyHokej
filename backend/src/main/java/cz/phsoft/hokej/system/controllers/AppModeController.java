package cz.phsoft.hokej.system.controllers;

import cz.phsoft.hokej.notifications.services.DemoModeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller poskytující informaci o aktuálním režimu aplikace.
 *
 * Controller slouží pro veřejné zjištění, zda aplikace běží
 * v demo režimu nebo ve standardním provozním režimu.
 * Na základě této informace může frontend upravovat
 * chování uživatelského rozhraní nebo omezovat určité operace.
 *
 * Vyhodnocení režimu aplikace je delegováno do DemoModeService.
 */
@RestController
@RequestMapping("/api/public")
public class AppModeController {

    private final DemoModeService demoModeService;

    /**
     * Vytvoří instanci controlleru.
     *
     * @param demoModeService service zajišťující vyhodnocení demo režimu
     */
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