package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.exceptions.CurrentPlayerNotSelectedException;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.requests.MatchRegistrationRequest;
import cz.phsoft.hokej.models.services.CurrentPlayerService;
import cz.phsoft.hokej.models.services.MatchRegistrationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller pro správu registrací hráče na zápasy
 * v kontextu přihlášeného uživatele.
 * <p>
 * Controller pracuje výhradně s „aktuálním hráčem“ vybraným
 * přihlášeným uživatelem a umožňuje:
 * <ul>
 *     <li>vytvoření nebo aktualizaci registrace na zápas,</li>
 *     <li>získání přehledu registrací aktuálního hráče.</li>
 * </ul>
 *
 * Veškerá business logika je delegována do {@link MatchRegistrationService}.
 */
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

    /**
     * Univerzální endpoint pro správu registrace aktuálního hráče na zápas.
     * <p>
     * Endpoint automaticky pracuje s aktuálně zvoleným hráčem
     * přihlášeného uživatele.
     * <p>
     * Konkrétní chování (registrace, odhlášení, omluva) je řízeno
     * obsahem {@link MatchRegistrationRequest}.
     *
     * @param request požadavek na změnu registrace
     * @return aktuální stav registrace
     * @throws CurrentPlayerNotSelectedException pokud není zvolen aktuální hráč
     */
    @PostMapping("/me/upsert")
    @PreAuthorize("isAuthenticated()")
    public MatchRegistrationDTO upsert(@Valid @RequestBody MatchRegistrationRequest request) {

        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();

        if (currentPlayerId == null) {
            throw new CurrentPlayerNotSelectedException();
        }

        return matchRegistrationService.upsertRegistration(currentPlayerId, request);
    }

    /**
     * Vrátí všechny registrace aktuálně zvoleného hráče.
     *
     * @return seznam registrací aktuálního hráče
     * @throws CurrentPlayerNotSelectedException pokud není zvolen aktuální hráč
     */
    @GetMapping("/me/for-current-player")
    @PreAuthorize("isAuthenticated()")
    public List<MatchRegistrationDTO> forCurrentPlayer() {

        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();

        return matchRegistrationService.getRegistrationsForPlayer(currentPlayerId);
    }
}
