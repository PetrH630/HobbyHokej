package cz.phsoft.hokej.match.controllers;

import cz.phsoft.hokej.match.dto.*;
import cz.phsoft.hokej.match.enums.MatchCancelReason;
import cz.phsoft.hokej.match.services.MatchAutoLineupService;
import cz.phsoft.hokej.match.services.MatchHistoryService;
import cz.phsoft.hokej.match.services.MatchPositionService;
import cz.phsoft.hokej.match.services.MatchService;
import cz.phsoft.hokej.player.enums.Team;
import cz.phsoft.hokej.player.services.CurrentPlayerService;
import cz.phsoft.hokej.registration.dto.MatchTeamPositionOverviewDTO;
import cz.phsoft.hokej.shared.dto.SuccessResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller, který se používá pro správu zápasů.
 *
 * Zajišťuje administrativní operace nad zápasy pro role ADMIN a MANAGER,
 * včetně vytváření, aktualizace, mazání, zrušení a obnovení zápasů.
 * Zároveň poskytuje pohled na zápasy z perspektivy aktuálního hráče
 * a detail zápasu včetně informací o registracích.
 *
 * Veškerá business logika se deleguje do {@link MatchService},
 * práce s historií zápasů do {@link MatchHistoryService} a práce
 * s aktuálním hráčem do {@link CurrentPlayerService}.
 */
