package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.SuccessResponseDTO;
import cz.phsoft.hokej.models.dto.requests.ChangePlayerUserRequest;
import cz.phsoft.hokej.models.services.CurrentPlayerService;
import cz.phsoft.hokej.models.services.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller pro správu hráčů.
 *
 * Zajišťuje:
 * <ul>
 *     <li>administraci hráčů (CRUD, schválení / zamítnutí, změna uživatele) pro role ADMIN/MANAGER,</li>
 *     <li>správu hráčů z pohledu přihlášeného uživatele (moji hráči /me).</li>
 * </ul>
 *
 * Veškerá business logika je delegována do {@link PlayerService}.
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

    // =========================================================
    //  ADMIN / MANAGER – GLOBÁLNÍ SPRÁVA HRÁČŮ
    // =========================================================

    /**
     * Vrátí seznam všech hráčů v systému.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<PlayerDTO> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    /**
     * Vrátí detail hráče podle jeho ID.
     *
     * @param id ID hráče
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public PlayerDTO getPlayerById(@PathVariable Long id) {
        return playerService.getPlayerById(id);
    }

    /**
     * Vytvoří nového hráče (administrátor/manažer).
     *
     * Používá se při ručním zakládání hráče do systému.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public PlayerDTO createPlayer(@Valid @RequestBody PlayerDTO playerDTO) {
        return playerService.createPlayer(playerDTO);
    }

    /**
     * Aktualizuje údaje hráče administrátorem.
     *
     * @param id  ID hráče
     * @param dto aktualizovaná data hráče
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PlayerDTO updatePlayerAdmin(@PathVariable Long id,
                                       @Valid @RequestBody PlayerDTO dto) {
        return playerService.updatePlayer(id, dto);
    }

    /**
     * Odstraní hráče ze systému.
     *
     * Operace je vyhrazena pouze pro administrátora.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponseDTO> deletePlayer(@PathVariable Long id) {
        SuccessResponseDTO response = playerService.deletePlayer(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Schválí hráče (změní jeho stav na APPROVED).
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponseDTO> approvePlayer(@PathVariable Long id) {
        SuccessResponseDTO response = playerService.approvePlayer(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Zamítne hráče (změní jeho stav na REJECTED).
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponseDTO> rejectPlayer(@PathVariable Long id) {
        SuccessResponseDTO response = playerService.rejectPlayer(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Změní přiřazení hráče k aplikačnímu uživateli.
     *
     * Operace je vyhrazena pouze pro roli ADMIN.
     *
     * @param playerId ID hráče, kterému má být změněn přiřazený uživatel
     * @param request  request obsahující ID nového uživatele
     */
    @PostMapping("/{playerId}/change-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> changePlayerUser(
            @PathVariable Long playerId,
            @RequestBody ChangePlayerUserRequest request
    ) {
        playerService.changePlayerUser(playerId, request.getNewUserId());
        return ResponseEntity.ok(
                "Hráč s id: " + playerId + " byl úspěšně přiřazen uživateli s ID: " + request.getNewUserId()
        );
    }

    // =========================================================
    //  USER – HRÁČI PŘIHLÁŠENÉHO UŽIVATELE (/me)
    // =========================================================

    /**
     * Vytvoří nového hráče pro přihlášeného uživatele.
     */
    @PostMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public PlayerDTO createMyPlayer(
            @Valid @RequestBody PlayerDTO playerDTO,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return playerService.createPlayerForUser(playerDTO, email);
    }

    /**
     * Vrátí seznam všech hráčů patřících přihlášenému uživateli.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public List<PlayerDTO> getMyPlayers(Authentication authentication) {
        String email = authentication.getName();
        return playerService.getPlayersByUser(email);
    }

    /**
     * Aktualizuje údaje aktuálně zvoleného hráče.
     *
     * Vyžaduje, aby měl uživatel nastaveného „aktuálního hráče“.
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public PlayerDTO updateMyCurrentPlayer(@Valid @RequestBody PlayerDTO dto) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return playerService.updatePlayer(currentPlayerId, dto);
    }
}
