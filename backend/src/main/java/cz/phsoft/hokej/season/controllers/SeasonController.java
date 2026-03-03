package cz.phsoft.hokej.season.controllers;

import cz.phsoft.hokej.season.dto.SeasonDTO;
import cz.phsoft.hokej.season.dto.SeasonHistoryDTO;
import cz.phsoft.hokej.season.mappers.SeasonMapper;
import cz.phsoft.hokej.season.services.CurrentSeasonService;
import cz.phsoft.hokej.season.services.SeasonHistoryService;
import cz.phsoft.hokej.season.services.SeasonService;
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
 * Veškerá business logika se předává do SeasonService
 * a CurrentSeasonService.
 */
@RestController
@RequestMapping("/api/seasons")
public class SeasonController {

    private final SeasonService seasonService;
    private final SeasonMapper seasonMapper;
    private final CurrentSeasonService currentSeasonService;
    private final SeasonHistoryService seasonHistoryService;

    /**
     * Vytváří instanci controlleru se všemi potřebnými závislostmi.
     *
     * Závislosti se používají pro správu sezón, práci s aktuální sezónou
     * přihlášeného uživatele, mapování entit na DTO a načítání historie sezón.
     *
     * @param seasonService service vrstva pro správu sezón
     * @param seasonMapper mapper pro převod mezi entitami a SeasonDTO
     * @param currentSeasonService service pro práci s aktuální sezónou uživatele
     * @param seasonHistoryService service pro načítání historie sezón
     */
    public SeasonController(SeasonService seasonService,
                            SeasonMapper seasonMapper,
                            CurrentSeasonService currentSeasonService,
                            SeasonHistoryService seasonHistoryService) {
        this.seasonService = seasonService;
        this.seasonMapper = seasonMapper;
        this.currentSeasonService = currentSeasonService;
        this.seasonHistoryService = seasonHistoryService;
    }

    // ADMIN – globální správa sezón

    /**
     * Vytváří novou sezónu.
     *
     * Před uložením se provádí validace vstupních dat. Vytvořená sezóna
     * se vrací jako DTO včetně přiřazeného identifikátoru.
     *
     * @param seasonDTO DTO s daty nové sezóny
     * @return vytvořená sezóna jako SeasonDTO zabalená v ResponseEntity
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<SeasonDTO> createSeason(
            @Valid @RequestBody SeasonDTO seasonDTO) {

        SeasonDTO created = seasonService.createSeason(seasonDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Aktualizuje existující sezónu.
     *
     * Před aktualizací se ověřuje existence cílové sezóny. Aktualizovaná
     * sezóna se vrací jako DTO.
     *
     * @param id        ID sezóny
     * @param seasonDTO DTO s aktualizovanými daty sezóny
     * @return aktualizovaná sezóna jako SeasonDTO zabalená v ResponseEntity
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<SeasonDTO> updateSeason(
            @PathVariable Long id,
            @Valid @RequestBody SeasonDTO seasonDTO
    ) {
        SeasonDTO updated = seasonService.updateSeason(id, seasonDTO);
        return ResponseEntity.ok(updated);
    }

    /**
     * Vrací historii sezóny dle ID.
     *
     * Historie se používá pro auditní účely a přehled změn
     * vlastností sezóny v čase.
     *
     * @param id ID sezóny
     * @return historie sezóny jako seznam SeasonHistoryDTO
     */
    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<SeasonHistoryDTO> getSeasonHistory(
            @PathVariable Long id
    ) {
        return seasonHistoryService.getHistoryForSeason(id);
    }

    /**
     * Vrací seznam všech sezón v systému.
     *
     * Endpoint je v tuto chvíli omezen na roli ADMIN nebo MANAGER.
     * Podle potřeby může být v budoucnu zpřístupněn širšímu okruhu
     * uživatelů.
     *
     * @return seznam sezón jako List SeasonDTO zabalený v ResponseEntity
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
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
     * @return aktivní sezóna jako SeasonDTO zabalená v ResponseEntity
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<SeasonDTO> getActiveSeason() {
        SeasonDTO dto = seasonMapper.toDTO(seasonService.getActiveSeason());
        return ResponseEntity.ok(dto);
    }

    /**
     * Nastavuje zadanou sezónu jako globálně aktivní.
     *
     * Po nastavení se informace o aktivní sezóně používá v dalších
     * částech systému jako výchozí sezóna pro operace nad daty.
     *
     * @param id ID sezóny, která má být nastavena jako aktivní
     * @return nově aktivní sezóna jako SeasonDTO zabalená v ResponseEntity
     */
    @PutMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
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
     * @return seznam sezón jako List SeasonDTO
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
     * výchozí hodnota podle implementace CurrentSeasonService.
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