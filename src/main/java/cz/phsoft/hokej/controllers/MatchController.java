package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.MatchDTO;
import cz.phsoft.hokej.models.dto.mappers.MatchMapper;
import cz.phsoft.hokej.models.services.MatchService;
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

    // všechny zápasy
    @GetMapping
    public List<MatchDTO> getAllMatches() {
        return matchService.getAllMatches();
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

    // první aktuální zápas
    @GetMapping("/next")
    public MatchDTO getNextMatch() {
        return matchService.getNextMatch();
    }

    // GET podle ID
    @GetMapping("/{id}")
    public MatchDTO getMatch(@PathVariable Long id) {
        return matchService.getMatchById(id);
    }

    // POST přidat zápas
    @PostMapping
    public MatchDTO createMatch(@RequestBody MatchDTO dto) {
        return matchService.createMatch(dto);
    }

    // PUT editovat zápas
    @PutMapping("/{id}")
    public MatchDTO updateMatch(@PathVariable Long id, @RequestBody MatchDTO dto) {
        return matchService.updateMatch(id, dto);
    }

    // DELETE smazat zápas
    @DeleteMapping("/{id}")
    public void deleteMatch(@PathVariable Long id) {
        matchService.deleteMatch(id);
    }
}
