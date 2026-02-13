package cz.phsoft.hokej.controllers;

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
 * Veškerá business logika se deleguje do {@link PlayerInactivityPeriodService}.
 * Informace o aktuálním hráči se získávají pomocí {@link CurrentPlayerService}.
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
     * Endpoint je dostupný pro role ADMIN a MANAGER a slouží
     * k přehledové správě všech evidovaných období neaktivity.
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
     * Endpoint se používá pro zobrazení nebo kontrolu konkrétního
     * záznamu před jeho úpravou či smazáním.
     *
     * @param id ID záznamu o neaktivitě
     * @return {@link PlayerInactivityPeriodDTO} s detailem období neaktivity
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
     * Endpoint je vhodný například pro kontrolu dlouhodobé absence
     * hráče nebo pro plánování jeho účasti na zápasech.
     *
     * @param playerId ID hráče
     * @return seznam období neaktivity pro daného hráče jako {@link PlayerInactivityPeriodDTO}
     */
    @GetMapping("/player/{playerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<PlayerInactivityPeriodDTO> getByPlayer(@PathVariable Long playerId) {
        return service.getByPlayer(playerId);
    }

    /**
     * Vytváří nový záznam o neaktivitě hráče.
     *
     * Vstupní data jsou validována pomocí bean validation a vlastní
     * uložení záznamu se deleguje do servisní vrstvy. Operace je
     * dostupná pro role ADMIN a MANAGER.
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
     * Endpoint je dostupný pro role ADMIN a MANAGER. Aktualizace
     * probíhá prostřednictvím servisní vrstvy a slouží k opravám
     * nebo úpravám dříve zadaných údajů.
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
     * Operace je vyhrazena pouze pro roli ADMIN a používá se
     * například při chybném zadání období neaktivity.
     *
     * @param id ID záznamu o neaktivitě
     * @return HTTP odpověď 204 No Content v případě úspěchu
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Vrací seznam období neaktivity aktuálně zvoleného hráče.
     *
     * Před voláním služby se vyžaduje, aby byl nastaven aktuální hráč
     * v {@link CurrentPlayerService}. Endpoint je určen pro uživatelské
     * zobrazení vlastních období neaktivity v rozhraní hráče.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return seznam období neaktivity pro aktuálního hráče jako {@link PlayerInactivityPeriodDTO}
     */
    @GetMapping("/me/all")
    @PreAuthorize("isAuthenticated()")
    public List<PlayerInactivityPeriodDTO> getMyInactivity(Authentication authentication) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return service.getByPlayer(currentPlayerId);
    }
}
