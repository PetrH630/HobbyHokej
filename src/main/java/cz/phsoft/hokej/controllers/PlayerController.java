package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.mappers.PlayerMapper;
import cz.phsoft.hokej.models.services.PlayerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/players")
@CrossOrigin(origins = "*")
public class PlayerController {

    private final PlayerService playerService;
    private final PlayerMapper playerMapper;

    public PlayerController(PlayerService playerService, PlayerMapper playerMapper) {
        this.playerService = playerService;
        this.playerMapper = playerMapper;
    }

    // GET all
    @GetMapping
    public List<PlayerDTO> getAllPlayers() {
        return playerService.getAllPlayers()
                .stream()
                .map(playerMapper::toDTO)
                .collect(Collectors.toList());
    }

    // GET by ID
    @GetMapping("/{id}")
    public PlayerDTO getPlayer(@PathVariable Long id) {
        return playerMapper.toDTO(playerService.getPlayerById(id));
    }

    // POST create
    @PostMapping
    public PlayerDTO createPlayer(@RequestBody PlayerDTO playerDTO) {
        return playerMapper.toDTO(
                playerService.createPlayer(playerMapper.toEntity(playerDTO))
        );
    }

    // PUT update
    @PutMapping("/{id}")
    public PlayerDTO updatePlayer(@PathVariable Long id, @RequestBody PlayerDTO playerDTO) {
        return playerMapper.toDTO(
                playerService.updatePlayer(id, playerMapper.toEntity(playerDTO))
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
    }
}
