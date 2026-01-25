package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.exceptions.CurrentPlayerNotSelectedException;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.requests.MatchRegistrationRequest;
import cz.phsoft.hokej.models.services.CurrentPlayerService;
import cz.phsoft.hokej.models.services.MatchRegistrationService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@CrossOrigin(origins = "*")
public class MatchRegistrationController {

    private final MatchRegistrationService matchRegistrationService;
    private final CurrentPlayerService currentPlayerService;

    public MatchRegistrationController(MatchRegistrationService matchRegistrationService,
                                       CurrentPlayerService currentPlayerService) {
        this.matchRegistrationService = matchRegistrationService;
        this.currentPlayerService = currentPlayerService;
    }


    // UNIVERZÁLNÍ ENDPOINT PRO REGISTRACE


    @PostMapping("/me/upsert")
    @PreAuthorize("isAuthenticated()")
    public MatchRegistrationDTO upsert(@Valid @RequestBody MatchRegistrationRequest request) {
        // automaticky bere vybraného hráče
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();

        //TODO - Vlastní vyjímiku
        if (currentPlayerId == null) {
            throw new CurrentPlayerNotSelectedException();
        }

        return matchRegistrationService.upsertRegistration(currentPlayerId,request);
    }

    // registrace na zápasy přihlášeného hráče
    @GetMapping("/me/for-current-player")
    @PreAuthorize("isAuthenticated()")
    public List<MatchRegistrationDTO> forCurrentPlayer() {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return matchRegistrationService.getRegistrationsForPlayer(currentPlayerId);
    }


}
