package cz.phsoft.hokej.controllers;

// REMOVED: importy na entity a repozitáře
// import cz.phsoft.hokej.data.entities.AppUserEntity;
// import cz.phsoft.hokej.data.entities.PlayerEntity;
// import cz.phsoft.hokej.data.repositories.AppUserRepository;
// import cz.phsoft.hokej.data.repositories.PlayerRepository;

import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.SuccessResponseDTO;
import cz.phsoft.hokej.models.services.PlayerService;
import cz.phsoft.hokej.models.services.CurrentPlayerService;   // NEW – správný import interface
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// aktuální hráč
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

    // -----------------------------------------------------
    // Nastavení aktuálního hráče
    // -----------------------------------------------------
    @PostMapping("/{playerId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponseDTO> setCurrentPlayer(@PathVariable Long playerId,
                                                               Authentication auth) {

        SuccessResponseDTO response =
                playerService.setCurrentPlayerForUser(auth.getName(), playerId);

        return ResponseEntity.ok(response);
    }

    // -----------------------------------------------------
    // Automatický výběr aktuálního hráče po loginu
    // -----------------------------------------------------
    @PostMapping("/auto-select")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SuccessResponseDTO> autoSelectCurrentPlayer(Authentication auth) {

        SuccessResponseDTO response =
                playerService.autoSelectCurrentPlayerForUser(auth.getName());

        return ResponseEntity.ok(response);
    }

    // -----------------------------------------------------
    // Získání aktuálního hráče
    // -----------------------------------------------------
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

    // -----------------------------------------------------
    // Pomocný endpoint – seznam hráčů aktuálního uživatele
    // -----------------------------------------------------
    @GetMapping("/my-players")
    @PreAuthorize("isAuthenticated()")
    public List<PlayerDTO> getMyPlayers(Authentication auth) {
        return playerService.getPlayersByUser(auth.getName());
    }
}
