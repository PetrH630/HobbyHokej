package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.services.CurrentPlayerService;
import cz.phsoft.hokej.models.services.PlayerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
@CrossOrigin(origins = "*")
public class PlayerController {

    private final PlayerService playerService;
    private final CurrentPlayerService currentPlayerService;

    public PlayerController(PlayerService playerService, CurrentPlayerService currentPlayerService) {
        this.playerService = playerService;
        this.currentPlayerService = currentPlayerService;
    }

    // vytvoření hráče pro přihlášeného uživatele
    @PostMapping("/me")
    @PreAuthorize("isAuthenticated()") // každý přihlášený uživatel
    public PlayerDTO createMyPlayer(@RequestBody PlayerDTO playerDTO, Authentication authentication) {
        String email = authentication.getName(); // email přihlášeného uživatele
        return playerService.createPlayerForUser(playerDTO, email);
    }

    // získání hráčů přihlášeného uživatele
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public List<PlayerDTO> getMyPlayers(Authentication authentication) {
        String email = authentication.getName();
        return playerService.getPlayersByUser(email);
    }

    // úprava hráče přihlášeného uživatele
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public PlayerDTO updatePlayer(@RequestBody PlayerDTO dto) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();

        return playerService.updatePlayer(currentPlayerId, dto);
    }
}
