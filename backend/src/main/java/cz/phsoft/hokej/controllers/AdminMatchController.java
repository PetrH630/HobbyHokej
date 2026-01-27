package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.data.enums.MatchCancelReason;
import cz.phsoft.hokej.models.dto.MatchDTO;
import cz.phsoft.hokej.models.dto.SuccessResponseDTO;
import cz.phsoft.hokej.models.services.CurrentPlayerService;
import cz.phsoft.hokej.models.services.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller pro administraci zápasů.
 * <p>
 * Endpoints jsou určeny pro role ADMIN a MANAGER a umožňují:
 * <ul>
 *     <li>správu zápasů (CRUD),</li>
 *     <li>získání seznamu nadcházejících a minulých zápasů,</li>
 *     <li>vyhodnocení dostupnosti zápasů pro konkrétního hráče,</li>
 *     <li>zrušení a opětovné obnovení zápasu.</li>
 * </ul>
 *
 * Veškerá business logika je delegována do {@link MatchService}.
 */
@RestController
@RequestMapping("/api/matches/admin")
public class AdminMatchController {

    private final MatchService matchService;
    private final CurrentPlayerService currentPlayerService;

    public AdminMatchController(MatchService matchService,
                                CurrentPlayerService currentPlayerService) {
        this.matchService = matchService;
        this.currentPlayerService = currentPlayerService;
    }

    /**
     * Vrátí seznam všech zápasů v systému.
     *
     * @return seznam všech zápasů
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getAllMatches() {
        return matchService.getAllMatches();
    }

    /**
     * Vrátí seznam všech nadcházejících zápasů.
     *
     * @return seznam budoucích zápasů
     */
    @GetMapping("/upcoming")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getUpcomingMatches() {
        return matchService.getUpcomingMatches();
    }

    /**
     * Vrátí seznam všech již odehraných (minulých) zápasů.
     *
     * @return seznam minulých zápasů
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
     * Vrátí detail zápasu podle jeho ID.
     *
     * @param id ID zápasu
     * @return detail zápasu
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
     * @return aktualizovaný zápas
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchDTO updateMatch(@PathVariable Long id,
                                @Valid @RequestBody MatchDTO dto) {
        return matchService.updateMatch(id, dto);
    }

    /**
     * Odstraní zápas ze systému.
     * <p>
     * Operace je vyhrazena pouze pro administrátora.
     *
     * @param id ID zápasu
     * @return informace o úspěšném smazání
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
     * @return seznam dostupných zápasů
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
     * @return informace o úspěšném zrušení
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
     * @return informace o úspěšném obnovení
     */
    @PatchMapping("/{matchId}/uncancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<SuccessResponseDTO> unCancelMatch(@PathVariable Long matchId) {
        SuccessResponseDTO response = matchService.unCancelMatch(matchId);
        return ResponseEntity.ok(response);
    }
}
