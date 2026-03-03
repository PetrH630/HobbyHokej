package cz.phsoft.hokej.player.controllers;

import cz.phsoft.hokej.player.dto.PlayerInactivityPeriodDTO;
import cz.phsoft.hokej.player.services.CurrentPlayerService;
import cz.phsoft.hokej.player.services.PlayerInactivityPeriodService;
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
 * Veškerá business logika je delegována do PlayerInactivityPeriodService.
 * Informace o aktuálním hráči se získávají pomocí CurrentPlayerService.
 */
@RestController
@RequestMapping("/api/inactivity/admin")
public class PlayerInactivityPeriodController {

    private final PlayerInactivityPeriodService service;
    private final CurrentPlayerService currentPlayerService;

    /**
     * Vytváří instanci kontroleru pro správu období neaktivity.
     *
     * Závislosti na servisních třídách se předávají přes konstruktor
     * a používají se pro delegaci logiky do servisní vrstvy.
     *
     * @param service              služba pro správu období neaktivity hráčů
     * @param currentPlayerService služba pro práci s aktuálním hráčem
     */
    public PlayerInactivityPeriodController(PlayerInactivityPeriodService service,
                                            CurrentPlayerService currentPlayerService) {
        this.service = service;
        this.currentPlayerService = currentPlayerService;
    }

    /**
     * Vrací seznam všech záznamů o neaktivitě hráčů.
     *
     * Endpoint je dostupný pro role ADMIN a MANAGER a používá se
     * k přehledové správě všech evidovaných období neaktivity.
     * Data se získávají z PlayerInactivityPeriodService.
     *
     * @return seznam období neaktivity jako PlayerInactivityPeriodDTO
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
     * záznamu před jeho úpravou či smazáním. Detail se načítá
     * z PlayerInactivityPeriodService.
     *
     * @param id ID záznamu o neaktivitě
     * @return PlayerInactivityPeriodDTO s detailem období neaktivity
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
     * Endpoint se používá například pro kontrolu dlouhodobé absence
     * hráče nebo pro plánování jeho účasti na zápasech. Data se načítají
     * z PlayerInactivityPeriodService.
     *
     * @param playerId ID hráče
     * @return seznam období neaktivity pro daného hráče jako PlayerInactivityPeriodDTO
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
     * uložení záznamu je delegováno do servisní vrstvy. Operace je
     * dostupná pro role ADMIN a MANAGER.
     *
     * @param dto DTO s daty období neaktivity
     * @return vytvořený záznam jako PlayerInactivityPeriodDTO
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
     * je prováděna prostřednictvím servisní vrstvy a používá se
     * k opravám nebo úpravám dříve zadaných údajů.
     *
     * @param id  ID záznamu o neaktivitě
     * @param dto DTO s aktualizovanými daty období neaktivity
     * @return aktualizovaný záznam jako PlayerInactivityPeriodDTO
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
     * Operace je určena pro role ADMIN a MANAGER a používá se
     * například při chybném zadání období neaktivity nebo při
     * čištění historických dat. Odstranění je zajištěno
     * v PlayerInactivityPeriodService.
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
     * v CurrentPlayerService. Endpoint je určen pro uživatelské
     * zobrazení vlastních období neaktivity v rozhraní hráče.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return seznam období neaktivity pro aktuálního hráče jako PlayerInactivityPeriodDTO
     */
    @GetMapping("/me/all")
    @PreAuthorize("isAuthenticated()")
    public List<PlayerInactivityPeriodDTO> getMyInactivity(Authentication authentication) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return service.getByPlayer(currentPlayerId);
    }
}