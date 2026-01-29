package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.PlayerSettingsDTO;
import cz.phsoft.hokej.models.services.PlayerSettingsService;
import cz.phsoft.hokej.models.services.CurrentPlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller pro nastavení hráče (PlayerSettingsEntity).
 *
 * API:
 *  GET   /api/players/{playerId}/settings         – nastavení konkrétního hráče
 *  PATCH /api/players/{playerId}/settings         – aktualizace nastavení konkrétního hráče
 *
 *  GET   /api/current-player/settings             – nastavení aktuálního hráče (currentPlayer)
 *  PATCH /api/current-player/settings             – aktualizace nastavení aktuálního hráče
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

    // =========================
    // /api/players/{id}/settings
    // =========================

    @GetMapping("/players/{playerId}/settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlayerSettingsDTO> getPlayerSettings(
            @PathVariable Long playerId,
            Authentication auth
    ) {
        // POZNÁMKA:
        // Ověření, že hráč patří přihlášenému uživateli nebo že má roli ADMIN/MANAGER
        // může být buď tady, nebo v PlayerService/PlayerSettingsService.
        // Tady nechávám jen místo pro případnou kontrolu.

        PlayerSettingsDTO dto = playerSettingsService.getSettingsForPlayer(playerId);
        return ResponseEntity.ok(dto);
    }

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

    // =========================
    // /api/current-player/settings
    // =========================

    @GetMapping("/current-player/settings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlayerSettingsDTO> getCurrentPlayerSettings() {

        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();

        PlayerSettingsDTO dto = playerSettingsService.getSettingsForPlayer(currentPlayerId);

        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/current-player/settings")
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
