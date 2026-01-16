package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.MatchDTO;
import cz.phsoft.hokej.models.dto.MatchDetailDTO;
import cz.phsoft.hokej.models.dto.MatchOverviewDTO;
import cz.phsoft.hokej.models.dto.SuccessResponseDTO;
import cz.phsoft.hokej.models.services.MatchService;
import cz.phsoft.hokej.security.CurrentPlayerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*")
public class MatchController {

    private final MatchService matchService;
    private final CurrentPlayerService currentPlayerService;


    public MatchController(MatchService matchService,
                           CurrentPlayerService currentPlayerService) {
        this.matchService = matchService;
        this.currentPlayerService = currentPlayerService;
    }

    // Detail zápasu
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/matchDetail/{id}")
    public MatchDetailDTO getMatchDetail(@PathVariable Long id) {
        return matchService.getMatchDetail(id);
    }

    // Všechny zápasy
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getAllMatches() {
        return matchService.getAllMatches();
    }

    // Nadcházející zápas
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/next")
    public MatchDTO getNextMatch() {
        return matchService.getNextMatch();
    }

    // Všechny nadcházející zápasy
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping("/admin/upcoming")
    public List<MatchDTO> getUpcomingMatches() {
        return matchService.getUpcomingMatches();
    }

    // Nadcházející zápasy pro přihlášeného hráče
    @GetMapping("/me/upcoming")
    @PreAuthorize("isAuthenticated()")
    public List<MatchDTO> getUpcomingMatchesForMe(Authentication authentication) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return matchService.getUpcomingMatchesForPlayer(currentPlayerId);
    }

    @GetMapping("/me/upcoming-overview")
    @PreAuthorize("isAuthenticated()")
    public List<MatchOverviewDTO> getUpcomingMatchesOverviewForMe(Authentication authentication) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return matchService.getUpcomingMatchesOverviewForPlayer(currentPlayerId);
    }

    @GetMapping("/me/all-passed")
    @PreAuthorize("isAuthenticated()")
    public List<MatchOverviewDTO> getAllMatchesForPlayer(Authentication authentication) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return matchService.getAllPassedMatchesForPlayer(currentPlayerId);
    }

    // Už uskutečněné zápasy
    @GetMapping("/admin/past")
    public List<MatchDTO> getPastMatches() {
        return matchService.getPastMatches();
    }

    // Vytvoření zápasu
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public MatchDTO createMatch(@Valid @RequestBody MatchDTO matchDTO) {
        return matchService.createMatch(matchDTO);
    }

    // Získání zápasu podle ID
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchDTO getMatch(@PathVariable Long id) {
        return matchService.getMatchById(id);
    }

    // Editace zápasu
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchDTO updateMatch(@PathVariable Long id, @Valid @RequestBody MatchDTO dto) {
        return matchService.updateMatch(id, dto);
    }

    // Smazání zápasu
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponseDTO> deleteMatch(@PathVariable Long id) {
        SuccessResponseDTO response = matchService.deleteMatch(id);
        return ResponseEntity.ok(response);
    }

    // Dostupné zápasy pro hráče
    @GetMapping("/admin/available-for-player/{playerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getAvailableMatchesForPlayer(@PathVariable Long playerId) {
        return matchService.getAvailableMatchesForPlayer(playerId);
    }



}
