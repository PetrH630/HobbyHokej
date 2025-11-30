package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.MatchDTO;
import cz.phsoft.hokej.models.services.MatchService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping("/{matchId}/players/{playerId}")
    public MatchDTO prihlasitHrace(@PathVariable Long matchId, @PathVariable Long playerId) {
        return matchService.prihlasitHrace(matchId, playerId);
    }

    @DeleteMapping("/{matchId}/players/{playerId}")
    public MatchDTO odhlasitHrace(@PathVariable Long matchId, @PathVariable Long playerId) {
        return matchService.odhlasitHrace(matchId, playerId);
    }

}
