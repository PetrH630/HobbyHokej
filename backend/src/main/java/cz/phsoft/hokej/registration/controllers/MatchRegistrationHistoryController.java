package cz.phsoft.hokej.registration.controllers;

import cz.phsoft.hokej.registration.dto.MatchRegistrationHistoryDTO;
import cz.phsoft.hokej.registration.services.MatchRegistrationHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller, který se používá pro práci s historií registrací
 * hráčů k zápasům.
 *
 * Controller poskytuje uživatelský pohled na historii registrace
 * aktuálního hráče a administrativní audit historie registrací
 * konkrétního hráče. Controller je read-only a slouží pouze ke čtení
 * z tabulky historie registrací.
 *
 * Veškerá business logika se předává do
 * {@link MatchRegistrationHistoryService}.
 */
@RestController
@RequestMapping("/api/registrations/history")
public class MatchRegistrationHistoryController {

    private final MatchRegistrationHistoryService historyService;

    public MatchRegistrationHistoryController(MatchRegistrationHistoryService historyService) {
        this.historyService = historyService;
    }

    // Uživatelský přístup – aktuální hráč

    /**
     * Vrací historii všech změn registrace aktuálně přihlášeného
     * hráče pro daný zápas.
     *
     * Metoda se používá například pro zobrazení historie registrace
     * na detailu zápasu v uživatelském rozhraní.
     *
     * @param matchId ID zápasu
     * @return seznam záznamů historie seřazených od nejnovějšího
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

    /**
     * Vrací historii všech změn registrace konkrétního hráče pro daný zápas.
     *
     * Metoda se používá pro administrativní audit, analýzu změn registrací
     * a řešení případných sporů.
     *
     * @param matchId  ID zápasu
     * @param playerId ID hráče
     * @return seznam záznamů historie seřazených od nejnovějšího
     */
    @GetMapping("/admin/matches/{matchId}/players/{playerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<MatchRegistrationHistoryDTO>> getPlayerHistoryForMatch(
            @PathVariable Long matchId,
            @PathVariable Long playerId
    ) {
        List<MatchRegistrationHistoryDTO> history =
                historyService.getHistoryForPlayerAndMatch(matchId, playerId);

        return ResponseEntity.ok(history);
    }
}
