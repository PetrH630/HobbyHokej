package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.PlayerInactivityPeriodDTO;
import cz.phsoft.hokej.models.services.PlayerInactivityPeriodService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller pro administraci období neaktivity hráčů.
 * <p>
 * Období neaktivity slouží k evidenci časových úseků, ve kterých
 * hráč dočasně nevystupuje v zápasech (např. zranění, dovolená).
 * <p>
 * Endpoints jsou určeny pro role ADMIN a MANAGER (dle typu operace)
 * a umožňují kompletní správu záznamů o neaktivitě.
 *
 * Veškerá business logika je delegována do {@link PlayerInactivityPeriodService}.
 */
@RestController
@RequestMapping("/api/inactivity/admin")
public class PlayerInactivityPeriodController {

    private final PlayerInactivityPeriodService service;

    public PlayerInactivityPeriodController(PlayerInactivityPeriodService service) {
        this.service = service;
    }

    /**
     * Vrátí seznam všech záznamů o neaktivitě hráčů.
     *
     * @return seznam období neaktivity
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<PlayerInactivityPeriodDTO> getAll() {
        return service.getAll();
    }

    /**
     * Vrátí detail záznamu o neaktivitě podle jeho ID.
     *
     * @param id ID záznamu o neaktivitě
     * @return detail období neaktivity
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<PlayerInactivityPeriodDTO> getById(@PathVariable Long id) {
        PlayerInactivityPeriodDTO dto = service.getById(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * Vrátí všechna období neaktivity pro konkrétního hráče.
     *
     * @param playerId ID hráče
     * @return seznam období neaktivity hráče
     */
    @GetMapping("/player/{playerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<PlayerInactivityPeriodDTO> getByPlayer(@PathVariable Long playerId) {
        return service.getByPlayer(playerId);
    }

    /**
     * Vytvoří nový záznam o neaktivitě hráče.
     * <p>
     * Operace je vyhrazena pouze pro administrátora.
     *
     * @param dto data období neaktivity
     * @return vytvořený záznam o neaktivitě
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlayerInactivityPeriodDTO> create(
            @Valid @RequestBody PlayerInactivityPeriodDTO dto) {

        PlayerInactivityPeriodDTO created = service.create(dto);
        return ResponseEntity.ok(created);
    }

    /**
     * Aktualizuje existující záznam o neaktivitě hráče.
     * <p>
     * Operace je vyhrazena pouze pro administrátora.
     *
     * @param id  ID záznamu o neaktivitě
     * @param dto aktualizovaná data období neaktivity
     * @return aktualizovaný záznam o neaktivitě
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlayerInactivityPeriodDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PlayerInactivityPeriodDTO dto) {

        PlayerInactivityPeriodDTO updated = service.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Odstraní záznam o neaktivitě hráče.
     * <p>
     * Operace je vyhrazena pouze pro administrátora.
     *
     * @param id ID záznamu o neaktivitě
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
