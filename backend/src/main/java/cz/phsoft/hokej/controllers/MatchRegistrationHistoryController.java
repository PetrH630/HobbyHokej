package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.MatchRegistrationHistoryDTO;
import cz.phsoft.hokej.models.services.CurrentPlayerService;
import cz.phsoft.hokej.models.services.MatchRegistrationHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * REST controller pro práci s historií registrací hráčů k zápasům.
 *
 * <p>
 * Zajišťuje:
 * <ul>
 *     <li>zobrazení historie registrací přihlášeného hráče (/user),</li>
 *     <li>administrativní přístup k historii konkrétního hráče (/admin).</li>
 * </ul>
 * </p>
 *
 * <p>
 * Controller je read-only – neprovádí žádné zápisy do tabulky
 * {@code match_registration_history}, pouze ji čte.
 * </p>
 */
@RestController
@RequestMapping("/api/registrations/history")
public class MatchRegistrationHistoryController {

    private final MatchRegistrationHistoryService historyService;


    public MatchRegistrationHistoryController(MatchRegistrationHistoryService historyService) {
        this.historyService = historyService;

    }

    // ==========================
    // USER – aktuálně přihlášený hráč
    // ==========================

    /**
     * Vrátí historii všech změn registrace
     * aktuálně přihlášeného hráče pro daný zápas.
     *
     * <p>
     * Typické použití:
     * <ul>
     *     <li>detail zápasu – záložka „Historie mojí registrace“.</li>
     * </ul>
     * </p>
     *
     * <p><b>URL:</b> GET /api/user/matches/{matchId}/registrations/history</p>
     *
     * @param matchId ID zápasu
     * @return seznam historických záznamů seřazených od nejnovějšího
     */
    @GetMapping("/me/matches/{matchId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MatchRegistrationHistoryDTO>> getMyHistoryForMatch(
            @PathVariable Long matchId
    ) {
        List<MatchRegistrationHistoryDTO> history =
                historyService.getHistoryForCurrentPlayerAndMatch(matchId);

        return ResponseEntity.ok(history);
    }

    // ==========================
    // ADMIN – audit konkrétního hráče
    // ==========================

    /**
     * Vrátí historii všech změn registrace konkrétního hráče
     * pro daný zápas.
     *
     * <p>
     * Typické použití:
     * <ul>
     *     <li>administrativní audit registrací hráče,</li>
     *     <li>řešení sporů (kdo a kdy změnil status).</li>
     * </ul>
     * </p>
     *
     * <p><b>URL:</b>
     * GET /api/admin/matches/{matchId}/players/{playerId}/registrations/history
     * </p>
     *
     * @param matchId  ID zápasu
     * @param playerId ID hráče
     * @return seznam historických záznamů seřazených od nejnovějšího
     */
    @GetMapping("/admin/matches/{matchId}/players/{playerId}")
    @PreAuthorize("hasRole('ADMIN')") // případně hasAnyRole('ADMIN','MANAGER')
    public ResponseEntity<List<MatchRegistrationHistoryDTO>> getPlayerHistoryForMatch(
            @PathVariable Long matchId,
            @PathVariable Long playerId
    ) {
        List<MatchRegistrationHistoryDTO> history =
                historyService.getHistoryForPlayerAndMatch(matchId, playerId);

        return ResponseEntity.ok(history);
    }
}



