package cz.phsoft.hokej.security.impersonation;

import cz.phsoft.hokej.data.entities.PlayerEntity;
import cz.phsoft.hokej.data.enums.PlayerStatus;
import cz.phsoft.hokej.data.repositories.PlayerRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * HTTP filtr, který umožňuje administrátorovi dočasně zastoupit hráče.
 *
 * Filtr čte hlavičku {@code X-Impersonate-PlayerId} a pokud je uživatel přihlášen
 * a má roli ADMIN, nastaví do {@link ImpersonationContext} identifikátor hráče,
 * který se má v daném requestu považovat za aktuálního.
 *
 * Filtr ověřuje, že hráč existuje a je ve stavu {@link PlayerStatus#APPROVED}.
 * V případě chyby vrací odpověď 403 a request dál nepokračuje.
 *
 * Identifikátor zastoupeného hráče se neukládá do session a platí pouze pro
 * daný request.
 */
public class ImpersonationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ImpersonationFilter.class);

    private static final String HEADER = "X-Impersonate-PlayerId";

    private final PlayerRepository playerRepository;

    public ImpersonationFilter(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth")
                || path.startsWith("/error")
                || path.startsWith("/public")
                || path.startsWith("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String headerValue = request.getHeader(HEADER);

            if (headerValue == null || headerValue.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                writeForbidden(response, "BE - Zastoupení je dostupné pouze pro přihlášeného administrátora.");
                return;
            }

            if (!hasRoleAdmin(authentication)) {
                writeForbidden(response, "BE - Zastoupení je dostupné pouze pro roli ADMIN.");
                return;
            }

            Long playerId = parseLongOrNull(headerValue);
            if (playerId == null) {
                writeForbidden(response, "BE - Neplatná hlavička X-Impersonate-PlayerId.");
                return;
            }

            PlayerEntity player = playerRepository.findById(playerId).orElse(null);
            if (player == null) {
                writeForbidden(response, "BE - Zastupovaný hráč neexistuje.");
                return;
            }

            if (player.getPlayerStatus() != PlayerStatus.APPROVED) {
                writeForbidden(response, "BE - Nelze zastoupit hráče, který není schválen administrátorem.");
                return;
            }

            ImpersonationContext.setImpersonatedPlayerId(playerId);

            log.info(
                    "Impersonation enabled: adminUser={}, impersonatedPlayerId={}, method={}, uri={}",
                    authentication.getName(),
                    playerId,
                    request.getMethod(),
                    request.getRequestURI()
            );

            filterChain.doFilter(request, response);

        } finally {
            ImpersonationContext.clear();
        }
    }

    private boolean hasRoleAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private Long parseLongOrNull(String v) {
        try {
            return Long.parseLong(v.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"forbidden\",\"message\":\"" + escapeJson(message) + "\"}");
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
