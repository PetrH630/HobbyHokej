package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.data.enums.ExcuseReason;
import cz.phsoft.hokej.data.enums.PlayerPosition;
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
 * a správu registrací pro aktuálního hráče pod endpointy s prefixem /me.
 * Umožňuje registraci, odhlášení, evidenci omluv a neomluvené neúčasti
 * a také přehled registrací napříč zápasy.
 *
 * Veškerá business logika se deleguje do {@link MatchRegistrationService},
 * informace o aktuálním hráči se získávají z {@link CurrentPlayerService}
 * a související doménová logika může být částečně řešena také v {@link MatchService}.
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
     * Endpoint je dostupný pro role ADMIN a MANAGER a slouží
     * k přehledové správě registrací napříč celým systémem.
     *
     * @return seznam všech registrací jako {@link MatchRegistrationDTO}
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> getAllRegistrations() {
        return matchRegistrationService.getAllRegistrations();
    }

    /**
     * Vrací všechny registrace hráčů pro konkrétní zápas.
     *
     * Endpoint se používá například při kontrole obsazenosti zápasu
     * nebo při ruční úpravě registrací ze strany administrátora či manažera.
     *
     * @param matchId ID zápasu
     * @return seznam registrací pro daný zápas jako {@link MatchRegistrationDTO}
     */
    @GetMapping("/match/{matchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> getRegistrationsForMatch(@PathVariable Long matchId) {
        return matchRegistrationService.getRegistrationsForMatch(matchId);
    }

    /**
     * Vrací všechny registrace konkrétního hráče napříč zápasy.
     *
     * Endpoint se používá pro přehled účasti hráče v jednotlivých zápasech,
     * typicky v administrativním rozhraní.
     *
     * @param playerId ID hráče
     * @return seznam registrací daného hráče jako {@link MatchRegistrationDTO}
     */
    @GetMapping("/player/{playerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> getRegistrationsForPlayer(@PathVariable Long playerId) {
        return matchRegistrationService.getRegistrationsForPlayer(playerId);
    }

    /**
     * Vrací seznam hráčů, kteří na pozvánku k danému zápasu zatím nereagovali.
     *
     * Informace se používá například pro přehled hráčů, kteří se ještě
     * nepřihlásili ani neomluvili, a může sloužit jako podklad pro
     * následnou komunikaci ze strany manažera.
     *
     * @param matchId ID zápasu
     * @return seznam hráčů bez reakce jako {@link PlayerDTO}
     */
    @GetMapping("/match/{matchId}/no-response")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<PlayerDTO> getNoResponsePlayers(@PathVariable Long matchId) {
        return matchRegistrationService.getNoResponsePlayers(matchId);
    }

    /**
     * Vytváří nebo aktualizuje registraci za konkrétního hráče.
     *
     * Endpoint se používá administrátorem nebo manažerem v situacích,
     * kdy je potřeba hráče ručně zaregistrovat, odhlásit nebo změnit
     * typ jeho účasti. Vstupní požadavek určuje cílový stav registrace.
     *
     * @param playerId ID hráče, za kterého se operace provádí
     * @param request  požadavek na změnu registrace
     * @return {@link MatchRegistrationDTO} s výsledným stavem registrace
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
     * Slouží k zaznamenání neomluvené absence hráče. Pro záznam může být
     * doplněna interní poznámka administrátora nebo manažera.
     *
     * @param matchId   ID zápasu
     * @param playerId  ID hráče
     * @param adminNote volitelná interní poznámka k záznamu
     * @return {@link MatchRegistrationDTO} s aktualizovaným stavem registrace
     */
    @PatchMapping("/match/{matchId}/players/{playerId}/no-excused")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchRegistrationDTO markNoExcused(
            @PathVariable Long matchId,
            @PathVariable Long playerId,
            @RequestParam(name = "adminNote", required = false) String adminNote
    ) {
        return matchRegistrationService.markNoExcused(matchId, playerId, adminNote);
    }

    /**
     * Ruší označení neomluvené absence hráče v konkrétním zápase a nastavuje omluvu.
     *
     * Slouží pro opravu dříve uložené neomluvené absence, například pokud
     * byla absence vyhodnocena dodatečně jako omluvená. Důvod omluvy a poznámka
     * se ukládají spolu s registrací.
     *
     * @param matchId      ID zápasu
     * @param playerId     ID hráče
     * @param excuseReason důvod omluvy
     * @param excuseNote   poznámka k omluvě
     * @return {@link MatchRegistrationDTO} s aktualizovaným stavem registrace
     */
    @PatchMapping("/match/{matchId}/players/{playerId}/cancel-no-excused")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchRegistrationDTO cancelNoExcused(
            @PathVariable Long matchId,
            @PathVariable Long playerId,
            @RequestParam ExcuseReason excuseReason,
            @RequestParam String excuseNote
    ) {
        return matchRegistrationService.cancelNoExcused(matchId, playerId, excuseReason, excuseNote);
    }

    /**
     * Mění team u přihlášeného hráče ke konkrétní registraci na opačný team.
     *
     *Aktuální hráč se získává
     *      * z {@link CurrentPlayerService}.
     * @param matchId   ID zápasu     *
     * @return {@link MatchRegistrationDTO} s aktualizovaným stavem registrace
     */
    @PatchMapping("/me/{matchId}/change-team")
    @PreAuthorize("isAuthenticated()")
    public MatchRegistrationDTO changeRegistrationTeam(
            @PathVariable Long matchId) {

        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return matchRegistrationService.changeRegistrationTeam(currentPlayerId, matchId);
    }

    /**
     * Mění team u hráče dle jeho id na opačný team.
     *
     * @param playerId  ID hráče
     * @param matchId   ID zápasu     *
     * @return {@link MatchRegistrationDTO} s aktualizovaným stavem registrace
     */
    @PatchMapping("/{playerId}/{matchId}/change-team")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchRegistrationDTO changeRegistrationTeamAdmin(
            @PathVariable Long playerId,
            @PathVariable Long matchId) {

        return matchRegistrationService.changeRegistrationTeam(playerId, matchId);
    }

    /**
     * Mění pozici u hráče dle jeho id v konkrétním zápase.
     *
     * @param playerId  ID hráče
     * @param matchId   ID zápasu
     * @param positionInMatch   pozice hráče
     * @return mění pozici hráče v aktuálním zápasu
     */
    @PatchMapping("/{matchId}/players/{playerId}/position")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchRegistrationDTO changePosition(
            @PathVariable Long matchId,
            @PathVariable Long playerId,
            @RequestParam("position") PlayerPosition positionInMatch
    ) {
        return matchRegistrationService.changeRegistrationPosition(playerId, matchId, positionInMatch);
    }

    // Uživatelská správa registrací pro aktuálního hráče

    /**
     * Spravuje registraci aktuálního hráče na zápas.
     *
     * Podle obsahu {@link MatchRegistrationRequest} se provádí registrace,
     * odhlášení, omluva nebo nastavení náhradníka. Aktuální hráč se získává
     * z {@link CurrentPlayerService}. V případě, že aktuální hráč není zvolen,
     * vyhazuje se {@link CurrentPlayerNotSelectedException}.
     *
     * @param request požadavek na změnu registrace
     * @return {@link MatchRegistrationDTO} s výsledným stavem registrace
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
     * Endpoint se používá pro uživatelské zobrazení historie a stavu
     * registrací hráče napříč zápasy. Identita hráče se získává z
     * {@link CurrentPlayerService}.
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
