package cz.phsoft.hokej.controllers;

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
 * REST controller pro administraci registrací hráčů na zápasy.
 * <p>
 * Endpoints zde jsou určeny pro role ADMIN a MANAGER a umožňují:
 * <ul>
 *     <li>získat seznam všech registrací,</li>
 *     <li>získat registrace pro konkrétní zápas nebo hráče,</li>
 *     <li>získat hráče, kteří na zápas vůbec nereagovali,</li>
 *     <li>provést registraci / odhlášení / omluvu hráče za něj,</li>
 *     <li>označit hráče v zápase jako neomluveně nepřítomného.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/registrations/admin")
public class AdminMatchRegistrationController {

    private final MatchRegistrationService service;
    private final CurrentPlayerService currentPlayerService;
    private final MatchService matchService;

    public AdminMatchRegistrationController(MatchRegistrationService service,
                                            CurrentPlayerService currentPlayerService,
                                            MatchService matchService) {
        this.service = service;
        this.currentPlayerService = currentPlayerService;
        this.matchService = matchService;
    }

    /**
     * Vrátí všechny registrace všech hráčů na všechny zápasy.
     * <p>
     * Určeno pro administrativní přehled a statistiky.
     *
     * @return seznam všech registrací
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> getAllRegistrations() {
        return service.getAllRegistrations();
    }

    /**
     * Vrátí všechny registrace hráčů pro konkrétní zápas.
     *
     * @param matchId ID zápasu
     * @return seznam registrací pro daný zápas
     */
    @GetMapping("/for-match/{matchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> forMatch(@PathVariable Long matchId) {
        return service.getRegistrationsForMatch(matchId);
    }

    /**
     * Vrátí všechny registrace konkrétního hráče napříč zápasy.
     *
     * @param playerId ID hráče
     * @return seznam registrací daného hráče
     */
    @GetMapping("/for-player/{playerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<MatchRegistrationDTO> forPlayer(@PathVariable Long playerId) {
        return service.getRegistrationsForPlayer(playerId);
    }

    /**
     * Vrátí seznam hráčů, kteří na pozvánku k danému zápasu
     * zatím vůbec nereagovali (žádná registrace ani omluva).
     *
     * @param matchId ID zápasu
     * @return seznam hráčů bez reakce
     */
    @GetMapping("/no-response/{matchId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public List<PlayerDTO> getNoResponse(@PathVariable Long matchId) {
        return service.getNoResponsePlayers(matchId);
    }

    /**
     * Univerzální endpoint pro vytvoření nebo aktualizaci registrace
     * za konkrétního hráče.
     * <p>
     * Umožňuje ADMIN / MANAGER:
     * <ul>
     *     <li>registrovat hráče na zápas,</li>
     *     <li>odregistrovat ho,</li>
     *     <li>vytvořit nebo upravit omluvu.</li>
     * </ul>
     * Konkrétní chování závisí na obsahu {@link MatchRegistrationRequest}.
     *
     * @param playerId ID hráče, za kterého se změna provádí
     * @param request  požadavek na změnu registrace (register / unregister / excuse)
     * @return aktuální stav registrace po provedení operace
     */
    @PostMapping("/upsert/{playerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchRegistrationDTO upsert(@PathVariable Long playerId,
                                       @Valid @RequestBody MatchRegistrationRequest request) {
        return service.upsertRegistration(playerId, request);
    }

    /**
     * Označí hráče v konkrétním zápase jako neomluveně nepřítomného.
     * <p>
     * Typické použití po odehrání zápasu, kdy ADMIN/MANAGER vyhodnocuje docházku.
     * Status registrace je aktualizován v {@link MatchService#markNoExcused(Long, Long, String)}.
     *
     * @param matchId   ID zápasu
     * @param playerId  ID hráče
     * @param adminNote volitelná poznámka administrátora (např. důvod označení)
     * @return aktualizovaná registrace hráče v daném zápase
     */
    @PatchMapping("/{matchId}/players/{playerId}/no-excused")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public MatchRegistrationDTO markNoExcused(
            @PathVariable Long matchId,
            @PathVariable Long playerId,
            @RequestParam(required = false) String adminNote
    ) {
        return matchService.markNoExcused(matchId, playerId, adminNote);
    }
}
