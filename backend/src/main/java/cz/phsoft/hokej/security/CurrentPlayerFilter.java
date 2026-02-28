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
 * v průběhu zpracování requestu.
 *
 * Filtr:
 * - načítá ID aktuálního hráče z HTTP session,
 * - ověřuje existenci hráče v databázi,
 * - ukládá hráče do CurrentPlayerContext,
 * - po dokončení requestu kontext vždy vyčistí.
 *
 * Filtr umožňuje, aby servisní a controller vrstvy
 * pracovaly s aktuálním hráčem bez nutnosti
 * přímého přístupu k HttpSession.
 */
@Component
public class CurrentPlayerFilter extends OncePerRequestFilter {

    private final PlayerRepository playerRepository;
    private final CurrentPlayerService currentPlayerService;

    public CurrentPlayerFilter(PlayerRepository playerRepository,
                               CurrentPlayerService currentPlayerService) {
        this.playerRepository = playerRepository;
        this.currentPlayerService = currentPlayerService;
    }

    /**
     * Filtrační metoda volaná jednou pro každý HTTP request.
     *
     * Postup zpracování:
     * - načte ID aktuálního hráče ze session,
     * - ověří existenci hráče v databázi,
     * - uloží hráče do thread-local kontextu,
     * - předá řízení dalším filtrům,
     * - v závěru vždy vyčistí kontext.
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
