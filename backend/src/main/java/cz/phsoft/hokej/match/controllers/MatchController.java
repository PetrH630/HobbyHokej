package cz.phsoft.hokej.match.controllers;

import cz.phsoft.hokej.match.dto.*;
import cz.phsoft.hokej.match.enums.MatchCancelReason;
import cz.phsoft.hokej.match.services.MatchAutoLineupService;
import cz.phsoft.hokej.match.services.MatchHistoryService;
import cz.phsoft.hokej.match.services.MatchPositionService;
import cz.phsoft.hokej.match.services.MatchService;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.player.services.CurrentPlayerService;
import cz.phsoft.hokej.shared.dto.SuccessResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller pro správu zápasů.
 *
 * Controller zajišťuje administrativní operace nad zápasy pro role ADMIN a MANAGER,
 * včetně vytváření, aktualizace, mazání, zrušení a obnovení zápasů.
 *
 * Současně poskytuje endpointy pro práci v kontextu aktuálního hráče,
 * například přehled nadcházejících a odehraných zápasů nebo detail zápasu
 * s informacemi o registracích.
 *
 * Veškerá business logika je delegována do service vrstvy, zejména do
 * MatchService, MatchHistoryService, MatchPositionService,
 * MatchAutoLineupService a CurrentPlayerService.
 */
