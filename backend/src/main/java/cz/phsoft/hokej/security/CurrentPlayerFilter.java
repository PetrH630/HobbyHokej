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
 * Security / request filtr pro práci s „aktuálním hráčem“.
 *
 * ÚČEL:
 * ------
 * Zajistit, aby byl v průběhu zpracování jednoho HTTP requestu
 * v thread-local kontextu ({@link CurrentPlayerContext})
 * dostupný hráč, kterého si uživatel vybral jako „current player“.
 *
 * DŮVOD EXISTENCE:
 * ----------------
 * <ul>
 *     <li>uživatelský účet ({@code AppUser}) může mít více hráčů ({@code Player}),</li>
 *     <li>FE si zvolí, který hráč je právě aktivní,</li>
 *     <li>backend potřebuje mít v průběhu requestu pohodlně dostupného hráče
 *         bez nutnosti pracovat přímo s {@code HttpSession} v každé vrstvě.</li>
 * </ul>
 *
 * CHOVÁNÍ:
 * --------
 * <ol>
 *     <li>získá ID aktuálního hráče ze session přes {@link CurrentPlayerService},</li>
 *     <li>ověří existenci hráče v DB přes {@link PlayerRepository},</li>
 *     <li>uloží {@code PlayerEntity} do {@link CurrentPlayerContext} (ThreadLocal),</li>
 *     <li>předá řízení dál ve filter chainu,</li>
 *     <li>po dokončení requestu vždy kontext vyčistí.</li>
 * </ol>
 *
 * THREAD-SAFETY:
 * --------------
 * <ul>
 *     <li>využívá {@link ThreadLocal} – každý request má vlastní kontext,</li>
 *     <li>v {@code finally} bloku se vždy volá {@link CurrentPlayerContext#clear()},</li>
 *     <li>tím se předchází memory leakům a přenosu dat mezi requesty.</li>
 * </ul>
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
     * <ol>
     *     <li>načte {@code currentPlayerId} ze session
     *         pomocí {@link CurrentPlayerService#getCurrentPlayerId()},</li>
     *     <li>pokud ID existuje, ověří přítomnost hráče v databázi,</li>
     *     <li>pokud je hráč nalezen, uloží ho do {@link CurrentPlayerContext},</li>
     *     <li>pokračuje ve filter chainu,</li>
     *     <li>vždy v {@code finally} bloku vyčistí thread-local kontext.</li>
     * </ol>
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ID aktuálního hráče uložené v HTTP session
        Long playerId = currentPlayerService.getCurrentPlayerId();

        if (playerId != null) {
            // bezpečnostní kontrola – hráč musí existovat v DB
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