@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;
    private final CurrentPlayerService currentPlayerService;
    private final MatchHistoryService matchHistoryService;
    private final MatchPositionService matchPositionService;
    private final MatchAutoLineupService matchAutoLineupService;

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
     * Endpoint je určen pro administrativní přehled zápasů a
     * je dostupný pro role ADMIN a MANAGER.
     *
     * @return seznam všech zápasů jako {@link MatchDTO}
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getAllMatches() {
        return matchService.getAllMatches();
    }

    /**
     * Vrací seznam všech nadcházejících zápasů.
     *
     * Endpoint je dostupný pro role ADMIN a MANAGER a slouží
     * k přehledu budoucích zápasů v systému.
     *
     * @return seznam nadcházejících zápasů jako {@link MatchDTO}
     */
    @GetMapping("/upcoming")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getUpcomingMatches() {
        return matchService.getUpcomingMatches();
    }

    /**
     * Vrací seznam všech již odehraných zápasů.
     *
     * Endpoint je dostupný pro role ADMIN a MANAGER a používá se
     * pro přehled historicky odehraných zápasů.
     *
     * @return seznam odehraných zápasů jako {@link MatchDTO}
     */
    @GetMapping("/past")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getPastMatches() {
        return matchService.getPastMatches();
    }

    /**
     * Vytváří nový zápas.
     *
     * Vstupní data jsou validována pomocí bean validation a
     * vlastní uložení zápasu se deleguje do servisní vrstvy.
     * Operace je vyhrazena roli ADMIN.
     *
     * @param matchDTO DTO s daty nového zápasu
     * @return vytvořený zápas jako {@link MatchDTO}
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchDTO createMatch(@Valid @RequestBody MatchDTO matchDTO) {
        return matchService.createMatch(matchDTO);
    }

    /**
     * Vrací detail zápasu podle jeho ID v administrativním pohledu.
     *
     * Jedná se o pohled pro administrátora nebo manažera bez vazby
     * na konkrétního hráče. Endpoint je vhodný pro editaci zápasu
     * nebo pro jeho detailní kontrolu.
     *
     * @param id ID zápasu
     * @return {@link MatchDTO} s detaily zápasu
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchDTO getMatch(@PathVariable Long id) {
        return matchService.getMatchById(id);
    }

    /**
     * Vrací historii změn daného zápasu.
     *
     * Historie slouží pro auditní účely a sledování průběžných
     * úprav parametrů zápasu. Záznamy jsou získávány ze servisní
     * vrstvy, která čte historii z databáze.
     *
     * @param id ID zápasu
     * @return seznam {@link MatchHistoryDTO} představujících historii zápasu
     */
    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchHistoryDTO> getMatchHistory(@PathVariable Long id) {
        return matchHistoryService.getHistoryForMatch(id);
    }

    /**
     * Aktualizuje existující zápas.
     *
     * Vstupní data jsou validována a následná aktualizace se
     * deleguje do servisní vrstvy. Endpoint je dostupný pro role
     * ADMIN a MANAGER.
     *
     * @param id  ID zápasu, který má být aktualizován
     * @param dto DTO s aktualizovanými daty zápasu
     * @return {@link MatchDTO} s uloženými změnami
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
     * Odstranění zápasu se používá výjimečně, například při
     * chybně zadaném zápasu. Operace je vyhrazena pouze pro roli ADMIN
     * a je realizována prostřednictvím servisní vrstvy.
     *
     * @param id ID zápasu, který má být odstraněn
     * @return {@link SuccessResponseDTO} s výsledkem operace
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponseDTO> deleteMatch(@PathVariable Long id) {
        SuccessResponseDTO response = matchService.deleteMatch(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Vrací seznam zápasů, které jsou dostupné pro konkrétního hráče.
     *
     * Dostupnost zápasů je určována na základě doménových pravidel,
     * například podle kapacity nebo stavu zápasu. Endpoint je dostupný
     * pro role ADMIN a MANAGER a slouží zejména pro administrativní práci
     * s registracemi konkrétního hráče.
     *
     * @param playerId ID hráče
     * @return seznam dostupných zápasů pro daného hráče jako {@link MatchDTO}
     */
    @GetMapping("/available-for-player/{playerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchDTO> getAvailableMatchesForPlayer(@PathVariable Long playerId) {
        return matchService.getAvailableMatchesForPlayer(playerId);
    }

    /**
     * Ruší zápas a ukládá důvod zrušení.
     *
     * Operace je dostupná pro role ADMIN a MANAGER a typicky
     * spouští navazující proces notifikací pro dotčené hráče.
     * Důvod zrušení je předáván jako enum {@link MatchCancelReason}.
     *
     * @param matchId ID zápasu, který má být zrušen
     * @param reason  důvod zrušení zápasu
     * @return {@link SuccessResponseDTO} s výsledkem operace
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
     * Obnovení zápasu vrací zápas do aktivního stavu a umožňuje
     * další práci s registracemi hráčů. Operace je dostupná pro
     * role ADMIN a MANAGER a zpracování je delegováno na servisní vrstvu.
     *
     * @param matchId ID zápasu, který má být obnoven
     * @return {@link SuccessResponseDTO} s výsledkem operace
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
     * Detail obsahuje informace o registracích, volných místech
     * a možnostech úprav registrace pro aktuálního hráče. Endpoint
     * je dostupný pro přihlášené uživatele.
     *
     * @param id ID zápasu
     * @return {@link MatchDetailDTO} s detailem zápasu pro hráče
     */
    @GetMapping("/{id}/detail")
    @PreAuthorize("isAuthenticated()")
    public MatchDetailDTO getMatchDetail(@PathVariable Long id) {
        return matchService.getMatchDetail(id);
    }

    /**
     * Vrací nejbližší nadcházející zápas v systému.
     *
     * Endpoint se používá například pro zobrazení nejbližšího
     * zápasu na úvodní stránce aplikace pro přihlášeného uživatele.
     *
     * @return {@link MatchDTO} s nejbližším zápasem nebo null, pokud žádný neexistuje
     */
    @GetMapping("/next")
    @PreAuthorize("isAuthenticated()")
    public MatchDTO getNextMatch() {
        return matchService.getNextMatch();
    }

    /**
     * Vrací seznam nadcházejících zápasů pro aktuálně zvoleného hráče.
     *
     * Před voláním služby se vyžaduje, aby byl nastaven aktuální hráč.
     * Samotné zjištění ID aktuálního hráče se zajišťuje pomocí
     * {@link CurrentPlayerService}. Endpoint je dostupný pro
     * přihlášené uživatele.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return seznam nadcházejících zápasů pro aktuálního hráče jako {@link MatchDTO}
     */
    @GetMapping("/me/upcoming")
    @PreAuthorize("isAuthenticated()")
    public List<MatchDTO> getUpcomingMatchesForMe(Authentication authentication) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return matchService.getUpcomingMatchesForPlayer(currentPlayerId);
    }

    /**
     * Vrací přehled nadcházejících zápasů pro aktuálního hráče.
     *
     * Přehled je určen zejména pro kompaktní zobrazení zápasů
     * v uživatelském rozhraní, například v podobě karet. Skutečné
     * načtení dat se deleguje na servisní vrstvu.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return seznam {@link MatchOverviewDTO} s nadcházejícími zápasy pro hráče
     */
    @GetMapping("/me/upcoming-overview")
    @PreAuthorize("isAuthenticated()")
    public List<MatchOverviewDTO> getUpcomingMatchesOverviewForMe(Authentication authentication) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return matchService.getUpcomingMatchesOverviewForPlayer(currentPlayerId);
    }

    /**
     * Vrací seznam všech již odehraných zápasů pro aktuálního hráče.
     *
     * Seznam slouží pro zobrazení historie zápasů daného hráče
     * v uživatelském rozhraní. Endpoint je dostupný pro přihlášené
     * uživatele a identita hráče se určuje pomocí {@link CurrentPlayerService}.
     *
     * @param authentication autentizační kontext přihlášeného uživatele
     * @return seznam {@link MatchOverviewDTO} pro odehrané zápasy aktuálního hráče
     */
    // TODO - JEN ZÁPASY OD VYTVOŘENÍ HRÁČE
    @GetMapping("/me/all-passed")
    @PreAuthorize("isAuthenticated()")
    public List<MatchOverviewDTO> getAllMatchesForPlayer(Authentication authentication) {
        currentPlayerService.requireCurrentPlayer();
        Long currentPlayerId = currentPlayerService.getCurrentPlayerId();
        return matchService.getAllPassedMatchesForPlayer(currentPlayerId);
    }


