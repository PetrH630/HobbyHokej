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

    // vytvoření registrace hráče - id hráče k id zápasu
    @PostMapping("/register")
    public MatchRegistrationEntity register(@RequestParam Long matchId, @RequestParam Long playerId,
                                            @RequestParam (required = false) JerseyColor jerseyColor,
                                            @RequestParam (required = false) String adminNote) {
        return service.registerPlayer(matchId, playerId, jerseyColor,adminNote);
    }

    // zrušení registrace hráče k zápasu - změna statutu na unregistered
    @PostMapping("/unregister")
    public MatchRegistrationEntity unregister(@RequestParam Long matchId, @RequestParam Long playerId,  @RequestParam String reason,
                                              @RequestParam(required = false) String note) {
        return service.unregisterPlayer(matchId, playerId, note, reason);
    }
    // omluvení hráče ze zápasu - jen pokud ještě neměl registraci
    @PostMapping("/excuse")
    public MatchRegistrationEntity excuse(@RequestParam Long matchId, @RequestParam Long playerId,
                                          @RequestParam String reason,
                                          @RequestParam(required = false) String note) {
        return service.excusePlayer(matchId, playerId, note, reason);
    }
    // všechny registrace
    @GetMapping("/all")
    public List<MatchRegistrationEntity> getAllRegistrations() {
        return service.getAllRegistrations();
    }

    // všechny registace k hráči dle id hráče
    @GetMapping("/for-player/{playerId}")
    public List<MatchRegistrationEntity> forPlayer(@PathVariable Long playerId) {
        return service.getRegistrationsForPlayer(playerId);
    }

    // všechny registrace k zápasu dle id zápasu
    @GetMapping("/for-match/{matchId}")
    public List<MatchRegistrationEntity> forMatch(@PathVariable Long matchId) {
        return service.getRegistrationsForMatch(matchId);
    }

    // všichni hráči co se ani neregistrovali, neodhlásili, neomluvili - bez reakce
    @GetMapping("/no-response/{matchId}")
    public List<PlayerEntity> getNoResponse(@PathVariable Long matchId) {
        return service.getNoResponsePlayers(matchId);
    }

   }
