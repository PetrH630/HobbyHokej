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

    // Nadcházející zápas - NEPOUŽÍVAT - NENÍ TAM PLAYER TYPE
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/next")
    public MatchDTO getNextMatch() {
        return matchService.getNextMatch();
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





}
