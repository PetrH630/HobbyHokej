package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.requests.MatchRegistrationRequest;
import cz.phsoft.hokej.models.services.MatchRegistrationService;
import cz.phsoft.hokej.security.PlayerSecurity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin(origins = "*")
public class MatchRegistrationController {

    private final MatchRegistrationService service;
    private final PlayerSecurity playerSecurity;

    public MatchRegistrationController(MatchRegistrationService service,
                                       PlayerSecurity playerSecurity) {
        this.service = service;
        this.playerSecurity = playerSecurity;
    }

    // -----------------------------------------------------
    // 🔥 JEDINÝ UNIVERZÁLNÍ ENDPOINT PRO REGISTRACE
    // -----------------------------------------------------
    @PostMapping("/upsert")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or @playerSecurity.isOwner(authentication, #request.playerId)")
    public MatchRegistrationDTO upsert(@RequestBody MatchRegistrationRequest request) {
        return service.upsertRegistration(
                request.getMatchId(),
                request.getPlayerId(),
                request.getJerseyColor(),
                request.getAdminNote(),
                request.getExcuseReason(),
                request.getExcuseNote(),
                request.isUnregister()
        );
    }

    // -----------------------------------------------------
    // GET ENDPOINTY
    // -----------------------------------------------------

    @GetMapping("/all")
    public List<MatchRegistrationDTO> getAllRegistrations() {
        return service.getAllRegistrations();
    }

    @GetMapping("/for-player/{playerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or @playerSecurity.isOwner(authentication, #playerId)")
    public List<MatchRegistrationDTO> forPlayer(@PathVariable Long playerId) {
        return service.getRegistrationsForPlayer(playerId);
    }

    @GetMapping("/for-match/{matchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> forMatch(@PathVariable Long matchId) {
        return service.getRegistrationsForMatch(matchId);
    }

    @GetMapping("/no-response/{matchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<PlayerDTO> getNoResponse(@PathVariable Long matchId) {
        return service.getNoResponsePlayers(matchId);
    }
}
