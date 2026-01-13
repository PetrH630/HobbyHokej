package cz.phsoft.hokej.security;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class CurrentPlayerFilter extends OncePerRequestFilter {

    private final PlayerRepository playerRepository;

    public CurrentPlayerFilter(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String email = auth.getName();

            // Načteme hráče přímo přes repository, ne přes lazy kolekci
            List<PlayerEntity> players = playerRepository.findAllByUserEmail(email);

            if (players.size() == 1) {
                // jen jeden hráč → nastavíme jako current
                CurrentPlayerContext.set(players.get(0));
            }
            // víc hráčů → frontend musí zavolat endpoint pro výběr hráče
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            CurrentPlayerContext.clear(); // vždy clear po requestu
        }
    }
}
