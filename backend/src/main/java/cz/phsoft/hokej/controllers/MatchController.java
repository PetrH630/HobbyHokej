package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.MatchDTO;
import cz.phsoft.hokej.models.dto.MatchDetailDTO;
import cz.phsoft.hokej.models.dto.MatchOverviewDTO;
import cz.phsoft.hokej.models.dto.SuccessResponseDTO;
import cz.phsoft.hokej.models.services.MatchService;
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


    public MatchController(MatchService matchService) {
        this.matchService = matchService;

    }

    // Detail zápasu
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or @playerSecurity.isOwner(authentication, #playerId)")
    @GetMapping("/upcoming")
    public List<MatchDTO> getUpcomingMatches() {
        return matchService.getUpcomingMatches();
    }

    // Nadcházející zápasy pro přihlášeného hráče
    @GetMapping("/me/upcoming")
    @PreAuthorize("isAuthenticated()")
    public List<MatchDTO> getUpcomingMatchesForMe(Authentication authentication) {
        // z UserDetails získáme username
        String email = authentication.getName();
        // najdeme hráče podle emailu
        Long playerId = matchService.getPlayerIdByEmail(email); // metoda si vytvoříme ve službě
        return matchService.getUpcomingMatchesForPlayer(playerId);
    }

    @GetMapping("/me/upcoming-overview")
    @PreAuthorize("isAuthenticated()")
    public List<MatchOverviewDTO> getUpcomingMatchesOverviewForMe(Authentication authentication) {
        String email = authentication.getName();
        Long playerId = matchService.getPlayerIdByEmail(email);
        return matchService.getUpcomingMatchesOverviewForPlayer(playerId);
    }



    // Nadcházející zápasy pro konkrétního hráče
    @GetMapping("/player/{playerId}/upcoming")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or @playerSecurity.isOwner(authentication, #playerId)")
    public List<MatchDTO> getUpcomingMatchesForPlayer(@PathVariable Long playerId) {
        return matchService.getUpcomingMatchesForPlayer(playerId);

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
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or @playerSecurity.isOwner(authentication, #playerId)")
    public List<MatchDTO> getAvailableMatchesForPlayer(@PathVariable Long playerId) {
        return matchService.getAvailableMatchesForPlayer(playerId);
    }

}
