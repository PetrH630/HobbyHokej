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
 * hráče (currentPlayer). Umožňuje načítat a aktualizovat nastavení
 * pro libovolného hráče podle ID a pro hráče, který je aktuálně
 * vybrán v kontextu přihlášeného uživatele.
 *
 * Veškerá business logika se předává do {@link PlayerSettingsService}.
 */
@RestController
@RequestMapping("/api")
public class PlayerSettingsController {

    private final PlayerSettingsService playerSettingsService;
    private final CurrentPlayerService currentPlayerService;

    public PlayerSettingsController(PlayerSettingsService playerSettingsService,
                                    CurrentPlayerService currentPlayerService) {
        this.playerSettingsService = playerSettingsService;
        this.currentPlayerService = currentPlayerService;
    }

    // Nastavení libovolného hráče podle ID

    /**
     * Vrací nastavení konkrétního hráče podle jeho ID.
     *
     * Ověření, zda hráč patří přihlášenému uživateli nebo zda má
     * uživatel roli ADMIN či MANAGER, může být prováděno v této
     * vrstvě nebo v service vrstvě.
     *
     * @param playerId ID hráče
     * @param auth     autentizační kontext přihlášeného uživatele
     * @return DTO {@link PlayerSettingsDTO} s nastavením hráče
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
     * Kontrola vlastnictví hráče a oprávnění může být doplněna
     * podle potřeb aplikace.
     *
     * @param playerId   ID hráče
     * @param requestDto DTO s novým nastavením hráče
     * @param auth       autentizační kontext přihlášeného uživatele
     * @return DTO {@link PlayerSettingsDTO} s aktualizovaným nastavením
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
     * Před čtením nastavení se vyžaduje, aby byl v kontextu
     * nastaven aktuální hráč.
     *
     * @return DTO {@link PlayerSettingsDTO} s nastavením aktuálního hráče
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
     * Informace o tom, který hráč je aktuální, se získává z
     * {@link CurrentPlayerService}. Endpoint se používá například
     * pro nastavení preferencí přímo z kontextu aktuálního hráče.
     *
     * @param requestDto DTO s novým nastavením aktuálního hráče
     * @return DTO {@link PlayerSettingsDTO} s aktualizovaným nastavením
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
