package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.data.enums.MatchCancelReason;
import cz.phsoft.hokej.models.dto.MatchDTO;
import cz.phsoft.hokej.models.dto.MatchDetailDTO;
import cz.phsoft.hokej.models.dto.MatchOverviewDTO;
import cz.phsoft.hokej.models.dto.SuccessResponseDTO;
import cz.phsoft.hokej.models.services.CurrentPlayerService;
import cz.phsoft.hokej.models.services.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller pro práci se zápasy.
 *
 * Zajišťuje:
 * <ul>
 *     <li>administraci zápasů (CRUD, zrušení / obnovení) pro role ADMIN/MANAGER,</li>
 *     <li>pohled na zápasy z perspektivy aktuálního hráče,</li>
 *     <li>detail zápasu včetně stavů registrací pro aktuálního hráče.</li>
 * </ul>
 *
 * Business logika je delegována do {@link MatchService}.
 */
@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;
    private final CurrentPlayerService currentPlayerService;

    public MatchController(MatchService matchService,
                           CurrentPlayerService currentPlayerService) {
        this.matchService = matchService;
        this.currentPlayerService = currentPlayerService;
    }

    // =========================================================
    //  ADMIN / MANAGER – GLOBÁLNÍ SPRÁVA ZÁPASŮ
    // =========================================================

    /**
     * Vrátí seznam všech zápasů v systému.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getAllMatches() {
        return matchService.getAllMatches();
    }

    /**
     * Vrátí seznam všech nadcházejících zápasů.
     */
    @GetMapping("/upcoming")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getUpcomingMatches() {
        return matchService.getUpcomingMatches();
    }

    /**
     * Vrátí seznam všech již odehraných (minulých) zápasů.
     */
    @GetMapping("/past")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getPastMatches() {
        return matchService.getPastMatches();
    }

    /**
     * Vytvoří nový zápas.
     *
     * @param matchDTO data nového zápasu
     * @return vytvořený zápas
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public MatchDTO createMatch(@Valid @RequestBody MatchDTO matchDTO) {
        return matchService.createMatch(matchDTO);
    }

    /**
     * Vrátí základní detail zápasu podle jeho ID
     * (admin / manager pohled – DTO bez hráčského kontextu).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchDTO getMatch(@PathVariable Long id) {
        return matchService.getMatchById(id);
    }

    /**
     * Aktualizuje existující zápas.
     *
     * @param id  ID zápasu
     * @param dto aktualizovaná data zápasu
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchDTO updateMatch(@PathVariable Long id,
                                @Valid @RequestBody MatchDTO dto) {
        return matchService.updateMatch(id, dto);
    }

    /**
     * Odstraní zápas ze systému.
     *
     * Operace je vyhrazena pouze pro administrátora.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponseDTO> deleteMatch(@PathVariable Long id) {
        SuccessResponseDTO response = matchService.deleteMatch(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Vrátí seznam zápasů, které jsou dostupné pro konkrétního hráče.
     *
     * @param playerId ID hráče
     */
    @GetMapping("/available-for-player/{playerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getAvailableMatchesForPlayer(@PathVariable Long playerId) {
        return matchService.getAvailableMatchesForPlayer(playerId);
    }

    /**
     * Zruší zápas a uloží důvod zrušení.
     *
     * @param matchId ID zápasu
     * @param reason  důvod zrušení zápasu
     */
    @PatchMapping("/{matchId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<SuccessResponseDTO> cancelMatch(
            @PathVariable Long matchId,
            @RequestParam MatchCancelReason reason
    ) {
        SuccessResponseDTO response = matchService.cancelMatch(matchId, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Obnoví dříve zrušený zápas.
     *
     * @param matchId ID zápasu
     */
    @PatchMapping("/{matchId}/uncancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<SuccessResponseDTO> unCancelMatch(@PathVariable Long matchId) {
        SuccessResponseDTO response = matchService.unCancelMatch(matchId);
        return ResponseEntity.ok(response);
    }

    // =========================================================
    //  HRÁČ – ENDPOINTY V KONTEXTU AKTUÁLNÍHO PLAYERA
    // =========================================================

    /**
     * Vrátí detail konkrétního zápasu z pohledu hráče
     * (včetně stavu registrací, práv, atd.).
     *
     * Změnil jsem URL na /{id}/detail kvůli konzistenci s ostatními
     * endpointy; pokud potřebuješ zachovat /matchDetail/{id},
     * můžeš přidat alias.
     */
    @GetMapping("/{id}/detail")
    @PreAuthorize("isAuthenticated()")
    public MatchDetailDTO getMatchDetail(@PathVariable Long id) {
        return matchService.getMatchDetail(id);
    }

    // Volitelný alias pro zpětnou kompatibilitu:
    // @GetMapping("/matchDetail/{id}")
    // public MatchDetailDTO getMatchDetailAlias(@PathVariable Long id) {
    //     return getMatchDetail(id);
    // }

    /**
     * Vrátí nejbližší nadcházející zápas (globálně).
     */
    @GetMapping("/next")
    @PreAuthorize("isAuthenticated()")
    public MatchDTO getNextMatch() {
        return matchService.getNextMatch();
    }

    /**
     * Vrátí seznam nadcházejících zápasů pro aktuálně zvoleného hráče.
     */
    @GetMapping("/me/upcoming")
    @PreAuthorize("isAuthenticated()")
    public List<MatchDTO> getUpcomingMatchesForMe(Authentication authentication) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return matchService.getUpcomingMatchesForPlayer(currentPlayerId);
    }

    /**
     * Vrátí přehled nadcházejících zápasů pro aktuálního hráče.
     */
    @GetMapping("/me/upcoming-overview")
    @PreAuthorize("isAuthenticated()")
    public List<MatchOverviewDTO> getUpcomingMatchesOverviewForMe(Authentication authentication) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return matchService.getUpcomingMatchesOverviewForPlayer(currentPlayerId);
    }

    /**
     * Vrátí seznam všech již odehraných zápasů pro aktuálního hráče.
     */
    @GetMapping("/me/all-passed")
    @PreAuthorize("isAuthenticated()")
    public List<MatchOverviewDTO> getAllMatchesForPlayer(Authentication authentication) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return matchService.getAllPassedMatchesForPlayer(currentPlayerId);
    }
}
