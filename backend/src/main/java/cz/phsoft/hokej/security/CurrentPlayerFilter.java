package cz.phsoft.hokej.security;

import cz.phsoft.hokej.player.repositories.PlayerRepository;
import cz.phsoft.hokej.player.services.CurrentPlayerService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * HTTP filtr zajišťující dostupnost aktuálního hráče
 * v průběhu zpracování jednoho requestu.
 *
 * Filtr:
 * - načítá identifikátor aktuálního hráče ze session prostřednictvím CurrentPlayerService,
 * - ověřuje existenci hráče v databázi,
 * - ukládá nalezenou entitu do CurrentPlayerContext,
 * - po dokončení requestu kontext vždy vyčistí.
 *
 * Díky tomu mohou servisní a controller vrstvy pracovat
 * s aktuálním hráčem bez přímé závislosti na HttpSession.
 *
 * Třída rozšiřuje OncePerRequestFilter, takže je garantováno,
 * že bude provedena maximálně jednou za HTTP request.
 */
@Component
public class CurrentPlayerFilter extends OncePerRequestFilter {

    private final PlayerRepository playerRepository;
    private final CurrentPlayerService currentPlayerService;

    /**
     * Vytvoří instanci filtru.
     *
     * @param playerRepository repozitář pro načítání hráčů z databáze
     * @param currentPlayerService service zajišťující přístup k aktuálnímu hráči ze session
     */
    public CurrentPlayerFilter(PlayerRepository playerRepository,
                               CurrentPlayerService currentPlayerService) {
        this.playerRepository = playerRepository;
        this.currentPlayerService = currentPlayerService;
    }

    /**
     * Zpracuje HTTP request a nastaví aktuálního hráče do thread-local kontextu.
     *
     * Postup zpracování:
     * - načte identifikátor aktuálního hráče,
     * - pokud je identifikátor přítomen, ověří existenci hráče v databázi,
     * - uloží entitu hráče do CurrentPlayerContext,
     * - předá řízení dalším filtrům v řetězci,
     * - po dokončení zpracování vždy vyčistí kontext.
     *
     * Vyčištění kontextu je provedeno ve finally bloku,
     * aby bylo zajištěno korektní uvolnění ThreadLocal i v případě výjimky.
     *
     * @param request aktuální HTTP request
     * @param response aktuální HTTP response
     * @param filterChain řetězec dalších filtrů
     * @throws ServletException v případě chyby servletového zpracování
     * @throws IOException v případě I/O chyby
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Long playerId = currentPlayerService.getCurrentPlayerId();

        if (playerId != null) {
            playerRepository.findById(playerId)
                    .ifPresent(CurrentPlayerContext::set);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            CurrentPlayerContext.clear();
        }
    }
}