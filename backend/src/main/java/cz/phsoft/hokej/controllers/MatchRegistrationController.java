package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.requests.MatchRegistrationRequest;
import cz.phsoft.hokej.models.services.CurrentPlayerService;
import cz.phsoft.hokej.models.services.MatchRegistrationService;
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


    // UNIVERZÁLNÍ ENDPOINT PRO REGISTRACE


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

    // registrace na zápasy přihlášeného hráče
    @GetMapping("/me/for-current-player")
    @PreAuthorize("isAuthenticated()")
    public List<MatchRegistrationDTO> forCurrentPlayer() {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return service.getRegistrationsForPlayer(currentPlayerId);
    }


}
