package cz.phsoft.hokej.security;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

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

