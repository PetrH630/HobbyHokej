package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.services.CurrentPlayerService;
import cz.phsoft.hokej.models.services.PlayerService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller pro správu hráčů z pohledu přihlášeného uživatele.
 * <p>
 * Controller umožňuje uživateli:
 * <ul>
 *     <li>vytvořit vlastního hráče,</li>
 *     <li>získat seznam svých hráčů,</li>
 *     <li>upravit aktuálně zvoleného hráče.</li>
 * </ul>
 *
 * Operace probíhají vždy v kontextu přihlášeného uživatele
 * a (v případě úprav) také v kontextu „aktuálního hráče“.
 */
@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;
    private final CurrentPlayerService currentPlayerService;

    public PlayerController(PlayerService playerService,
                            CurrentPlayerService currentPlayerService) {
        this.playerService = playerService;
        this.currentPlayerService = currentPlayerService;
    }

    /**
     * Vytvoří nového hráče pro přihlášeného uživatele.
     *
     * @param playerDTO      data nového hráče
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return vytvořený hráč
     */
    @PostMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public PlayerDTO createMyPlayer(
            @Valid @RequestBody PlayerDTO playerDTO,
            Authentication authentication) {

        String email = authentication.getName();
        return playerService.createPlayerForUser(playerDTO, email);
    }

    /**
     * Vrátí seznam všech hráčů patřících přihlášenému uživateli.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return seznam hráčů přihlášeného uživatele
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public List<PlayerDTO> getMyPlayers(Authentication authentication) {

        String email = authentication.getName();
        return playerService.getPlayersByUser(email);
    }

    /**
     * Aktualizuje údaje aktuálně zvoleného hráče.
     * <p>
     * Vyžaduje, aby měl uživatel nastaveného „aktuálního hráče“.
     *
     * @param dto aktualizovaná data hráče
     * @return aktualizovaný hráč
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public PlayerDTO updatePlayer(@Valid @RequestBody PlayerDTO dto) {

        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();

        return playerService.updatePlayer(currentPlayerId, dto);
    }
}
