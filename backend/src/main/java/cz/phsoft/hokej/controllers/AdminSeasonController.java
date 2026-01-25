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

@RestController
@RequestMapping("/api/admin/seasons")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')") // celý controller jen pro Admin / Manager
public class AdminSeasonController {

    private final SeasonService seasonService;
    private final SeasonMapper seasonMapper;

    public AdminSeasonController(SeasonService seasonService, SeasonMapper seasonMapper) {
        this.seasonService = seasonService;
        this.seasonMapper = seasonMapper;
    }

    // ======================
    // CREATE
    // ======================
    @PostMapping
    public ResponseEntity<SeasonDTO> createSeason(@Valid @RequestBody SeasonDTO seasonDTO) {
        SeasonDTO created = seasonService.createSeason(seasonDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ======================
    // UPDATE
    // ======================
    @PutMapping("/{id}")
    public ResponseEntity<SeasonDTO> updateSeason(
            @PathVariable Long id,
            @Valid @RequestBody SeasonDTO seasonDTO
    ) {
        SeasonDTO updated = seasonService.updateSeason(id, seasonDTO);
        return ResponseEntity.ok(updated);
    }

    // ======================
    // SEZNAM VŠECH SEZÓN
    // ======================
    @GetMapping("/all")
    public ResponseEntity<List<SeasonDTO>> getAllSeasons() {
        List<SeasonDTO> seasons = seasonService.getAllSeasons();
        return ResponseEntity.ok(seasons);
    }

    // ======================
    // AKTIVNÍ SEZÓNA
    // ======================
    @GetMapping("/active")
    public ResponseEntity<SeasonDTO> getActiveSeason() {
        // service vrací Entity → v controlleru ji přemapujeme na DTO
        SeasonDTO dto = seasonMapper.toDTO(seasonService.getActiveSeason());
        return ResponseEntity.ok(dto);
    }

    // ======================
    // NASTAVENÍ AKTIVNÍ SEZÓNY
    // ======================
    @PutMapping("/{id}/active")
    public ResponseEntity<SeasonDTO> setActiveSeason(@PathVariable Long id) {
        seasonService.setActiveSeason(id);
        // po nastavení vrátíme aktuálně aktivní sezónu jako DTO
        SeasonDTO active = seasonMapper.toDTO(seasonService.getActiveSeason());
        return ResponseEntity.ok(active);
    }
}
