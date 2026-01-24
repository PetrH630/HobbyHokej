package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.requests.MatchRegistrationRequest;
import cz.phsoft.hokej.models.services.MatchRegistrationService;
import cz.phsoft.hokej.models.services.MatchService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations/admin")
@CrossOrigin(origins = "*")
public class AdminMatchRegistrationController {
    private final MatchRegistrationService service;
    private final CurrentPlayerService currentPlayerService;
    private final MatchService matchService;

    public AdminMatchRegistrationController(MatchRegistrationService service,
                                       CurrentPlayerService currentPlayerService,
                                            MatchService matchService) {
        this.service = service;
        this.currentPlayerService = currentPlayerService;
        this.matchService = matchService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> getAllRegistrations() {
        return service.getAllRegistrations();
    }
    @GetMapping("/for-match/{matchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> forMatch(@PathVariable Long matchId) {
        return service.getRegistrationsForMatch(matchId);
    }

    @GetMapping("/for-player/{playerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> forPlayer(@PathVariable Long playerId) {
        return service.getRegistrationsForPlayer(playerId);
    }
    @GetMapping("/no-response/{matchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<PlayerDTO> getNoResponse(@PathVariable Long matchId) {
        return service.getNoResponsePlayers(matchId);
    }

    // UNIVERZÁLNÍ ENDPOINT PRO REGISTRACE - za hráče

    @PostMapping("/upsert/{playerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchRegistrationDTO upsert(@PathVariable Long playerId, @RequestBody MatchRegistrationRequest request) {

        return service.upsertRegistration(
                request.getMatchId(),
                request.getPlayerId(), // vybraný hráč
                request.getTeam(),
                request.getAdminNote(),
                request.getExcuseReason(),
                request.getExcuseNote(),
                request.isUnregister()
        );
    }

    @PostMapping("/matches/{matchId}/players/{playerId}/no-excused")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchRegistrationDTO markNoExcused(
            @PathVariable Long matchId,
            @PathVariable Long playerId,
            @RequestBody(required = false) String adminNote
    ) {
        return matchService.markNoExcused(matchId, playerId, adminNote);
    }
}

