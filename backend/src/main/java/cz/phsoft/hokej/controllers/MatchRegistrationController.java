package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.exceptions.CurrentPlayerNotSelectedException;
import cz.phsoft.hokej.models.dto.MatchRegistrationDTO;
import cz.phsoft.hokej.models.dto.PlayerDTO;
import cz.phsoft.hokej.models.dto.requests.MatchRegistrationRequest;
import cz.phsoft.hokej.models.services.CurrentPlayerService;
import cz.phsoft.hokej.models.services.MatchRegistrationService;
import cz.phsoft.hokej.models.services.MatchService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller pro správu registrací hráčů na zápasy.
 *
 * Zajišťuje:
 * <ul>
 *     <li>administraci registrací (ADMIN / MANAGER),</li>
 *     <li>správu registrací aktuálního hráče (/me).</li>
 * </ul>
 *
 * Business logika je delegována do {@link MatchRegistrationService}
 * a částečně do {@link MatchService}.
 */
@RestController
@RequestMapping("/api/registrations")
public class MatchRegistrationController {

    private final MatchRegistrationService matchRegistrationService;
    private final CurrentPlayerService currentPlayerService;
    private final MatchService matchService;

    public MatchRegistrationController(MatchRegistrationService matchRegistrationService,
                                       CurrentPlayerService currentPlayerService,
                                       MatchService matchService) {
        this.matchRegistrationService = matchRegistrationService;
        this.currentPlayerService = currentPlayerService;
        this.matchService = matchService;
    }

    // =========================================================
    //  ADMIN / MANAGER – GLOBÁLNÍ SPRÁVA REGISTRACÍ
    // =========================================================

    /**
     * Vrátí všechny registrace všech hráčů na všechny zápasy.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> getAllRegistrations() {
        return matchRegistrationService.getAllRegistrations();
    }

    /**
     * Vrátí všechny registrace hráčů pro konkrétní zápas.
     *
     * @param matchId ID zápasu
     */
    @GetMapping("/match/{matchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> getRegistrationsForMatch(@PathVariable Long matchId) {
        return matchRegistrationService.getRegistrationsForMatch(matchId);
    }

    /**
     * Vrátí všechny registrace konkrétního hráče napříč zápasy.
     *
     * @param playerId ID hráče
     */
    @GetMapping("/player/{playerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> getRegistrationsForPlayer(@PathVariable Long playerId) {
        return matchRegistrationService.getRegistrationsForPlayer(playerId);
    }

    /**
     * Vrátí seznam hráčů, kteří na pozvánku k danému zápasu
     * zatím vůbec nereagovali (žádná registrace ani omluva).
     *
     * @param matchId ID zápasu
     */
    @GetMapping("/match/{matchId}/no-response")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<PlayerDTO> getNoResponsePlayers(@PathVariable Long matchId) {
        return matchRegistrationService.getNoResponsePlayers(matchId);
    }

    /**
     * Univerzální endpoint pro vytvoření nebo aktualizaci registrace
     * za konkrétního hráče (ADMIN / MANAGER).
     *
     * @param playerId ID hráče, za kterého se změna provádí
     * @param request  požadavek na změnu registrace
     */
    @PostMapping("/upsert/{playerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchRegistrationDTO upsertForPlayer(
            @PathVariable Long playerId,
            @Valid @RequestBody MatchRegistrationRequest request
    ) {
        return matchRegistrationService.upsertRegistration(playerId, request);
    }

    /**
     * Označí hráče v konkrétním zápase jako neomluveně nepřítomného.
     *
     * @param matchId   ID zápasu
     * @param playerId  ID hráče
     * @param adminNote volitelná poznámka administrátora
     */
    @PatchMapping("/match/{matchId}/players/{playerId}/no-excused")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchRegistrationDTO markNoExcused(
            @PathVariable Long matchId,
            @PathVariable Long playerId,
            @RequestParam(required = false) String adminNote
    ) {
        return matchRegistrationService.markNoExcused(matchId, playerId, adminNote);
    }

    // =========================================================
    //  USER – REGISTRACE AKTUÁLNÍHO HRÁČE (/me)
    // =========================================================

    /**
     * Univerzální endpoint pro správu registrace aktuálního hráče na zápas.
     *
     * Pracuje automaticky s aktuálně zvoleným hráčem.
     */
    @PostMapping("/me/upsert")
    @PreAuthorize("isAuthenticated()")
    public MatchRegistrationDTO upsertForCurrentPlayer(
            @Valid @RequestBody MatchRegistrationRequest request
    ) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();

        if (currentPlayerId == null) {
            throw new CurrentPlayerNotSelectedException();
        }

        return matchRegistrationService.upsertRegistration(currentPlayerId, request);
    }

    /**
     * Vrátí všechny registrace aktuálně zvoleného hráče.
     */
    @GetMapping("/me/for-current-player")
    @PreAuthorize("isAuthenticated()")
    public List<MatchRegistrationDTO> getRegistrationsForCurrentPlayer() {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return matchRegistrationService.getRegistrationsForPlayer(currentPlayerId);
    }
}
