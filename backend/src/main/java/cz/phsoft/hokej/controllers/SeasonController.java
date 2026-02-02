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
 * REST controller, který se používá pro správu sezón.
 *
 * Zajišťuje administrativní správu sezón pro roli ADMIN včetně
 * vytváření, aktualizace a nastavení globálně aktivní sezóny.
 * Dále poskytuje endpointy pro práci s aktuální sezónou uživatele
 * pod prefixem /me.
 *
 * Veškerá business logika se předává do {@link SeasonService}
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

    // ADMIN – globální správa sezón

    /**
     * Vytváří novou sezónu.
     *
     * @param seasonDTO DTO s daty nové sezóny
     * @return vytvořená sezóna jako {@link SeasonDTO}
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
     * @param seasonDTO DTO s aktualizovanými daty sezóny
     * @return aktualizovaná sezóna jako {@link SeasonDTO}
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
     * Vrací seznam všech sezón v systému.
     *
     * Endpoint je v tuto chvíli omezen na roli ADMIN. Podle potřeby
     * může být v budoucnu zpřístupněn širšímu okruhu uživatelů.
     *
     * @return seznam sezón jako {@link SeasonDTO}
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SeasonDTO>> getAllSeasonsAdmin() {
        List<SeasonDTO> seasons = seasonService.getAllSeasons();
        return ResponseEntity.ok(seasons);
    }

    /**
     * Vrací aktuálně globálně aktivní sezónu.
     *
     * Aktivní sezóna představuje výchozí období pro systémové operace,
     * které nejsou vázány na konkrétní volbu uživatele.
     *
     * @return aktivní sezóna jako {@link SeasonDTO}
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeasonDTO> getActiveSeason() {
        SeasonDTO dto = seasonMapper.toDTO(seasonService.getActiveSeason());
        return ResponseEntity.ok(dto);
    }

    /**
     * Nastavuje zadanou sezónu jako globálně aktivní.
     *
     * Po nastavení se informace o aktivní sezóně používá v dalších
     * částech systému jako výchozí sezóna.
     *
     * @param id ID sezóny, která má být nastavena jako aktivní
     * @return nově aktivní sezóna jako {@link SeasonDTO}
     */
    @PutMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SeasonDTO> setActiveSeason(@PathVariable Long id) {
        seasonService.setActiveSeason(id);
        SeasonDTO active = seasonMapper.toDTO(seasonService.getActiveSeason());
        return ResponseEntity.ok(active);
    }

    // Uživatelská práce s „mojí“ sezónou

    /**
     * Vrací seznam všech sezón pro účely výběru na frontendu.
     *
     * Jedná se o uživatelskou variantu endpointu, která se používá
     * například pro zobrazení seznamu sezón v nabídce.
     *
     * @return seznam sezón jako {@link SeasonDTO}
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public List<SeasonDTO> getAllSeasonsForUser() {
        return seasonService.getAllSeasons();
    }

    /**
     * Vrací sezónu, která je aktuálně vybraná pro přihlášeného uživatele.
     *
     * Pokud uživatel nemá explicitně vybranou sezónu, může být vrácena
     * výchozí hodnota podle implementace {@link CurrentSeasonService}.
     *
     * @return aktuální sezóna pro uživatele nebo null
     */
    @GetMapping("/me/current")
    @PreAuthorize("isAuthenticated()")
    public SeasonDTO getCurrentSeasonForUser() {
        Long id = currentSeasonService.getCurrentSeasonIdOrDefault();
        return (id != null) ? seasonService.getSeasonById(id) : null;
    }

    /**
     * Nastavuje aktuální sezónu pro přihlášeného uživatele.
     *
     * Před nastavením se ověřuje, že sezóna existuje. Id sezóny
     * se následně ukládá do kontextu aktuální sezóny uživatele.
     *
     * @param seasonId ID sezóny, která má být nastavena jako aktuální
     */
    @PostMapping("/me/current/{seasonId}")
    @PreAuthorize("isAuthenticated()")
    public void setCurrentSeasonForUser(@PathVariable Long seasonId) {
        seasonService.getSeasonById(seasonId);
        currentSeasonService.setCurrentSeasonId(seasonId);
    }
}