@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;
    private final CurrentPlayerService currentPlayerService;
    private final MatchHistoryService matchHistoryService;
    private final MatchPositionService matchPositionService;
    private final MatchAutoLineupService matchAutoLineupService;

    /**
     * Vytváří controller pro správu zápasů.
     *
     * @param matchService služba pro práci se zápasy
     * @param currentPlayerService služba pro práci s aktuálním hráčem
     * @param matchHistoryService služba pro historii změn zápasů
     * @param matchPositionService služba pro práci s pozicemi v zápase
     * @param matchAutoLineupService služba pro automatické generování lajny
     */
    public MatchController(MatchService matchService,
                           CurrentPlayerService currentPlayerService,
                           MatchHistoryService matchHistoryService,
                           MatchPositionService matchPositionService,
                           MatchAutoLineupService matchAutoLineupService) {
        this.matchService = matchService;
        this.currentPlayerService = currentPlayerService;
        this.matchHistoryService = matchHistoryService;
        this.matchPositionService = matchPositionService;
        this.matchAutoLineupService = matchAutoLineupService;
    }

    // ADMIN / MANAGER – globální správa zápasů

    /**
     * Vrací seznam všech zápasů v systému.
     *
     * Endpoint je určen pro administrativní přehled zápasů
     * a je dostupný pro role ADMIN a MANAGER.
     *
     * @return seznam všech zápasů jako MatchDTO
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getAllMatches() {
        return matchService.getAllMatches();
    }

    /**
     * Vrací seznam všech nadcházejících zápasů.
     *
     * Endpoint je dostupný pro role ADMIN a MANAGER
     * a slouží k přehledu budoucích zápasů v systému.
     *
     * @return seznam nadcházejících zápasů jako MatchDTO
     */
    @GetMapping("/upcoming")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getUpcomingMatches() {
        return matchService.getUpcomingMatches();
    }

    /**
     * Vrací seznam všech již odehraných zápasů.
     *
     * Endpoint je dostupný pro role ADMIN a MANAGER
     * a používá se pro přehled historicky odehraných zápasů.
     *
     * @return seznam odehraných zápasů jako MatchDTO
     */
    @GetMapping("/past")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getPastMatches() {
        return matchService.getPastMatches();
    }

    /**
     * Vytváří nový zápas.
     *
     * Vstupní data jsou validována pomocí bean validation
     * a vlastní uložení zápasu je delegováno do service vrstvy.
     * Operace je dostupná pro role ADMIN a MANAGER.
     *
     * @param matchDTO DTO s daty nového zápasu
     * @return vytvořený zápas jako MatchDTO
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchDTO createMatch(@Valid @RequestBody MatchDTO matchDTO) {
        return matchService.createMatch(matchDTO);
    }

    /**
     * Vrací detail zápasu podle jeho ID v administrativním pohledu.
     *
     * Jedná se o pohled bez vazby na konkrétního hráče.
     *
     * @param id identifikátor zápasu
     * @return detail zápasu jako MatchDTO
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchDTO getMatch(@PathVariable Long id) {
        return matchService.getMatchById(id);
    }

    /**
     * Vrací historii změn daného zápasu.
     *
     * Historie slouží pro auditní účely a sledování průběžných úprav.
     *
     * @param id identifikátor zápasu
     * @return seznam MatchHistoryDTO představujících historii zápasu
     */
    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchHistoryDTO> getMatchHistory(@PathVariable Long id) {
        return matchHistoryService.getHistoryForMatch(id);
    }

    /**
     * Aktualizuje existující zápas.
     *
     * Aktualizace je delegována do service vrstvy.
     *
     * @param id identifikátor zápasu
     * @param dto aktualizovaná data zápasu
     * @return aktualizovaný zápas jako MatchDTO
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchDTO updateMatch(@PathVariable Long id,
                                @Valid @RequestBody MatchDTO dto) {
        return matchService.updateMatch(id, dto);
    }

    /**
     * Odstraňuje zápas ze systému.
     *
     * Operace je vyhrazena pouze pro roli ADMIN.
     *
     * @param id identifikátor zápasu
     * @return potvrzení úspěšné operace
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponseDTO> deleteMatch(@PathVariable Long id) {
        SuccessResponseDTO response = matchService.deleteMatch(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Ruší zápas a ukládá důvod zrušení.
     *
     * @param matchId identifikátor zápasu
     * @param reason důvod zrušení zápasu
     * @return potvrzení úspěšné operace
     */
    @PatchMapping("/{matchId}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<SuccessResponseDTO> cancelMatch(
            @PathVariable Long matchId,
            @RequestParam MatchCancelReason reason
    ) {
        SuccessResponseDTO response = matchService.cancelMatch(matchId, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Obnovuje dříve zrušený zápas.
     *
     * @param matchId identifikátor zápasu
     * @return potvrzení úspěšné operace
     */
    @PatchMapping("/{matchId}/uncancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<SuccessResponseDTO> unCancelMatch(@PathVariable Long matchId) {
        SuccessResponseDTO response = matchService.unCancelMatch(matchId);
        return ResponseEntity.ok(response);
    }

    // Hráč – endpointy v kontextu aktuálního hráče

    /**
     * Vrací detail konkrétního zápasu z pohledu hráče.
     *
     * @param id identifikátor zápasu
     * @return detail zápasu jako MatchDetailDTO
     */
    @GetMapping("/{id}/detail")
    @PreAuthorize("isAuthenticated()")
    public MatchDetailDTO getMatchDetail(@PathVariable Long id) {
        return matchService.getMatchDetail(id);
    }

    /**
     * Vrací nejbližší nadcházející zápas.
     *
     * @return nejbližší zápas nebo null, pokud žádný neexistuje
     */
    @GetMapping("/next")
    @PreAuthorize("isAuthenticated()")
    public MatchDTO getNextMatch() {
        return matchService.getNextMatch();
    }

    /**
     * Vrací přehled pozic a kapacity pro daný zápas.
     *
     * @param matchId identifikátor zápasu
     * @return přehled pozic pro oba týmy
     */
    @GetMapping("/{matchId}/positions")
    @PreAuthorize("isAuthenticated()")
    public MatchPositionOverviewDTO getPositionOverview(@PathVariable Long matchId) {
        return matchPositionService.getPositionOverviewForMatch(matchId);
    }

    /**
     * Vrací přehled pozic pro konkrétní tým v daném zápase.
     *
     * @param matchId identifikátor zápasu
     * @param team tým, pro který se přehled získává
     * @return přehled pozic pro daný tým
     */
    @GetMapping("/{matchId}/positions/{team}")
    @PreAuthorize("isAuthenticated()")
    public MatchTeamPositionOverviewDTO getTeamPositionOverview(
            @PathVariable Long matchId,
            @PathVariable Team team
    ) {
        return matchPositionService.getPositionOverviewForMatchAndTeam(matchId, team);
    }

    /**
     * Spouští automatické generování první lajny.
     *
     * Operace je dostupná pro role ADMIN a MANAGER.
     *
     * @param matchId identifikátor zápasu
     * @return potvrzení úspěšné operace
     */
    @PostMapping("/{matchId}/auto-lineup")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<SuccessResponseDTO> autoLineup(@PathVariable Long matchId) {
        matchAutoLineupService.autoArrangeStartingLineup(matchId);
        return ResponseEntity.ok(
                new SuccessResponseDTO(
                        "BE - Automatická první lajna byla vygenerována",
                        matchId,
                        LocalDateTime.now().toString()
                )
        );
    }

    /**
     * Aktualizuje skóre zápasu.
     *
     * @param matchId identifikátor zápasu
     * @param request DTO obsahující skóre pro oba týmy
     * @return aktualizovaný zápas jako MatchDTO
     */
    @PatchMapping("/{matchId}/score")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchDTO updateMatchScore(
            @PathVariable Long matchId,
            @Valid @RequestBody MatchScoreUpdateRequest request
    ) {
        return matchService.updateMatchScore(
                matchId,
                request.getScoreLight(),
                request.getScoreDark()
        );
    }
}