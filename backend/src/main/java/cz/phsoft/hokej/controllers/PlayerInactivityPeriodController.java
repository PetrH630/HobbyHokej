package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.MatchDTO;
import cz.phsoft.hokej.models.dto.PlayerInactivityPeriodDTO;
import cz.phsoft.hokej.models.services.CurrentPlayerService;
import cz.phsoft.hokej.models.services.PlayerInactivityPeriodService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller, který se používá pro administraci období neaktivity hráčů.
 *
 * Období neaktivity slouží k evidenci časových úseků, ve kterých se hráč
 * neúčastní zápasů, například z důvodu zranění nebo dovolené. Endpointy jsou
 * určeny pro role ADMIN a MANAGER a umožňují úplnou správu záznamů o neaktivitě.
 *
 * Veškerá business logika se předává do {@link PlayerInactivityPeriodService}.
 */
@RestController
@RequestMapping("/api/inactivity/admin")
public class PlayerInactivityPeriodController {

    private final PlayerInactivityPeriodService service;
    private final CurrentPlayerService currentPlayerService;

    public PlayerInactivityPeriodController(PlayerInactivityPeriodService service,
                                            CurrentPlayerService currentPlayerService) {
        this.service = service;
        this.currentPlayerService = currentPlayerService;
    }

    /**
     * Vrací seznam všech záznamů o neaktivitě hráčů.
     *
     * @return seznam období neaktivity jako {@link PlayerInactivityPeriodDTO}
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<PlayerInactivityPeriodDTO> getAll() {
        return service.getAll();
    }

    /**
     * Vrací detail záznamu o neaktivitě podle jeho ID.
     *
     * @param id ID záznamu o neaktivitě
     * @return DTO {@link PlayerInactivityPeriodDTO} s detailem období neaktivity
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<PlayerInactivityPeriodDTO> getById(@PathVariable Long id) {
        PlayerInactivityPeriodDTO dto = service.getById(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * Vrací všechna období neaktivity pro konkrétního hráče.
     *
     * @param playerId ID hráče
     * @return seznam období neaktivity pro daného hráče
     */
    @GetMapping("/player/{playerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<PlayerInactivityPeriodDTO> getByPlayer(@PathVariable Long playerId) {
        return service.getByPlayer(playerId);
    }

    /**
     * Vytváří nový záznam o neaktivitě hráče.
     *
     * Operace je vyhrazena pouze pro roli ADMIN.
     *
     * @param dto DTO s daty období neaktivity
     * @return vytvořený záznam jako {@link PlayerInactivityPeriodDTO}
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<PlayerInactivityPeriodDTO> create(
            @Valid @RequestBody PlayerInactivityPeriodDTO dto) {

        PlayerInactivityPeriodDTO created = service.create(dto);
        return ResponseEntity.ok(created);
    }

    /**
     * Aktualizuje existující záznam o neaktivitě hráče.
     *
     * Operace je vyhrazena pouze pro roli ADMIN.
     *
     * @param id  ID záznamu o neaktivitě
     * @param dto DTO s aktualizovanými daty období neaktivity
     * @return aktualizovaný záznam jako {@link PlayerInactivityPeriodDTO}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<PlayerInactivityPeriodDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PlayerInactivityPeriodDTO dto) {

        PlayerInactivityPeriodDTO updated = service.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Odstraňuje záznam o neaktivitě hráče.
     *
     * Operace je vyhrazena pouze pro roli ADMIN.
     *
     * @param id ID záznamu o neaktivitě
     * @return HTTP odpověď 204 No Content v případě úspěchu
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // TODO- V SERVICE ZKONTROLOVAT.
    /**
     * Vrací seznam neaktivit aktuálně zvoleného hráče.
     *
     * Před voláním služby se vyžaduje, aby byl nastaven aktuální hráč.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return seznam neaktivit pro aktuálního hráče
     */
    @GetMapping("/me/all")
    @PreAuthorize("isAuthenticated()")
    public List<PlayerInactivityPeriodDTO> getMyInactivity(Authentication authentication) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return service.getByPlayer(currentPlayerId);
    }
}


