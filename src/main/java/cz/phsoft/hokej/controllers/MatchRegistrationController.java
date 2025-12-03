package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.data.entities.MatchRegistrationEntity;
import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.JerseyColor;
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
    public MatchRegistrationEntity register(@RequestParam Long matchId, @RequestParam Long playerId,
                                            @RequestParam (required = false) JerseyColor jerseyColor,
                                            @RequestParam (required = false) String adminNote) {
        return service.registerPlayer(matchId, playerId, jerseyColor,adminNote);
    }

    @PostMapping("/unregister")
    public MatchRegistrationEntity unregister(@RequestParam Long matchId, @RequestParam Long playerId,  @RequestParam String reason,
                                              @RequestParam(required = false) String note) {
        return service.unregisterPlayer(matchId, playerId, note, reason);
    }

    @PostMapping("/excuse")
    public MatchRegistrationEntity excuse(@RequestParam Long matchId, @RequestParam Long playerId,
                                          @RequestParam String reason,
                                          @RequestParam(required = false) String note) {
        return service.excusePlayer(matchId, playerId, note, reason);
    }
    @GetMapping("/all")
    public List<MatchRegistrationEntity> getAllRegistrations() {
        return service.getAllRegistrations();
    }

    @GetMapping("/for-player")
    public List<MatchRegistrationEntity> forPlayer(@RequestParam Long playerId) {
        return service.getRegistrationsForPlayer(playerId);
    }



    @GetMapping("/for-match")
    public List<MatchRegistrationEntity> forMatch(@RequestParam Long matchId) {
        return service.getRegistrationsForMatch(matchId);
    }

    @GetMapping("/no-response/{matchId}")
    public List<PlayerEntity> getNoResponse(@PathVariable Long matchId) {
        return service.getNoResponsePlayers(matchId);
    }

   }
