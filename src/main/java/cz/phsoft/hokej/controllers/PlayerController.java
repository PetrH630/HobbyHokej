package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.mappers.PlayerMapper;
import cz.phsoft.hokej.models.services.PlayerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // GET all players
    @GetMapping
    public List<PlayerDTO> getAllPlayers() {
        return playerService.getAllPlayers()
                .stream()
                .map(playerMapper::toDTO)
                .toList();
    }

    // GET by ID
    @GetMapping("/{id}")
    public PlayerDTO getPlayer(@PathVariable Long id) {
        return playerMapper.toDTO(playerService.getPlayerById(id));
    }

    // CREATE new player
    @PostMapping
    public PlayerDTO createPlayer(@RequestBody PlayerDTO dto) {
        return playerMapper.toDTO(
                playerService.createPlayer(playerMapper.toEntity(dto))
        );
    }

    // UPDATE player
    @PutMapping("/{id}")
    public PlayerDTO updatePlayer(@PathVariable Long id, @RequestBody PlayerDTO dto) {
/*
        // Map DTO → Entity (bez hesla)
        var newEntity = playerMapper.toEntity(dto);

        // Update uloženého hráče
        var updated = playerService.updatePlayer(id, newEntity);

        return playerMapper.toDTO(updated);
  */
        var entity = playerService.getPlayerById(id);
        playerMapper.updatePlayerEntity(dto, entity);
        return playerMapper.toDTO(entity);

    }

    // DELETE player
    @DeleteMapping("/{id}")
    public void deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
    }

    

}
