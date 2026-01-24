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

@Component
public class CurrentPlayerFilter extends OncePerRequestFilter {

    private final PlayerRepository playerRepository;
    private final CurrentPlayerService currentPlayerService;

    public CurrentPlayerFilter(PlayerRepository playerRepository,
                               CurrentPlayerService currentPlayerService) {
        this.playerRepository = playerRepository;
        this.currentPlayerService = currentPlayerService;
    }

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

