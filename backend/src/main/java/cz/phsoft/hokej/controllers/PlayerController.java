package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.SuccessResponseDTO;
import cz.phsoft.hokej.models.dto.mappers.PlayerMapper;
import cz.phsoft.hokej.models.services.PlayerService;
import cz.phsoft.hokej.security.CurrentPlayerService;
import org.springframework.http.ResponseEntity;
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

    // všichni hráči
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<PlayerDTO> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    // hráč dle id

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public PlayerDTO getPlayerById(@PathVariable Long id) {
        return playerService.getPlayerById(id);
    }

    /*
    /// vytvoření hráče
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PostMapping
    public PlayerDTO createPlayer(@RequestBody PlayerDTO playerDTO) {
        return playerService.createPlayer(playerDTO);
    }
    */
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

    // úprava hráče administrátorem dle id hráče
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PlayerDTO upatePlayerAdmin(@PathVariable Long id,  @RequestBody PlayerDTO dto) {

        return playerService.updatePlayer(id, dto);
    }


    // odstraní hráče
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<SuccessResponseDTO> deletePlayer(@PathVariable Long id) {
        SuccessResponseDTO response = playerService.deletePlayer(id);
        return ResponseEntity.ok(response);
    }


}
