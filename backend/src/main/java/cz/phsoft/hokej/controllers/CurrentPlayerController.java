package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.security.CurrentPlayerContext;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


// aktuální hráč
@RestController
@RequestMapping("/api/current-player")
public class CurrentPlayerController {

    private final PlayerRepository playerRepository;
    private final AppUserRepository appUserRepository;

    public CurrentPlayerController(PlayerRepository playerRepository,
                                   AppUserRepository appUserRepository) {
        this.playerRepository = playerRepository;
        this.appUserRepository = appUserRepository;
    }

    @PostMapping("/{playerId}")
    public void setCurrentPlayer(@PathVariable Long playerId, Authentication auth) {
        AppUserEntity user = appUserRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PlayerEntity player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        if (!player.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Player does not belong to user");
        }

        CurrentPlayerContext.set(player);
        System.out.println("Aktuální hráč nastaven na ID: " + player.getId());
    }

    // Získání aktuálního hráče
    @GetMapping
    public PlayerEntity getCurrentPlayer() {
        PlayerEntity current = CurrentPlayerContext.get();

        if (current != null) {
            System.out.println("Aktuální hráč ID: " + current.getId()); // výpis do konzole
            return current;
        } else {
            System.out.println("Žádný aktuální hráč");
            return null;
        }
    }

    // Pomocný endpoint pro získání seznamu hráčů aktuálního uživatele
    @GetMapping("/my-players")
    public List<PlayerEntity> getMyPlayers(Authentication auth) {
        AppUserEntity user = appUserRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<PlayerEntity> players = playerRepository.findAllByUserEmail(user.getEmail());

        System.out.println("Seznam hráčů uživatele " + user.getEmail() + ": " +
                players.stream().map(PlayerEntity::getId).toList());

        return players;
    }
}
