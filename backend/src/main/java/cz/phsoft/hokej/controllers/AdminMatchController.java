package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.MatchDTO;
import cz.phsoft.hokej.models.dto.SuccessResponseDTO;
import cz.phsoft.hokej.models.services.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches/admin")
@CrossOrigin(origins = "*")
public class AdminMatchController {
    private final MatchService matchService;
    private final CurrentPlayerService currentPlayerService;


    public AdminMatchController(MatchService matchService,
                           CurrentPlayerService currentPlayerService) {
        this.matchService = matchService;
        this.currentPlayerService = currentPlayerService;
    }

    // Všechny zápasy
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getAllMatches() {
        return matchService.getAllMatches();
    }

    // Všechny nadcházející zápasy
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping("/upcoming")
    public List<MatchDTO> getUpcomingMatches() {
        return matchService.getUpcomingMatches();
    }

    // Už uskutečněné zápasy
    @GetMapping("/past")
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
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchDTO getMatch(@PathVariable Long id) {
        return matchService.getMatchById(id);
    }

    // Editace zápasu
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchDTO updateMatch(@PathVariable Long id, @Valid @RequestBody MatchDTO dto) {
        return matchService.updateMatch(id, dto);
    }

    // Smazání zápasu
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponseDTO> deleteMatch(@PathVariable Long id) {
        SuccessResponseDTO response = matchService.deleteMatch(id);
        return ResponseEntity.ok(response);
    }

    // Dostupné zápasy pro hráče
    @GetMapping("/available-for-player/{playerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getAvailableMatchesForPlayer(@PathVariable Long playerId) {
        return matchService.getAvailableMatchesForPlayer(playerId);
    }

}
