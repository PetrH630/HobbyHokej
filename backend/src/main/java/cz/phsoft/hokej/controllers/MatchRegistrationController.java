package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.requests.MatchRegistrationRequest;
import cz.phsoft.hokej.models.services.MatchRegistrationService;
import cz.phsoft.hokej.security.CurrentPlayerService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin(origins = "*")
public class MatchRegistrationController {

    private final MatchRegistrationService service;
    private final CurrentPlayerService currentPlayerService;

    public MatchRegistrationController(MatchRegistrationService service,
                                       CurrentPlayerService currentPlayerService) {
        this.service = service;
        this.currentPlayerService = currentPlayerService;
    }

    // -----------------------------------------------------
    // 🔥 JEDINÝ UNIVERZÁLNÍ ENDPOINT PRO REGISTRACE
    // -----------------------------------------------------
    @PostMapping("/me/upsert")
    @PreAuthorize("isAuthenticated()")
    public MatchRegistrationDTO upsert(@RequestBody MatchRegistrationRequest request) {
        // automaticky bere vybraného hráče
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();

        return service.upsertRegistration(
                request.getMatchId(),
                currentPlayerId, // vždy aktuální hráč
                request.getTeam(),
                request.getAdminNote(),
                request.getExcuseReason(),
                request.getExcuseNote(),
                request.isUnregister()
        );
    }

    // -----------------------------------------------------
    // GET ENDPOINTY
    // -----------------------------------------------------

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> getAllRegistrations() {
        return service.getAllRegistrations();
    }

    @GetMapping("/me/for-current-player")
    @PreAuthorize("isAuthenticated()")
    public List<MatchRegistrationDTO> forCurrentPlayer() {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return service.getRegistrationsForPlayer(currentPlayerId);
    }

    @GetMapping("/admin/for-match/{matchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> forMatch(@PathVariable Long matchId) {
        return service.getRegistrationsForMatch(matchId);
    }

    @GetMapping("/admin/for-player/{playerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> forPlayer(@PathVariable Long playerId) {
        return service.getRegistrationsForPlayer(playerId);
    }
    @GetMapping("/admin/no-response/{matchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<PlayerDTO> getNoResponse(@PathVariable Long matchId) {
        return service.getNoResponsePlayers(matchId);
    }
}
