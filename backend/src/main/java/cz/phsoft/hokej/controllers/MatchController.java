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
 * REST controller, který se používá pro správu zápasů.
 *
 * Zajišťuje administraci zápasů pro role ADMIN a MANAGER včetně
 * vytváření, aktualizace, mazání, zrušení a obnovení zápasu. Dále
 * poskytuje pohled na zápasy z perspektivy aktuálního hráče a detail
 * zápasu včetně informací o registracích.
 *
 * Veškerá business logika se předává do {@link MatchService}.
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

    // ADMIN / MANAGER – globální správa zápasů

    /**
     * Vrací seznam všech zápasů v systému.
     *
     * Endpoint je dostupný pro role ADMIN a MANAGER.
     *
     * @return seznam zápasů jako {@link MatchDTO}
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getAllMatches() {
        return matchService.getAllMatches();
    }

    /**
     * Vrací seznam všech nadcházejících zápasů.
     *
     * Endpoint je dostupný pro role ADMIN a MANAGER.
     *
     * @return seznam nadcházejících zápasů
     */
    @GetMapping("/upcoming")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getUpcomingMatches() {
        return matchService.getUpcomingMatches();
    }

    /**
     * Vrací seznam všech již odehraných zápasů.
     *
     * Endpoint je dostupný pro role ADMIN a MANAGER.
     *
     * @return seznam minulých zápasů
     */
    @GetMapping("/past")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getPastMatches() {
        return matchService.getPastMatches();
    }

    /**
     * Vytváří nový zápas.
     *
     * @param matchDTO DTO s daty nového zápasu
     * @return vytvořený zápas jako {@link MatchDTO}
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public MatchDTO createMatch(@Valid @RequestBody MatchDTO matchDTO) {
        return matchService.createMatch(matchDTO);
    }

    /**
     * Vrací detail zápasu podle jeho ID.
     *
     * Jedná se o administrátorský nebo manažerský pohled bez vazby
     * na konkrétního hráče.
     *
     * @param id ID zápasu
     * @return DTO {@link MatchDTO} s detaily zápasu
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchDTO getMatch(@PathVariable Long id) {
        return matchService.getMatchById(id);
    }

    /**
     * Aktualizuje existující zápas.
     *
     * @param id  ID zápasu, který má být aktualizován
     * @param dto DTO s aktualizovanými daty zápasu
     * @return DTO {@link MatchDTO} s uloženými změnami
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchDTO updateMatch(@PathVariable Long id,
                                @Valid @RequestBody MatchDTO dto) {
        return matchService.updateMatch(id, dto);
    }

    /**
     * Odstraňuje zápas ze systému.
     *
     * Operace je vyhrazena pouze pro roli ADMIN.
     *
     * @param id ID zápasu, který má být odstraněn
     * @return DTO {@link SuccessResponseDTO} s výsledkem operace
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponseDTO> deleteMatch(@PathVariable Long id) {
        SuccessResponseDTO response = matchService.deleteMatch(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Vrací seznam zápasů, které jsou dostupné pro konkrétního hráče.
     *
     * Endpoint je dostupný pro role ADMIN a MANAGER.
     *
     * @param playerId ID hráče
     * @return seznam dostupných zápasů pro hráče
     */
    @GetMapping("/available-for-player/{playerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getAvailableMatchesForPlayer(@PathVariable Long playerId) {
        return matchService.getAvailableMatchesForPlayer(playerId);
    }

    /**
     * Ruší zápas a ukládá důvod zrušení.
     *
     * Operace je dostupná pro role ADMIN a MANAGER a typicky
     * spouští notifikační proces pro dotčené hráče.
     *
     * @param matchId ID zápasu
     * @param reason  důvod zrušení zápasu
     * @return DTO {@link SuccessResponseDTO} s výsledkem operace
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
     * Obnovuje dříve zrušený zápas.
     *
     * Operace je dostupná pro role ADMIN a MANAGER.
     *
     * @param matchId ID zápasu
     * @return DTO {@link SuccessResponseDTO} s výsledkem operace
     */
    @PatchMapping("/{matchId}/uncancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<SuccessResponseDTO> unCancelMatch(@PathVariable Long matchId) {
        SuccessResponseDTO response = matchService.unCancelMatch(matchId);
        return ResponseEntity.ok(response);
    }

    // Hráč – endpointy v kontextu aktuálního hráče

    /**
     * Vrací detail konkrétního zápasu z pohledu hráče.
     *
     * Detail zahrnuje například stav registrací, volná místa nebo
     * informace o tom, zda může hráč měnit svojí registraci.
     *
     * @param id ID zápasu
     * @return DTO {@link MatchDetailDTO} s detailem zápasu pro hráče
     */
    @GetMapping("/{id}/detail")
    @PreAuthorize("isAuthenticated()")
    public MatchDetailDTO getMatchDetail(@PathVariable Long id) {
        return matchService.getMatchDetail(id);
    }

    /**
     * Vrací nejbližší nadcházející zápas v systému.
     *
     * Endpoint se používá pro zobrazení nejbližšího zápasu
     * například na úvodní stránce.
     *
     * @return DTO {@link MatchDTO} s nejbližším zápasem nebo null
     */
    @GetMapping("/next")
    @PreAuthorize("isAuthenticated()")
    public MatchDTO getNextMatch() {
        return matchService.getNextMatch();
    }

    /**
     * Vrací seznam nadcházejících zápasů pro aktuálně zvoleného hráče.
     *
     * Před voláním služby se vyžaduje, aby byl nastaven aktuální hráč.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return seznam nadcházejících zápasů pro aktuálního hráče
     */
    @GetMapping("/me/upcoming")
    @PreAuthorize("isAuthenticated()")
    public List<MatchDTO> getUpcomingMatchesForMe(Authentication authentication) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return matchService.getUpcomingMatchesForPlayer(currentPlayerId);
    }

    /**
     * Vrací přehled nadcházejících zápasů pro aktuálního hráče.
     *
     * Přehled se používá například pro kompaktní zobrazení zápasů
     * v kartách na frontendu.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return seznam {@link MatchOverviewDTO} pro aktuálního hráče
     */
    @GetMapping("/me/upcoming-overview")
    @PreAuthorize("isAuthenticated()")
    public List<MatchOverviewDTO> getUpcomingMatchesOverviewForMe(Authentication authentication) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return matchService.getUpcomingMatchesOverviewForPlayer(currentPlayerId);
    }

    /**
     * Vrací seznam všech již odehraných zápasů pro aktuálního hráče.
     *
     * Seznam může být použit například pro zobrazení historie
     * zápasů daného hráče.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return seznam {@link MatchOverviewDTO} pro odehrané zápasy hráče
     */
    @GetMapping("/me/all-passed")
    @PreAuthorize("isAuthenticated()")
    public List<MatchOverviewDTO> getAllMatchesForPlayer(Authentication authentication) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return matchService.getAllPassedMatchesForPlayer(currentPlayerId);
    }
}
