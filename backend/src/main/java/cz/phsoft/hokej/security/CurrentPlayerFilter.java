package cz.phsoft.hokej.security;

import cz.phsoft.hokej.data.repositories.PlayerRepository;
import cz.phsoft.hokej.models.services.CurrentPlayerService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Security / request filter, který:
 *
 * - načte "current player" ze session (HttpSession)
 * - uloží ho do thread-local kontextu (CurrentPlayerContext)
 * - po zpracování requestu kontext vždy vyčistí
 *
 * DŮVOD EXISTENCE:
 * ----------------
 * Uživatelský účet (AppUser) může mít více hráčů (Player).
 * FE si zvolí, který hráč je právě "aktivní".
 *
 * Tento filtr zajistí, že:
 * - během zpracování jednoho HTTP requestu
 * - je dostupný aktuální hráč BEZ nutnosti ho tahat ze session všude ručně
 *
 * Použití:
 * - služby / kontrolery mohou číst CurrentPlayerContext.get()
 * - není potřeba řešit HttpSession v business logice
 *
 * THREAD-SAFETY:
 * - používá se ThreadLocal
 * - každý request má svůj vlastní kontext
 * - v finally bloku je kontext vždy vyčištěn
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
     * Hlavní filtrační metoda – volá se jednou pro každý HTTP request.
     *
     * Postup:
     * 1) načteme currentPlayerId ze session (přes CurrentPlayerService)
     * 2) pokud existuje, ověříme, že hráč opravdu existuje v DB
     * 3) uložíme PlayerEntity do CurrentPlayerContext (ThreadLocal)
     * 4) necháme pokračovat filter chain
     * 5) v finally bloku kontext VŽDY vyčistíme
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ID aktuálního hráče uložené v HTTP session
        Long playerId = currentPlayerService.getCurrentPlayerId();

        if (playerId != null) {
            // bezpečnostní kontrola – hráč musí existovat
            playerRepository.findById(playerId)
                    .ifPresent(CurrentPlayerContext::set);
        }

        try {
            // pokračování ve zpracování requestu
            filterChain.doFilter(request, response);
        } finally {
            // důležité: vždy vyčistit ThreadLocal kontext
            CurrentPlayerContext.clear();
        }
    }
}
