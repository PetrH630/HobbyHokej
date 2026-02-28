package cz.phsoft.hokej.player.controllers;

import cz.phsoft.hokej.player.dto.PlayerDTO;
import cz.phsoft.hokej.player.dto.PlayerHistoryDTO;
import cz.phsoft.hokej.player.dto.PlayerStatsDTO;
import cz.phsoft.hokej.shared.dto.SuccessResponseDTO;
import cz.phsoft.hokej.user.dto.ChangePlayerUserRequest;
import cz.phsoft.hokej.player.services.CurrentPlayerService;
import cz.phsoft.hokej.player.services.PlayerHistoryService;
import cz.phsoft.hokej.player.services.PlayerService;
import cz.phsoft.hokej.player.services.PlayerStatsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller, který se používá pro správu hráčů.
 *
 * Zajišťuje administrativní správu hráčů pro role ADMIN a MANAGER,
 * včetně vytváření, aktualizace, mazání a schvalování hráčů, a také
 * správu hráčů z pohledu přihlášeného uživatele pod endpointy s prefixem /me.
 *
 * Veškerá business logika se deleguje do {@link PlayerService}.
 * Historie změn hráčů se získává pomocí {@link PlayerHistoryService}
 * a práce s aktuálním hráčem se zajišťuje přes {@link CurrentPlayerService}.
 */
@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;
    private final CurrentPlayerService currentPlayerService;
    private final PlayerHistoryService playerHistoryService;
    private final PlayerStatsService playerStatsService;

    public PlayerController(PlayerService playerService,
                            CurrentPlayerService currentPlayerService,
                            PlayerHistoryService playerHistoryService,
                            PlayerStatsService playerStatsService) {
        this.playerService = playerService;
        this.currentPlayerService = currentPlayerService;
        this.playerHistoryService = playerHistoryService;
        this.playerStatsService = playerStatsService;
    }

    // ADMIN / MANAGER – globální správa hráčů

    /**
     * Vrací seznam všech hráčů v systému.
     *
     * Endpoint je dostupný pro role ADMIN a MANAGER a slouží
     * pro přehledovou správu hráčů v administraci.
     *
     * @return seznam všech hráčů jako {@link PlayerDTO}
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<PlayerDTO> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    /**
     * Vrací detail hráče podle jeho ID.
     *
     * Endpoint je určen pro administrativní pohled na hráče,
     * například pro úpravu jeho údajů.
     *
     * @param id ID hráče
     * @return {@link PlayerDTO} s detailem hráče
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public PlayerDTO getPlayerById(@PathVariable Long id) {
        return playerService.getPlayerById(id);
    }

    /**
     * Vrací historii hráče podle jeho ID.
     *
     * Historie obsahuje záznamy o změnách provedených nad daným hráčem
     * a slouží pro auditní a přehledové účely.
     *
     * @param id ID hráče
     * @return seznam {@link PlayerHistoryDTO} představujících historii hráče
     */
    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<PlayerHistoryDTO> getPlayerHistoryById(@PathVariable Long id) {
        return playerHistoryService.getHistoryForPlayer(id);
    }

    /**
     * Vytváří nového hráče administrátorem nebo manažerem.
     *
     * Operace se používá při ručním zakládání hráče v systému,
     * typicky pro dodatečné doplnění hráčů mimo uživatelské rozhraní.
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
     * Aktualizuje údaje hráče v administrativním rozhraní.
     *
     * Endpoint je dostupný pro role ADMIN a MANAGER a slouží
     * k úpravě existujících údajů hráče.
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
     * Operace je dostupná pro role ADMIN a MANAGER a používá se
     * pouze ve výjimečných situacích, například při chybném založení
     * hráče. Samotné odstranění se provádí v servisní vrstvě.
     *
     * @param id ID hráče
     * @return {@link SuccessResponseDTO} s výsledkem operace
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
     * Endpoint se používá po kontrole údajů hráče administrátorem
     * nebo manažerem. Schválení umožňuje hráči plnohodnotné využití
     * systému.
     *
     * @param id ID hráče
     * @return {@link SuccessResponseDTO} s výsledkem operace
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
     * Endpoint se používá v situacích, kdy hráč nesplňuje podmínky
     * pro schválení, například z hlediska validity údajů.
     *
     * @param id ID hráče
     * @return {@link SuccessResponseDTO} s výsledkem operace
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
     * pokud je potřeba hráče převést k jinému uživateli, například
     * při změně vlastníka účtu.
     *
     * @param playerId ID hráče
     * @param request  požadavek obsahující ID nového uživatele
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
     * Nový hráč se automaticky přiřazuje k uživatelskému účtu
     * odvozenému z e-mailové adresy v objektu {@link Authentication}.
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
     * Hráči se identifikují na základě e-mailové adresy uživatele
     * získané z autentizačního kontextu.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return seznam {@link PlayerDTO} patřících danému uživateli
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
     * aktuální hráč v {@link CurrentPlayerService}. Aktualizace
     * se následně deleguje do {@link PlayerService}.
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
     * Vrací historii pro aktuálně zvoleného hráče přihlášeného uživatele.
     *
     * Identita hráče se získává z {@link CurrentPlayerService}. Endpoint
     * je určen pro uživatelské zobrazení historie změn nad vlastním hráčem.
     *
     * @return seznam {@link PlayerHistoryDTO} představujících historii aktuálního hráče
     */
    @GetMapping("/me/history")
    @PreAuthorize("isAuthenticated()")
    public List<PlayerHistoryDTO> getMyPlayerHistory() {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return playerHistoryService.getHistoryForPlayer(currentPlayerId);
    }


    /**
     * Vrací statistiky pro aktuálně zvoleného hráče přihlášeného uživatele.
     *
     * Identita hráče se získává z {@link CurrentPlayerService}. Endpoint
     * je určen pro zobrazení statistik k zápasům vlastním hráčem.
     *
     * @return seznam {@link PlayerStatsDTO} představujících statistiky aktuálního hráče
     */
    @GetMapping("/me/stats")
    @PreAuthorize("isAuthenticated()")
    public PlayerStatsDTO getMyPlayerStats() {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return playerStatsService.getPlayerStats(currentPlayerId);
    }

    /**
     * Vrací statistiky pro aktuálně zvoleného hráče přihlášeného uživatele.
     *
     * je určen pro zobrazení statistik k zápasům hráče dle jeho ide.
     * @param playerId ID hráče
     * @return seznam {@link PlayerStatsDTO} představujících statistiky hráče dle id
     */
    @GetMapping("/{playerId}/stats")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public PlayerStatsDTO getPlayerStats(@PathVariable Long playerId) {
        return playerStatsService.getPlayerStats(playerId);
    }



}
