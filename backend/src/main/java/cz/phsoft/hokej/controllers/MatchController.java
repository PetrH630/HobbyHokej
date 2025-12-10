package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.data.entities.MatchEntity;
import cz.phsoft.hokej.models.dto.MatchDTO;
import cz.phsoft.hokej.models.dto.MatchDetailDTO;
import cz.phsoft.hokej.models.dto.mappers.MatchMapper;
import cz.phsoft.hokej.models.services.MatchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*")
public class MatchController {

    private final MatchService matchService;
    private final MatchMapper matchMapper;

    public MatchController(MatchService matchService, MatchMapper matchMapper) {
        this.matchService = matchService;
        this.matchMapper = matchMapper;
    }

    // Detail zápasu
    @GetMapping("/matchDetail/{id}")
    public MatchDetailDTO getMatchDetail(@PathVariable Long id) {
        return matchService.getMatchDetail(id);
    }

    // Všechny zápasy
    @GetMapping
    public List<MatchDTO> getAllMatches() {
        return matchService.getAllMatches();
    }

    // Nadcházející zápas
    @GetMapping("/next")
    public MatchDTO getNextMatch() {
        return matchService.getNextMatch();
    }

    // Všechny nadcházející zápasy
    @GetMapping("/upcoming")
    public List<MatchDTO> getUpcomingMatches() {
        return matchService.getUpcomingMatches();
    }

    // Nadcházející zápasy pro konkrétního hráče (pouze DTO)
    @GetMapping("/player/{playerId}/upcoming")
    public List<MatchDTO> getPlayerUpcomingMatches(@PathVariable Long playerId) {
        List<MatchEntity> entities = matchService.getUpcomingMatchesForPlayer(playerId);
        return entities.stream()
                .map(matchMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Už uskutečněné zápasy
    @GetMapping("/past")
    public List<MatchDTO> getPastMatches() {
        return matchService.getPastMatches();
    }

    // Vytvoření zápasu
    @PostMapping
    public MatchDTO createMatch(@Valid @RequestBody MatchDTO dto) {
        return matchService.createMatch(dto);
    }

    // Získání zápasu podle ID
    @GetMapping("/{id}")
    public MatchDTO getMatch(@PathVariable Long id) {
        return matchService.getMatchById(id);
    }

    // Editace zápasu
    @PutMapping("/{id}")
    public MatchDTO updateMatch(@PathVariable Long id, @Valid @RequestBody MatchDTO dto) {
        return matchService.updateMatch(id, dto);
    }

    // Smazání zápasu
    @DeleteMapping("/{id}")
    public void deleteMatch(@PathVariable Long id) {
        matchService.deleteMatch(id);
    }

    // Dostupné zápasy pro hráče
    @GetMapping("/available-for-player/{playerId}")
    public List<MatchDTO> getAvailableMatchesForPlayer(@PathVariable Long playerId) {
        return matchService.getAvailableMatchesForPlayer(playerId)
                .stream()
                .map(matchMapper::toDTO)
                .collect(Collectors.toList());
    }

}
