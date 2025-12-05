
package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.PlayerInactivityPeriodDTO;
import cz.phsoft.hokej.models.dto.mappers.PlayerInactivityPeriodMapper;
import cz.phsoft.hokej.data.entities.PlayerInactivityPeriodEntity;
import cz.phsoft.hokej.models.services.PlayerInactivityPeriodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

        import java.util.List;

@RestController
@RequestMapping("/api/inactivity")
@CrossOrigin(origins = "*")
public class PlayerInactivityPeriodController {

    private final PlayerInactivityPeriodService service;
    private final PlayerInactivityPeriodMapper mapper;

    public PlayerInactivityPeriodController(PlayerInactivityPeriodService service,
                                            PlayerInactivityPeriodMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    // všechny záznamy o neaktivitě hráčů
    @GetMapping("/All")
    public List<PlayerInactivityPeriodDTO> getAll() {
        return service.getAll()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    // neaktivita hráčů dle id neaktivity
    @GetMapping("/{id}")
    public ResponseEntity<PlayerInactivityPeriodDTO> getById(@PathVariable Long id) {
        PlayerInactivityPeriodEntity entity = service.getById(id);
        return ResponseEntity.ok(mapper.toDTO(entity));
    }

// získá záznamy o periodě neaktivity dle id hráče
    @GetMapping("/player/{playerId}")
    public List<PlayerInactivityPeriodDTO> getByPlayer(@PathVariable Long playerId) {
        return service.getByPlayer(playerId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    // vytvoří záznam o neaktivitě hráče
    @PostMapping
    public ResponseEntity<PlayerInactivityPeriodDTO> create(@RequestBody PlayerInactivityPeriodDTO dto) {
        PlayerInactivityPeriodEntity entity = service.create(dto);
        return ResponseEntity.ok(mapper.toDTO(entity));
    }

    // změní záznam o neaktivitě hráče dle id
    @PutMapping("/{id}")
    public ResponseEntity<PlayerInactivityPeriodDTO> update(
            @PathVariable Long id,
            @RequestBody PlayerInactivityPeriodDTO dto) {

        PlayerInactivityPeriodEntity updated = service.update(id, dto);
        return ResponseEntity.ok(mapper.toDTO(updated));
    }
    // vymaže záznam o neaktivitě hráče dle id záznamu - Pouze pro testování, v praxi všechny záznamy musí zůstat
    // kvůli historii, aby se nemohl dívat na informace o zápasech u kterých nebyl aktivní
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
