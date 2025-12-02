package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.models.services.MatchRegistrationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin(origins = "*")
public class MatchRegistrationController {

    private final MatchRegistrationService service;

    public MatchRegistrationController(MatchRegistrationService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public MatchRegistrationEntity register(@RequestParam Long matchId, @RequestParam Long playerId) {
        return service.registerPlayer(matchId, playerId);
    }

    @PostMapping("/unregister")
    public MatchRegistrationEntity unregister(@RequestParam Long matchId, @RequestParam Long playerId) {
        return service.unregisterPlayer(matchId, playerId);
    }

    @PostMapping("/excuse")
    public MatchRegistrationEntity excuse(@RequestParam Long matchId, @RequestParam Long playerId,
                                          @RequestParam String reason,
                                          @RequestParam(required = false) String note) {
        return service.excusePlayer(matchId, playerId, note, reason);
    }

    // --- Dotazy ---
    @GetMapping("/last-status")
    public MatchRegistrationEntity lastStatus(@RequestParam Long matchId, @RequestParam Long playerId) {
        return service.getLastStatus(matchId, playerId);
    }

    @GetMapping("/for-match")
    public List<MatchRegistrationEntity> forMatch(@RequestParam Long matchId) {
        return service.getRegistrationsForMatch(matchId);
    }

    @GetMapping("/no-response/{matchId}")
    public List<PlayerEntity> getNoResponse(@PathVariable Long matchId) {
        return service.getNoResponsePlayers(matchId);
    }

    @GetMapping("/all")
    public List<MatchRegistrationEntity> getAllRegistrations() {
        return service.getAllRegistrations();
    }

    @GetMapping("/for-player")
    public List<MatchRegistrationEntity> forPlayer(@RequestParam Long playerId) {
        return service.getRegistrationsForPlayer(playerId);
    }


}
