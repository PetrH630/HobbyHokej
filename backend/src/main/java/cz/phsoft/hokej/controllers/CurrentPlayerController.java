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
 * REST controller pro práci s „aktuálním hráčem“ přihlášeného uživatele.
 * <p>
 * Aktuální hráč představuje kontext, ve kterém uživatel pracuje
 * (např. registrace na zápasy, zobrazení statistik apod.).
 * <p>
 * Controller umožňuje:
 * <ul>
 *     <li>ruční nastavení aktuálního hráče,</li>
 *     <li>automatický výběr aktuálního hráče po přihlášení,</li>
 *     <li>získání aktuálního hráče,</li>
 *     <li>získání seznamu hráčů přihlášeného uživatele.</li>
 * </ul>
 *
 * Veškerá business logika je delegována do {@link PlayerService}
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
     * Nastaví aktuálního hráče pro přihlášeného uživatele.
     * <p>
     * Používá se zejména v případech, kdy má uživatel
     * přiřazeno více hráčů a chce mezi nimi ručně přepínat.
     *
     * @param playerId ID hráče, který má být nastaven jako aktuální
     * @param auth     autentizační kontext přihlášeného uživatele
     * @return informace o úspěšném nastavení aktuálního hráče
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
     * Automaticky zvolí aktuálního hráče pro přihlášeného uživatele.
     * <p>
     * Typicky se volá po přihlášení uživatele:
     * <ul>
     *     <li>pokud má uživatel právě jednoho hráče, je vybrán automaticky,</li>
     *     <li>pokud má více hráčů, výběr závisí na pravidlech ve service vrstvě.</li>
     * </ul>
     *
     * @param auth autentizační kontext přihlášeného uživatele
     * @return informace o výsledku automatického výběru
     */
    @PostMapping("/auto-select")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponseDTO> autoSelectCurrentPlayer(Authentication auth) {

        SuccessResponseDTO response =
                playerService.autoSelectCurrentPlayerForUser(auth.getName());

        return ResponseEntity.ok(response);
    }

    /**
     * Vrátí aktuálně zvoleného hráče přihlášeného uživatele.
     * <p>
     * Pokud uživatel nemá aktuálního hráče nastaveného,
     * je vrácena hodnota {@code null}.
     *
     * @return aktuální hráč nebo {@code null}, pokud není nastaven
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

    /**
     * Vrátí seznam všech hráčů patřících přihlášenému uživateli.
     * <p>
     * Slouží zejména pro výběr aktuálního hráče na frontendu.
     *
     * @param auth autentizační kontext přihlášeného uživatele
     * @return seznam hráčů aktuálního uživatele
     */
    @GetMapping("/my-players")
    @PreAuthorize("isAuthenticated()")
    public List<PlayerDTO> getMyPlayers(Authentication auth) {
        return playerService.getPlayersByUser(auth.getName());
    }
}
