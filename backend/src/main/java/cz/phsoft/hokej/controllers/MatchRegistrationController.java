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
 * REST controller, který se používá pro správu registrací hráčů na zápasy.
 *
 * Zajišťuje administrativní správu registrací pro role ADMIN a MANAGER
 * a správu registrací pro aktuálního hráče pod endpointy /me.
 *
 * Veškerá business logika se předává do {@link MatchRegistrationService}
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

    // ADMIN / MANAGER – globální správa registrací

    /**
     * Vrací seznam všech registrací na všechny zápasy.
     *
     * Endpoint je dostupný pro role ADMIN a MANAGER.
     *
     * @return seznam {@link MatchRegistrationDTO}
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> getAllRegistrations() {
        return matchRegistrationService.getAllRegistrations();
    }

    /**
     * Vrací všechny registrace hráčů pro konkrétní zápas.
     *
     * @param matchId ID zápasu
     * @return seznam registrací pro daný zápas
     */
    @GetMapping("/match/{matchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> getRegistrationsForMatch(@PathVariable Long matchId) {
        return matchRegistrationService.getRegistrationsForMatch(matchId);
    }

    /**
     * Vrací všechny registrace konkrétního hráče napříč zápasy.
     *
     * @param playerId ID hráče
     * @return seznam registrací daného hráče
     */
    @GetMapping("/player/{playerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> getRegistrationsForPlayer(@PathVariable Long playerId) {
        return matchRegistrationService.getRegistrationsForPlayer(playerId);
    }

    /**
     * Vrací seznam hráčů, kteří na pozvánku k danému zápasu zatím nereagovali.
     *
     * Tato informace se používá například pro přehled hráčů, kteří se
     * ještě nepřihlásili ani neomluvili.
     *
     * @param matchId ID zápasu
     * @return seznam hráčů bez reakce
     */
    @GetMapping("/match/{matchId}/no-response")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<PlayerDTO> getNoResponsePlayers(@PathVariable Long matchId) {
        return matchRegistrationService.getNoResponsePlayers(matchId);
    }

    /**
     * Vytváří nebo aktualizuje registraci za konkrétního hráče.
     *
     * Endpoint se používá administrátorem nebo manažerem pro scénáře,
     * kdy je potřeba registrovat nebo přehlásit hráče ručně.
     *
     * @param playerId ID hráče, za kterého se operace provádí
     * @param request  požadavek na změnu registrace
     * @return DTO {@link MatchRegistrationDTO} s výsledným stavem registrace
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
     * Označuje hráče v konkrétním zápase jako neomluveně nepřítomného.
     *
     * Pro záznam může být doplněna poznámka administrátora.
     *
     * @param matchId   ID zápasu
     * @param playerId  ID hráče
     * @param adminNote volitelná interní poznámka
     * @return DTO {@link MatchRegistrationDTO} s aktualizovaným stavem
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

    // Uživatelská správa registrací pro aktuálního hráče

    /**
     * Spravuje registraci aktuálního hráče na zápas.
     *
     * Podle obsahu {@link MatchRegistrationRequest} se provádí registrace,
     * odhlášení, omluva nebo nastavení náhradníka. Aktuální hráč se získává
     * z {@link CurrentPlayerService}.
     *
     * @param request požadavek na změnu registrace
     * @return DTO {@link MatchRegistrationDTO} s výsledným stavem registrace
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
     * Vrací všechny registrace aktuálně zvoleného hráče.
     *
     * @return seznam {@link MatchRegistrationDTO} pro aktuálního hráče
     */
    @GetMapping("/me/for-current-player")
    @PreAuthorize("isAuthenticated()")
    public List<MatchRegistrationDTO> getRegistrationsForCurrentPlayer() {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return matchRegistrationService.getRegistrationsForPlayer(currentPlayerId);
    }
}
