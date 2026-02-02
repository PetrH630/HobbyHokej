package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.SuccessResponseDTO;
import cz.phsoft.hokej.models.services.PlayerService;
import cz.phsoft.hokej.models.services.CurrentPlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller, který se používá pro práci s aktuálním hráčem
 * přihlášeného uživatele.
 *
 * Aktuální hráč představuje kontext, ve kterém uživatel pracuje
 * například při registraci na zápasy nebo při zobrazení statistik.
 * Controller umožňuje nastavení aktuálního hráče, automatický výběr
 * hráče po přihlášení a získání aktuálně zvoleného hráče.
 *
 * Veškerá business logika se předává do {@link PlayerService}
 * a {@link CurrentPlayerService}.
 */
@RestController
@RequestMapping("/api/current-player")
public class CurrentPlayerController {

    private final CurrentPlayerService currentPlayerService;
    private final PlayerService playerService;

    public CurrentPlayerController(CurrentPlayerService currentPlayerService,
                                   PlayerService playerService) {
        this.currentPlayerService = currentPlayerService;
        this.playerService = playerService;
    }

    /**
     * Nastavuje aktuálního hráče pro přihlášeného uživatele.
     *
     * Metoda se používá zejména v případech, kdy má uživatel přiřazeno
     * více hráčů a potřebuje mezi nimi ručně přepínat.
     *
     * @param playerId ID hráče, který má být nastaven jako aktuální
     * @param auth     autentizační kontext přihlášeného uživatele
     * @return DTO {@link SuccessResponseDTO} s informací o provedené změně
     */
    @PostMapping("/{playerId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponseDTO> setCurrentPlayer(
            @PathVariable Long playerId,
            Authentication auth) {

        SuccessResponseDTO response =
                playerService.setCurrentPlayerForUser(auth.getName(), playerId);

        return ResponseEntity.ok(response);
    }

    /**
     * Provádí automatický výběr aktuálního hráče pro přihlášeného
     * uživatele podle nastavení v AppUserSettings.
     *
     * Například může být vybrán první hráč podle ID nebo může být
     * ponechán stav bez vybraného hráče, aby si uživatel vybral hráče
     * ručně na frontendu.
     *
     * @param auth autentizační kontext přihlášeného uživatele
     * @return DTO {@link SuccessResponseDTO} s výsledkem automatického výběru
     */
    @PostMapping("/auto-select")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponseDTO> autoSelectCurrentPlayer(Authentication auth) {
        SuccessResponseDTO response =
                playerService.autoSelectCurrentPlayerForUser(auth.getName());

        return ResponseEntity.ok(response);
    }

    /**
     * Vrací aktuálně zvoleného hráče přihlášeného uživatele.
     *
     * Pokud není aktuální hráč nastaven, vrací se hodnota null.
     *
     * @return DTO {@link PlayerDTO} s detaily hráče nebo null
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlayerDTO> getCurrentPlayer() {

        Long playerId = currentPlayerService.getCurrentPlayerId();
        if (playerId == null) {
            return ResponseEntity.ok(null);
        }

        PlayerDTO player = playerService.getPlayerById(playerId);
        return ResponseEntity.ok(player);
    }
}
