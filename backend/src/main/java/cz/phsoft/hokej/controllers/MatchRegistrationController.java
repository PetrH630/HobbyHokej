package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.requests.MatchRegistrationRequest;
import cz.phsoft.hokej.models.services.MatchRegistrationService;
import cz.phsoft.hokej.security.CurrentPlayerContext;
import cz.phsoft.hokej.security.PlayerSecurity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin(origins = "*")
public class MatchRegistrationController {

    private final MatchRegistrationService service;

    public MatchRegistrationController(MatchRegistrationService service) {
        this.service = service;

    }
    // -----------------------------------------------------
    // 🔥 JEDINÝ UNIVERZÁLNÍ ENDPOINT PRO REGISTRACE
    // -----------------------------------------------------
    @PostMapping("/upsert")
    @PreAuthorize("isAuthenticated()")
    public MatchRegistrationDTO upsert(@RequestBody MatchRegistrationRequest request) {
        PlayerEntity currentPlayer = CurrentPlayerContext.get();
        if (currentPlayer == null) {
            throw new RuntimeException("No current player selected");
        }
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> getAllRegistrations() {
        return service.getAllRegistrations();
    }

    @GetMapping("/for-current-player")
    @PreAuthorize("isAuthenticated()")
    public List<MatchRegistrationDTO> forCurrentPlayer() {
        PlayerEntity currentPlayer = CurrentPlayerContext.get();
        if (currentPlayer == null) {
            throw new RuntimeException("No current player selected");
        }
        return service.getRegistrationsForPlayer(currentPlayer.getId());
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
