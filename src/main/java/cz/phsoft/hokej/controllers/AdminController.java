package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final PlayerRepository playerRepository;

    public AdminController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @GetMapping("/players/pending")
    public List<PlayerEntity> getPendingPlayers() {
        return playerRepository.findAll().stream()
                .filter(p -> !p.isEnabled())
                .toList();
    }

    @PostMapping("/players/{id}/approve")
    public PlayerEntity approvePlayer(@PathVariable Long id) {
        PlayerEntity player = playerRepository.findById(id).orElseThrow();
        player.setEnabled(true);
        return playerRepository.save(player);
    }

    @PostMapping("/players/{id}/disable")
    public PlayerEntity disablePlayer(@PathVariable Long id) {
        PlayerEntity player = playerRepository.findById(id).orElseThrow();
        player.setEnabled(false);
        return playerRepository.save(player);
    }
}