/**
 * Vrací přehled pozic a kapacity pro daný zápas pro oba týmy.
 *
 * Metoda se používá například pro administrátorské zobrazení
 * nebo detail zápasu, kde je potřeba vidět obsazenost DARK i LIGHT
 * současně.
 *
 * @param matchId Identifikátor zápasu.
 * @return Přehled pozic a jejich obsazenosti pro oba týmy.
 */
    @GetMapping("/{matchId}/positions")
    @PreAuthorize("isAuthenticated()")
    public MatchPositionOverviewDTO getPositionOverview(
            @PathVariable Long matchId
    ) {
        return matchPositionService.getPositionOverviewForMatch(matchId);
    }
    /**
     * Vrací přehled pozic a kapacity pro konkrétní tým v daném zápase.
     *
     * Metoda se používá pro zobrazení obsazených a volných pozic na ledě
     * v rámci jednoho zápasu a jednoho týmu. Výpočet kapacity pozic se
     * deleguje do MatchModeLayoutUtil, načtení registrací a výpočet
     * obsazenosti se deleguje do MatchPositionService.
     *
     * @param matchId Identifikátor zápasu.
     * @param team    Tým, pro který se přehled pozic získává.
     * @return Přehled pozic a jejich obsazenosti pro daný tým.
     */
    @GetMapping("/{matchId}/positions/{team}")
    @PreAuthorize("isAuthenticated()")
    public MatchTeamPositionOverviewDTO getTeamPositionOverview(
            @PathVariable Long matchId,
            @PathVariable Team team
    ) {
        return matchPositionService.getPositionOverviewForMatchAndTeam(matchId, team);
    }

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
}
