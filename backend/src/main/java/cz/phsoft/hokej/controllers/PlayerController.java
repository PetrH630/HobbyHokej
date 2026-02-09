package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.PlayerHistoryDTO;
import cz.phsoft.hokej.models.dto.SuccessResponseDTO;
import cz.phsoft.hokej.models.dto.requests.ChangePlayerUserRequest;
import cz.phsoft.hokej.models.services.CurrentPlayerService;
import cz.phsoft.hokej.models.services.PlayerHistoryService;
import cz.phsoft.hokej.models.services.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import cz.phsoft.hokej.models.services.PlayerHistoryService;

import java.util.List;

/**
 * REST controller, který se používá pro správu hráčů.
 *
 * Zajišťuje administrativní správu hráčů pro role ADMIN a MANAGER,
 * včetně vytváření, aktualizace, mazání a schvalování hráčů, a také
 * správu hráčů z pohledu přihlášeného uživatele pod endpointy /me.
 *
 * Veškerá business logika se předává do {@link PlayerService}.
 */
@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;
    private final CurrentPlayerService currentPlayerService;
    private final PlayerHistoryService playerHistoryService;

    public PlayerController(PlayerService playerService,
                            CurrentPlayerService currentPlayerService,
                            PlayerHistoryService playerHistoryService) {
        this.playerService = playerService;
        this.currentPlayerService = currentPlayerService;
        this.playerHistoryService = playerHistoryService;
    }

    // ADMIN / MANAGER – globální správa hráčů

    /**
     * Vrací seznam všech hráčů v systému.
     *
     * Endpoint je dostupný pro role ADMIN a MANAGER.
     *
     * @return seznam {@link PlayerDTO}
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<PlayerDTO> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    /**
     * Vrací detail hráče podle jeho ID.
     *
     * @param id ID hráče
     * @return DTO {@link PlayerDTO} s detailem hráče
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public PlayerDTO getPlayerById(@PathVariable Long id) {
        return playerService.getPlayerById(id);
    }

    /**
     * Vrací historii hráče podle jeho ID.
     *
     * @param id ID hráče
     * @return DTO {@link List<PlayerHistoryDTO>} s historii hráče
     */
    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<PlayerHistoryDTO> getPlayerHistoryById(@PathVariable Long id) {
        return playerHistoryService.getHistoryForPlayer(id);
    }

    /**
     * Vytváří nového hráče administrátorem nebo manažerem.
     *
     * Operace se používá při ručním zakládání hráče v systému.
     *
     * @param playerDTO DTO s daty nového hráče
     * @return vytvořený hráč jako {@link PlayerDTO}
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
     * @param dto DTO s aktualizovanými daty hráče
     * @return aktualizovaný hráč jako {@link PlayerDTO}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public PlayerDTO updatePlayerAdmin(@PathVariable Long id,
                                       @Valid @RequestBody PlayerDTO dto) {
        return playerService.updatePlayer(id, dto);
    }

    /**
     * Odstraňuje hráče ze systému.
     *
     * Operace je vyhrazena pouze pro roli ADMIN.
     *
     * @param id ID hráče
     * @return DTO {@link SuccessResponseDTO} s výsledkem operace
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<SuccessResponseDTO> deletePlayer(@PathVariable Long id) {
        SuccessResponseDTO response = playerService.deletePlayer(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Schvaluje hráče a nastavuje jeho stav na APPROVED.
     *
     * @param id ID hráče
     * @return DTO {@link SuccessResponseDTO} s výsledkem operace
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<SuccessResponseDTO> approvePlayer(@PathVariable Long id) {
        SuccessResponseDTO response = playerService.approvePlayer(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Zamítá hráče a nastavuje jeho stav na REJECTED.
     *
     * @param id ID hráče
     * @return DTO {@link SuccessResponseDTO} s výsledkem operace
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<SuccessResponseDTO> rejectPlayer(@PathVariable Long id) {
        SuccessResponseDTO response = playerService.rejectPlayer(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Mění přiřazení hráče k aplikačnímu uživateli.
     *
     * Operace je určena pro roli ADMIN nebo MANAGER a používá se,
     * pokud je potřeba hráče převést k jinému uživateli.
     *
     * @param playerId ID hráče
     * @param request  request obsahující ID nového uživatele
     * @return textová zpráva o úspěšné změně přiřazení
     */
    @PostMapping("/{playerId}/change-user")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<String> changePlayerUser(
            @PathVariable Long playerId,
            @RequestBody ChangePlayerUserRequest request
    ) {
        playerService.changePlayerUser(playerId, request.getNewUserId());
        return ResponseEntity.ok(
                "Hráč s id: " + playerId + " byl úspěšně přiřazen uživateli s ID: " + request.getNewUserId()
        );
    }

    // Uživatelská správa hráčů přihlášeného uživatele

    /**
     * Vytváří nového hráče pro přihlášeného uživatele.
     *
     * Nový hráč se automaticky přiřazuje k uživatelskému účtu,
     * který vychází z e-mailu v objektu {@link Authentication}.
     *
     * @param playerDTO      DTO s daty nového hráče
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return vytvořený hráč jako {@link PlayerDTO}
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
     * Vrací seznam všech hráčů patřících přihlášenému uživateli.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return seznam {@link PlayerDTO} pro daného uživatele
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
     * Před provedením aktualizace se vyžaduje, aby byl nastaven
     * aktuální hráč v {@link CurrentPlayerService}.
     *
     * @param dto DTO s aktualizovanými daty hráče
     * @return aktualizovaný hráč jako {@link PlayerDTO}
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public PlayerDTO updateMyCurrentPlayer(@Valid @RequestBody PlayerDTO dto) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return playerService.updatePlayer(currentPlayerId, dto);
    }

    /**
     * Vrací historii aktuálně přihlášeného hráče podle jeho ID.
     *
     * @param id ID hráče
     * @return DTO {@link List<PlayerHistoryDTO>} s historii hráče
     */
    @GetMapping("/me/history")
    @PreAuthorize("isAuthenticated()")
    public List<PlayerHistoryDTO> getMyPlayerHistory(Long id) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return playerHistoryService.getHistoryForPlayer(currentPlayerId);
    }

}
