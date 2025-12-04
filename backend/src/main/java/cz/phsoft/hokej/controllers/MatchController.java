package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.models.dto.MatchDTO;
import cz.phsoft.hokej.models.dto.MatchDetailDTO;
import cz.phsoft.hokej.models.services.MatchService;
import jakarta.validation.Valid;
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

    // detail zápasu
    @GetMapping("/matchDetail/{id}")
    public MatchDetailDTO getMatchDetail(@PathVariable Long id) {
        return matchService.getMatchDetail(id);
    }

    // všechny zápasy
    @GetMapping
    public List<MatchDTO> getAllMatches() {
        return matchService.getAllMatches();
    }

    // první aktuální zápas
    @GetMapping("/next")
    public MatchDTO getNextMatch() {
        return matchService.getNextMatch();
    }

    // aktuální zápasy (bez prošlých dat)
    @GetMapping("/upcoming")
    public List<MatchDTO> getUpcomingMatches() {
        return matchService.getUpcomingMatches();
    }

    // prošlé zápasy
    @GetMapping("/past")
    public List<MatchDTO> getPastMatches() {
        return matchService.getPastMatches();
    }


    // POST přidat zápas
    @PostMapping
    public MatchDTO createMatch(@Valid @RequestBody MatchDTO dto) {
        return matchService.createMatch(dto);
    }

    // GET podle ID
    @GetMapping("/{id}")
    public MatchDTO getMatch(@PathVariable Long id) {
        return matchService.getMatchById(id);
    }


    // PUT editovat zápas
    @PutMapping("/{id}")
    public MatchDTO updateMatch(@PathVariable Long id, @Valid @RequestBody MatchDTO dto) {
        return matchService.updateMatch(id, dto);
    }

    // DELETE smazat zápas
    @DeleteMapping("/{id}")
    public void deleteMatch(@PathVariable Long id) {
        matchService.deleteMatch(id);
    }
    // dostupné zápasy pro hráče - byl active
    @GetMapping("/available-for-player/{id}")
    public List<MatchEntity> getAvailableMatchesForPlayer(@PathVariable Long id) {
        return matchService.getAvailableMatchesForPlayer(id);
    }

}
