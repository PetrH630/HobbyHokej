package cz.phsoft.hokej.controllers;

import cz.phsoft.hokej.models.dto.MatchDTO;
import cz.phsoft.hokej.models.dto.MatchDetailDTO;
import cz.phsoft.hokej.models.dto.MatchOverviewDTO;
import cz.phsoft.hokej.models.services.CurrentPlayerService;
import cz.phsoft.hokej.models.services.MatchService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller pro práci se zápasy z pohledu přihlášeného uživatele / hráče.
 * <p>
 * Controller poskytuje:
 * <ul>
 *     <li>detail zápasu,</li>
 *     <li>seznam nadcházejících zápasů pro aktuálního hráče,</li>
 *     <li>přehled nadcházejících zápasů (overview),</li>
 *     <li>seznam již odehraných zápasů aktuálního hráče.</li>
 * </ul>
 *
 * Controller pracuje vždy v kontextu „aktuálního hráče“,
 * který je spravován pomocí {@link CurrentPlayerService}.
 */
@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "*")
public class MatchController {

    private final MatchService matchService;
    private final CurrentPlayerService currentPlayerService;

    public MatchController(MatchService matchService,
                           CurrentPlayerService currentPlayerService) {
        this.matchService = matchService;
        this.currentPlayerService = currentPlayerService;
    }

    /**
     * Vrátí detail konkrétního zápasu.
     *
     * @param id ID zápasu
     * @return detail zápasu
     */
    @GetMapping("/matchDetail/{id}")
    @PreAuthorize("isAuthenticated()")
    public MatchDetailDTO getMatchDetail(@PathVariable Long id) {
        return matchService.getMatchDetail(id);
    }

    /**
     * Vrátí nejbližší nadcházející zápas.
     * <p>
     * Endpoint je zachován spíše pro interní nebo zpětnou kompatibilitu.
     * Pro práci v kontextu hráče se doporučuje používat endpointy
     * založené na „aktuálním hráči“.
     *
     * @return nejbližší nadcházející zápas
     */
    @GetMapping("/next")
    @PreAuthorize("isAuthenticated()")
    public MatchDTO getNextMatch() {
        return matchService.getNextMatch();
    }

    /**
     * Vrátí seznam nadcházejících zápasů pro aktuálně zvoleného hráče.
     * <p>
     * Vyžaduje, aby měl uživatel nastaveného aktuálního hráče.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return seznam nadcházejících zápasů
     */
    @GetMapping("/me/upcoming")
    @PreAuthorize("isAuthenticated()")
    public List<MatchDTO> getUpcomingMatchesForMe(Authentication authentication) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return matchService.getUpcomingMatchesForPlayer(currentPlayerId);
    }

    /**
     * Vrátí přehled (overview) nadcházejících zápasů pro aktuálního hráče.
     * <p>
     * Přehled obsahuje zjednodušená data určená pro seznamové zobrazení
     * na frontendu.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return seznam přehledů nadcházejících zápasů
     */
    @GetMapping("/me/upcoming-overview")
    @PreAuthorize("isAuthenticated()")
    public List<MatchOverviewDTO> getUpcomingMatchesOverviewForMe(Authentication authentication) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return matchService.getUpcomingMatchesOverviewForPlayer(currentPlayerId);
    }

    /**
     * Vrátí seznam všech již odehraných zápasů pro aktuálního hráče.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return seznam odehraných zápasů (overview)
     */
    @GetMapping("/me/all-passed")
    @PreAuthorize("isAuthenticated()")
    public List<MatchOverviewDTO> getAllMatchesForPlayer(Authentication authentication) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return matchService.getAllPassedMatchesForPlayer(currentPlayerId);
    }
}
