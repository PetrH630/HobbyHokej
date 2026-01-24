package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.SuccessResponseDTO;
import cz.phsoft.hokej.models.services.CurrentPlayerService;
import cz.phsoft.hokej.models.services.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/players/admin")
@CrossOrigin(origins = "*")
public class AdminPlayerController {

    private final PlayerService playerService;
    private final CurrentPlayerService currentPlayerService;

    public AdminPlayerController(PlayerService playerService, CurrentPlayerService currentPlayerService) {
        this.playerService = playerService;
        this.currentPlayerService = currentPlayerService;
    }
    // všichni hráči
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<PlayerDTO> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    // hráč dle id

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public PlayerDTO getPlayerById(@PathVariable Long id) {
        return playerService.getPlayerById(id);
    }

    // vytvoření hráče
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
        public PlayerDTO createPlayer(@Valid @RequestBody PlayerDTO playerDTO) {
        return playerService.createPlayer(playerDTO);
    }

    // úprava hráče administrátorem dle id hráče
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PlayerDTO upatePlayerAdmin(@PathVariable Long id,  @Valid @RequestBody PlayerDTO dto) {

        return playerService.updatePlayer(id, dto);
    }
    // odstraní hráče
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponseDTO> deletePlayer(@PathVariable Long id) {
        SuccessResponseDTO response = playerService.deletePlayer(id);
        return ResponseEntity.ok(response);
    }

    //
    // SCHVÁLENÍ HRÁČE
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/approve/{id}")
    public ResponseEntity<SuccessResponseDTO> approvePlayer(@PathVariable Long id) {
        SuccessResponseDTO response = playerService.approvePlayer(id);
        return ResponseEntity.ok(response);

    }
    // ZAMÍTNUTÍ HRÁČE
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/reject/{id}")
    public ResponseEntity<SuccessResponseDTO> rejectPlayer(@PathVariable Long id) {
        SuccessResponseDTO response = playerService.rejectPlayer(id);
        return ResponseEntity.ok(response);

    }





}
