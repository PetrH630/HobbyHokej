package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.PlayerInactivityPeriodDTO;
import cz.phsoft.hokej.models.services.PlayerInactivityPeriodService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inactivity")
@CrossOrigin(origins = "*")
public class PlayerInactivityPeriodController {

    private final PlayerInactivityPeriodService service;

    public PlayerInactivityPeriodController(PlayerInactivityPeriodService service) {
        this.service = service;
    }

    // všechny záznamy o neaktivitě hráčů
    @GetMapping("/all")
    public List<PlayerInactivityPeriodDTO> getAll() {
        return service.getAll();
    }

    // neaktivita hráčů dle id neaktivity
    @GetMapping("/{id}")
    public ResponseEntity<PlayerInactivityPeriodDTO> getById(@PathVariable Long id) {
        PlayerInactivityPeriodDTO dto = service.getById(id);
        return ResponseEntity.ok(dto);
    }

    // získá záznamy o periodě neaktivity dle id hráče
    @GetMapping("/player/{playerId}")
    public List<PlayerInactivityPeriodDTO> getByPlayer(@PathVariable Long playerId) {
        return service.getByPlayer(playerId);
    }

    // vytvoří záznam o neaktivitě hráče
    @PostMapping
    public ResponseEntity<PlayerInactivityPeriodDTO> create(@RequestBody PlayerInactivityPeriodDTO dto) {
        PlayerInactivityPeriodDTO created = service.create(dto);
        return ResponseEntity.ok(created);
    }

    // změní záznam o neaktivitě hráče dle id
    @PutMapping("/{id}")
    public ResponseEntity<PlayerInactivityPeriodDTO> update(
            @PathVariable Long id,
            @RequestBody PlayerInactivityPeriodDTO dto) {

        PlayerInactivityPeriodDTO updated = service.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    // vymaže záznam o neaktivitě hráče dle id záznamu
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
