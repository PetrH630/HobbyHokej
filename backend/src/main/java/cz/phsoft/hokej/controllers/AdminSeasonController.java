package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.SeasonDTO;
import cz.phsoft.hokej.models.dto.mappers.SeasonMapper;
import cz.phsoft.hokej.models.services.SeasonService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller pro administraci sezón.
 * <p>
 * Controller je dostupný výhradně pro roli ADMIN a umožňuje:
 * <ul>
 *     <li>vytváření a úpravu sezón,</li>
 *     <li>získání seznamu všech sezón,</li>
 *     <li>zjištění aktuálně aktivní sezóny,</li>
 *     <li>nastavení aktivní sezóny v systému.</li>
 * </ul>
 *
 * Veškerá business logika je delegována do {@link SeasonService}.
 */
@RestController
@RequestMapping("/api/admin/seasons")
@PreAuthorize("hasAnyRole('ADMIN')") // celý controller je přístupný pouze pro ADMIN
public class AdminSeasonController {

    private final SeasonService seasonService;
    private final SeasonMapper seasonMapper;

    public AdminSeasonController(SeasonService seasonService,
                                 SeasonMapper seasonMapper) {
        this.seasonService = seasonService;
        this.seasonMapper = seasonMapper;
    }

    /**
     * Vytvoří novou sezónu.
     *
     * @param seasonDTO data nové sezóny
     * @return vytvořená sezóna
     */
    @PostMapping
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
     * @return seznam sezón
     */
    @GetMapping("/all")
    public ResponseEntity<List<SeasonDTO>> getAllSeasons() {
        List<SeasonDTO> seasons = seasonService.getAllSeasons();
        return ResponseEntity.ok(seasons);
    }

    /**
     * Vrátí aktuálně aktivní sezónu.
     * <p>
     * Service vrací entitu, která je v controlleru mapována na DTO.
     *
     * @return aktivní sezóna
     */
    @GetMapping("/active")
    public ResponseEntity<SeasonDTO> getActiveSeason() {
        SeasonDTO dto = seasonMapper.toDTO(seasonService.getActiveSeason());
        return ResponseEntity.ok(dto);
    }

    /**
     * Nastaví zadanou sezónu jako aktivní.
     * <p>
     * Po úspěšném nastavení je vrácena aktuálně aktivní sezóna.
     *
     * @param id ID sezóny, která má být nastavena jako aktivní
     * @return nově aktivní sezóna
     */
    @PutMapping("/{id}/active")
    public ResponseEntity<SeasonDTO> setActiveSeason(@PathVariable Long id) {
        seasonService.setActiveSeason(id);
        SeasonDTO active = seasonMapper.toDTO(seasonService.getActiveSeason());
        return ResponseEntity.ok(active);
    }
}
