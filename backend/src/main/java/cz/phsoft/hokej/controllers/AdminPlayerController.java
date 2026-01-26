package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.SuccessResponseDTO;
import cz.phsoft.hokej.models.dto.requests.ChangePlayerUserRequest;
import cz.phsoft.hokej.models.services.CurrentPlayerService;
import cz.phsoft.hokej.models.services.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller pro administraci hráčů v systému.
 * <p>
 * Endpoints jsou určeny pro role ADMIN a MANAGER (dle konkrétní operace)
 * a umožňují:
 * <ul>
 *     <li>správu hráčů (CRUD),</li>
 *     <li>schvalování a zamítání hráčů,</li>
 *     <li>získání detailu hráče nebo seznamu všech hráčů.</li>
 * </ul>
 *
 * Controller neobsahuje business logiku – veškeré zpracování
 * je delegováno do {@link PlayerService}.
 */
@RestController
@RequestMapping("/api/players/admin")
@CrossOrigin(origins = "*")
public class AdminPlayerController {

    private final PlayerService playerService;
    private final CurrentPlayerService currentPlayerService;

    public AdminPlayerController(PlayerService playerService,
                                 CurrentPlayerService currentPlayerService) {
        this.playerService = playerService;
        this.currentPlayerService = currentPlayerService;
    }

    /**
     * Vrátí seznam všech hráčů v systému.
     *
     * @return seznam hráčů
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
     * @return detail hráče
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public PlayerDTO getPlayerById(@PathVariable Long id) {
        return playerService.getPlayerById(id);
    }

    /**
     * Vytvoří nového hráče.
     * <p>
     * Používá se typicky administrátorem nebo manažerem
     * při ručním zakládání hráče do systému.
     *
     * @param playerDTO data nového hráče
     * @return vytvořený hráč
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
     * @return aktualizovaný hráč
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PlayerDTO updatePlayerAdmin(@PathVariable Long id,
                                       @Valid @RequestBody PlayerDTO dto) {
        return playerService.updatePlayer(id, dto);
    }

    /**
     * Odstraní hráče ze systému.
     * <p>
     * Operace je vyhrazena pouze pro administrátora.
     *
     * @param id ID hráče
     * @return informace o úspěšném provedení operace
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponseDTO> deletePlayer(@PathVariable Long id) {
        SuccessResponseDTO response = playerService.deletePlayer(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Schválí hráče (změní jeho stav na APPROVED).
     *
     * @param id ID hráče
     * @return informace o úspěšném schválení
     */
    @PutMapping("/approve/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponseDTO> approvePlayer(@PathVariable Long id) {
        SuccessResponseDTO response = playerService.approvePlayer(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Zamítne hráče (změní jeho stav na REJECTED).
     *
     * @param id ID hráče
     * @return informace o úspěšném zamítnutí
     */
    @PutMapping("/reject/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponseDTO> rejectPlayer(@PathVariable Long id) {
        SuccessResponseDTO response = playerService.rejectPlayer(id);
        return ResponseEntity.ok(response);
    }
    /**
     * Změní přiřazení hráče k aplikačnímu uživateli.
     * <p>
     * Slouží k administrátorské korekci vazby mezi {@code Player} a {@code AppUser},
     * typicky v případech:
     * <ul>
     *     <li>chybně spárovaného uživatelského účtu,</li>
     *     <li>sloučení duplicitních hráčů nebo uživatelů,</li>
     *     <li>ruční administrátorské opravy dat.</li>
     * </ul>
     *
     * Operace je vyhrazena pouze pro roli ADMIN.
     *
     * @param playerId ID hráče, kterému má být změněn přiřazený uživatel
     * @param request request obsahující ID nového uživatele
     * @return textová informace o úspěšném provedení operace
     */
    @PostMapping("/{playerId}/change-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> changePlayerUser (
            @PathVariable Long playerId, @RequestBody ChangePlayerUserRequest request) {

        playerService.changePlayerUser(playerId, request.getNewUserId());

        return ResponseEntity.ok("Hráč s id: " + playerId + " byl úspěšně přiřazen uživateli s ID: " + request.getNewUserId());
    }

}
