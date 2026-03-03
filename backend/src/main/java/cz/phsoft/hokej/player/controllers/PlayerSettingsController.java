package cz.phsoft.hokej.player.controllers;

import cz.phsoft.hokej.player.dto.PlayerSettingsDTO;
import cz.phsoft.hokej.player.services.PlayerSettingsService;
import cz.phsoft.hokej.player.services.CurrentPlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller, který se používá pro správu nastavení hráče.
 *
 * Pracuje s nastavením navázaným na konkrétního hráče a na aktuálního
 * hráče. Umožňuje načítat a aktualizovat nastavení pro libovolného hráče
 * podle ID a pro hráče, který je aktuálně vybrán v kontextu přihlášeného
 * uživatele.
 *
 * Veškerá business logika je delegována do PlayerSettingsService.
 * Identifikace aktuálního hráče se zajišťuje pomocí CurrentPlayerService.
 */
@RestController
@RequestMapping("/api")
public class PlayerSettingsController {

    private final PlayerSettingsService playerSettingsService;
    private final CurrentPlayerService currentPlayerService;

    /**
     * Vytváří instanci kontroleru pro správu nastavení hráče.
     *
     * Závislosti na servisních třídách se předávají přes konstruktor
     * a používají se pro delegaci veškeré logiky do servisní vrstvy.
     *
     * @param playerSettingsService služba pro práci s nastavením hráčů
     * @param currentPlayerService  služba pro práci s aktuálním hráčem
     */
    public PlayerSettingsController(PlayerSettingsService playerSettingsService,
                                    CurrentPlayerService currentPlayerService) {
        this.playerSettingsService = playerSettingsService;
        this.currentPlayerService = currentPlayerService;
    }

    // Nastavení libovolného hráče podle ID

    /**
     * Vrací nastavení konkrétního hráče podle jeho ID.
     *
     * Endpoint je dostupný přihlášeným uživatelům. Kontrola, zda hráč
     * patří danému uživateli nebo zda má uživatel roli administrátora,
     * se provádí podle potřeb aplikace, typicky v servisní vrstvě.
     *
     * @param playerId ID hráče, pro kterého se načítá nastavení
     * @param auth     autentizační kontext přihlášeného uživatele
     * @return PlayerSettingsDTO s nastavením hráče
     */
    @GetMapping("/players/{playerId}/settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlayerSettingsDTO> getPlayerSettings(
            @PathVariable Long playerId,
            Authentication auth
    ) {
        PlayerSettingsDTO dto = playerSettingsService.getSettingsForPlayer(playerId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Aktualizuje nastavení konkrétního hráče podle jeho ID.
     *
     * Endpoint je dostupný přihlášeným uživatelům. Kontrola vlastnictví
     * hráče a oprávnění pro změnu nastavení se může provádět v této vrstvě
     * nebo v PlayerSettingsService, podle návrhu bezpečnostního modelu.
     *
     * @param playerId   ID hráče, pro kterého se mění nastavení
     * @param requestDto DTO s novým nastavením hráče
     * @param auth       autentizační kontext přihlášeného uživatele
     * @return PlayerSettingsDTO s aktualizovaným nastavením hráče
     */
    @PatchMapping("/players/{playerId}/settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlayerSettingsDTO> updatePlayerSettings(
            @PathVariable Long playerId,
            @RequestBody PlayerSettingsDTO requestDto,
            Authentication auth
    ) {
        // TODO: případná kontrola vlastnictví hráče

        PlayerSettingsDTO updated = playerSettingsService.updateSettingsForPlayer(playerId, requestDto);
        return ResponseEntity.ok(updated);
    }

    // Nastavení aktuálního hráče (currentPlayer)

    /**
     * Vrací nastavení aktuálně vybraného hráče.
     *
     * Před čtením nastavení se vyžaduje, aby byl v CurrentPlayerService
     * nastaven aktuální hráč. Endpoint se používá pro načtení nastavení
     * přímo pro hráče, se kterým uživatel aktuálně pracuje.
     *
     * @return PlayerSettingsDTO s nastavením aktuálního hráče
     */
    @GetMapping("/me/settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlayerSettingsDTO> getCurrentPlayerSettings() {

        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();

        PlayerSettingsDTO dto = playerSettingsService.getSettingsForPlayer(currentPlayerId);

        return ResponseEntity.ok(dto);
    }

    /**
     * Aktualizuje nastavení aktuálně vybraného hráče.
     *
     * Identita aktuálního hráče se získává z CurrentPlayerService.
     * Endpoint se používá typicky pro nastavení preferencí a dalších
     * parametrů přímo v kontextu hráče, kterého má uživatel právě zvoleného.
     *
     * @param requestDto DTO s novým nastavením aktuálního hráče
     * @return PlayerSettingsDTO s aktualizovaným nastavením aktuálního hráče
     */
    @PatchMapping("/me/settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlayerSettingsDTO> updateCurrentPlayerSettings(
            @RequestBody PlayerSettingsDTO requestDto
    ) {

        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();

        PlayerSettingsDTO updated = playerSettingsService.updateSettingsForPlayer(currentPlayerId, requestDto);

        return ResponseEntity.ok(updated);
    }
}