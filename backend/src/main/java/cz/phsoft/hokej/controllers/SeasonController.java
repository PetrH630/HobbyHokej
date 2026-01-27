package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.SeasonDTO;
import cz.phsoft.hokej.models.services.CurrentSeasonService;
import cz.phsoft.hokej.models.services.SeasonService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seasons/me")
public class SeasonController {

    private final CurrentSeasonService currentSeasonService;
    private final SeasonService seasonService;

    public SeasonController(CurrentSeasonService currentSeasonService,
                                   SeasonService seasonService) {
        this.currentSeasonService = currentSeasonService;
        this.seasonService = seasonService;
    }
    /**
     * Vrátí všechny sezony
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<SeasonDTO> getAllSeasons() {
        return seasonService.getAllSeasons();
    }

    /**
     * Vrátí sezonu, která je aktuálně vybraná pro přihlášeného uživatele.
     * Pokud není vybrána vrátí @Null.
     */
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public SeasonDTO getCurrentSeasonForUser() {
        Long id = currentSeasonService.getCurrentSeasonIdOrDefault();
        return (id != null) ? seasonService.getSeasonById(id) : null;
    }

    /**
     * Nastavení sezony dle id
     */
    @PostMapping("/current/{seasonId}")
    @PreAuthorize("isAuthenticated()")
    public void setCurrentSeasonForUser(@PathVariable Long seasonId) {
        // jednoduchá validace – jen ověříme, že sezóna existuje
        seasonService.getSeasonById(seasonId);
        currentSeasonService.setCurrentSeasonId(seasonId);
    }


}
