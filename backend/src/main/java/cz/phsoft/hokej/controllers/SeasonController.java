package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.SeasonDTO;
import cz.phsoft.hokej.models.mappers.SeasonMapper;
import cz.phsoft.hokej.models.services.CurrentSeasonService;
import cz.phsoft.hokej.models.services.SeasonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller pro správu sezón.
 *
 * Zajišťuje:
 * <ul>
 *     <li>administraci sezón (CRUD, nastavení aktivní sezóny) pro roli ADMIN,</li>
 *     <li>práci s „aktuální sezónou“ pro přihlášeného uživatele (/me).</li>
 * </ul>
 *
 * Veškerá business logika je delegována do {@link SeasonService}
 * a {@link CurrentSeasonService}.
 */
@RestController
@RequestMapping("/api/seasons")
public class SeasonController {

    private final SeasonService seasonService;
    private final SeasonMapper seasonMapper;
    private final CurrentSeasonService currentSeasonService;

    public SeasonController(SeasonService seasonService,
                            SeasonMapper seasonMapper,
                            CurrentSeasonService currentSeasonService) {
        this.seasonService = seasonService;
        this.seasonMapper = seasonMapper;
        this.currentSeasonService = currentSeasonService;
    }

    // =========================================================
    //  ADMIN – GLOBÁLNÍ SPRÁVA SEZÓN
    // =========================================================

    /**
     * Vytvoří novou sezónu.
     *
     * @param seasonDTO data nové sezóny
     * @return vytvořená sezóna
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeasonDTO> createSeason(
            @Valid @RequestBody SeasonDTO seasonDTO) {

        SeasonDTO created = seasonService.createSeason(seasonDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Aktualizuje existující sezónu.
     *
     * @param id        ID sezóny
     * @param seasonDTO aktualizovaná data sezóny
     * @return aktualizovaná sezóna
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeasonDTO> updateSeason(
            @PathVariable Long id,
            @Valid @RequestBody SeasonDTO seasonDTO
    ) {
        SeasonDTO updated = seasonService.updateSeason(id, seasonDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Vrátí seznam všech sezón v systému.
     *
     * Tady můžeš nechat jen ADMIN, nebo klidně všem přihlášeným.
     * Aktuálně omezeno na ADMIN kvůli původnímu chování.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SeasonDTO>> getAllSeasonsAdmin() {
        List<SeasonDTO> seasons = seasonService.getAllSeasons();
        return ResponseEntity.ok(seasons);
    }

    /**
     * Vrátí aktuálně aktivní globální sezónu.
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeasonDTO> getActiveSeason() {
        SeasonDTO dto = seasonMapper.toDTO(seasonService.getActiveSeason());
        return ResponseEntity.ok(dto);
    }

    /**
     * Nastaví zadanou sezónu jako globálně aktivní.
     *
     * @param id ID sezóny, která má být nastavena jako aktivní
     * @return nově aktivní sezóna
     */
    @PutMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeasonDTO> setActiveSeason(@PathVariable Long id) {
        seasonService.setActiveSeason(id);
        SeasonDTO active = seasonMapper.toDTO(seasonService.getActiveSeason());
        return ResponseEntity.ok(active);
    }

    // =========================================================
    //  USER – PRÁCE S „MOJÍ“ SEZÓNOU (/me)
    // =========================================================

    /**
     * Vrátí všechny sezóny pro účely výběru na frontendu.
     *
     * Toto je user-friendly varianta (původní /api/seasons/me GET).
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public List<SeasonDTO> getAllSeasonsForUser() {
        return seasonService.getAllSeasons();
    }

    /**
     * Vrátí sezónu, která je aktuálně vybraná pro přihlášeného uživatele.
     * Pokud není vybraná, vrátí null (nebo default dle CurrentSeasonService).
     */
    @GetMapping("/me/current")
    @PreAuthorize("isAuthenticated()")
    public SeasonDTO getCurrentSeasonForUser() {
        Long id = currentSeasonService.getCurrentSeasonIdOrDefault();
        return (id != null) ? seasonService.getSeasonById(id) : null;
    }

    /**
     * Nastaví aktuální sezónu pro přihlášeného uživatele.
     *
     * @param seasonId ID sezóny, která má být nastavena jako current pro uživatele
     */
    @PostMapping("/me/current/{seasonId}")
    @PreAuthorize("isAuthenticated()")
    public void setCurrentSeasonForUser(@PathVariable Long seasonId) {
        // jednoduchá validace – ověříme, že sezóna existuje
        seasonService.getSeasonById(seasonId);
        currentSeasonService.setCurrentSeasonId(seasonId);
    }
}
