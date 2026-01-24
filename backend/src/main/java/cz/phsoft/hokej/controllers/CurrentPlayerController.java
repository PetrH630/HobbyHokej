package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.services.PlayerService;
import cz.phsoft.hokej.security.CurrentPlayerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.util.List;

// aktuální hráč
@RestController
@RequestMapping("/api/current-player")
public class CurrentPlayerController {

    private final PlayerRepository playerRepository;
    private final AppUserRepository appUserRepository;
    private final CurrentPlayerService currentPlayerService;
    private final PlayerService playerService;

    public CurrentPlayerController(PlayerRepository playerRepository,
                                   AppUserRepository appUserRepository,
                                   CurrentPlayerService currentPlayerService,
                                   PlayerService playerService) {
        this.playerRepository = playerRepository;
        this.appUserRepository = appUserRepository;
        this.currentPlayerService = currentPlayerService;
        this.playerService = playerService;
    }

    // -----------------------------------------------------
    // Nastavení aktuálního hráče – pokud uživatel má jen jednoho, vybere se automaticky
    // -----------------------------------------------------
    @PostMapping("/{playerId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> setCurrentPlayer(@PathVariable Long playerId,
                                 Authentication auth,
                                 HttpSession session) {

        AppUserEntity user = appUserRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        if (!player.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Player does not belong to user");
        }

        currentPlayerService.setCurrentPlayerId(player.getId());
        return ResponseEntity.ok("Aktuální hráč nastaven na ID: " + player.getId());
    }

    // -----------------------------------------------------
    // Automatický výběr aktuálního hráče po loginu
    // Zavolat z frontendu /api/current-player/auto-select po přihlášení
    // -----------------------------------------------------
    @PostMapping("/auto-select")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> autoSelectCurrentPlayer(Authentication auth) {
        AppUserEntity user = appUserRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PlayerDTO> players = playerService.getPlayersByUser(user.getEmail());

        if (players.size() == 1) {
            PlayerDTO player = players.get(0);
            currentPlayerService.setCurrentPlayerId(player.getId());
            return ResponseEntity.ok("Automaticky nastaven hráč nastaven na ID: " + player.getId());
        } else {
            return ResponseEntity.ok("Uživatel má více hráčů, výběr nutný ručně");
        }
    }

    // -----------------------------------------------------
    // Získání aktuálního hráče
    // -----------------------------------------------------
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PlayerDTO getCurrentPlayer(HttpSession session) {

        Long playerId = currentPlayerService.getCurrentPlayerId();
        if (playerId == null) {
            System.out.println("Žádný aktuální hráč");
            return null;
        }

        PlayerDTO player = playerService.getPlayerById(playerId);
        System.out.println("Aktuální hráč ID: " + playerId);
        return player;
    }

    // -----------------------------------------------------
    // Pomocný endpoint – seznam hráčů aktuálního uživatele
    // -----------------------------------------------------
    @GetMapping("/my-players")
    @PreAuthorize("isAuthenticated()")
    public List<PlayerDTO> getMyPlayers(Authentication auth, HttpSession session) {
        AppUserEntity user = appUserRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PlayerDTO> players = playerService.getPlayersByUser(user.getEmail());

        System.out.println("Seznam hráčů uživatele " + user.getEmail() + ": " +
                players.stream().map(PlayerDTO::getId).toList());

        return players;
    }
}
