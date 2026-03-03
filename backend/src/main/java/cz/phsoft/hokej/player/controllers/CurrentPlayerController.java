package cz.phsoft.hokej.player.controllers;

import cz.phsoft.hokej.player.dto.PlayerDTO;
import cz.phsoft.hokej.shared.dto.SuccessResponseDTO;
import cz.phsoft.hokej.player.services.PlayerService;
import cz.phsoft.hokej.player.services.CurrentPlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller, který se používá pro práci s aktuálním hráčem
 * přihlášeného uživatele.
 *
 * Aktuální hráč představuje kontext, ve kterém uživatel pracuje,
 * například při registraci na zápasy nebo při zobrazení statistik.
 * Controller umožňuje nastavení aktuálního hráče, automatický výběr
 * hráče po přihlášení a získání aktuálně zvoleného hráče.
 *
 * Veškerá business logika je delegována do PlayerService
 * a CurrentPlayerService.
 */
@RestController
@RequestMapping("/api/current-player")
public class CurrentPlayerController {

    private final CurrentPlayerService currentPlayerService;
    private final PlayerService playerService;

    /**
     * Vytváří instanci kontroleru pro práci s aktuálním hráčem.
     *
     * Závislosti na servisních třídách se předávají přes konstruktor
     * a používají se pro delegaci logiky do servisní vrstvy.
     *
     * @param currentPlayerService služba pro práci s aktuálním hráčem
     * @param playerService        služba pro správu hráčů
     */
    public CurrentPlayerController(CurrentPlayerService currentPlayerService,
                                   PlayerService playerService) {
        this.currentPlayerService = currentPlayerService;
        this.playerService = playerService;
    }

    /**
     * Nastavuje aktuálního hráče pro přihlášeného uživatele.
     *
     * Metoda se používá zejména v případech, kdy má uživatel přiřazeno
     * více hráčů a potřebuje mezi nimi ručně přepínat. Nastavení aktuálního
     * hráče je delegováno do PlayerService.
     *
     * @param playerId ID hráče, který má být nastaven jako aktuální
     * @param auth     autentizační kontext přihlášeného uživatele
     * @return SuccessResponseDTO s informací o provedené změně
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
     * uživatele podle nastavení v uživatelských preferencích.
     *
     * Logika výběru (například volba prvního hráče podle ID nebo ponechání
     * stavu bez vybraného hráče) je implementována v PlayerService.
     *
     * @param auth autentizační kontext přihlášeného uživatele
     * @return SuccessResponseDTO s výsledkem automatického výběru
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
     * Pokud není aktuální hráč nastaven, vrací se hodnota null v těle
     * odpovědi. V případě, že je aktuální hráč k dispozici, jeho data
     * se načítají z PlayerService.
     *
     * @return PlayerDTO s detaily aktuálního hráče nebo null, pokud není nastaven
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