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
 * HTTP filtr umožňující administrátorovi dočasně vystupovat jménem hráče.
 *
 * Filtr zpracovává hlavičku X-Impersonate-PlayerId. Pokud je hlavička přítomna,
 * ověřuje se, že aktuálně přihlášený uživatel má roli ADMIN. Následně se kontroluje,
 * že cílový hráč existuje a je ve stavu APPROVED.
 *
 * Pokud jsou všechny podmínky splněny, nastaví se identifikátor hráče
 * do {@link ImpersonationContext}. Hodnota je uložena pomocí ThreadLocal
 * a platí pouze po dobu zpracování jednoho HTTP požadavku.
 *
 * V případě porušení pravidel je vrácena odpověď se statusem 403
 * a zpracování requestu nepokračuje.
 *
 * Filtr je navržen jako bezpečnostní vrstva nad aplikační logikou
 * a neukládá informace o impersonaci do session.
 */
public class ImpersonationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ImpersonationFilter.class);

    private static final String HEADER = "X-Impersonate-PlayerId";

    private final PlayerRepository playerRepository;

    public ImpersonationFilter(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    /**
     * Určuje, zda má být filtr přeskočen.
     *
     * Filtr se neaplikuje na autentizační endpointy, veřejné cesty,
     * chybové stránky a favicon, aby nedocházelo k interferenci
     * s přihlašovacím procesem nebo veřejnými zdroji.
     *
     * @param request Aktuální HTTP požadavek.
     * @return True, pokud se filtr nemá aplikovat, jinak false.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth")
                || path.startsWith("/error")
                || path.startsWith("/public")
                || path.startsWith("/favicon.ico");
    }

    /**
     * Zpracovává HTTP požadavek a případně aktivuje impersonaci hráče.
     *
     * Pokud hlavička X-Impersonate-PlayerId není přítomna, request pokračuje
     * bez zásahu. Pokud je přítomna, provádí se následující kontroly:
     * - uživatel musí být přihlášen,
     * - uživatel musí mít roli ADMIN,
     * - identifikátor hráče musí být validní,
     * - hráč musí existovat a být ve stavu APPROVED.
     *
     * Při splnění podmínek se identifikátor hráče nastaví do
     * {@link ImpersonationContext}. Po dokončení requestu se kontext
     * vždy vyčistí v bloku finally, aby nedošlo k úniku hodnoty
     * mezi jednotlivými požadavky.
     *
     * @param request HTTP požadavek.
     * @param response HTTP odpověď.
     * @param filterChain Řetězec filtrů, do kterého se deleguje další zpracování.
     * @throws ServletException Pokud dojde k chybě na úrovni servletu.
     * @throws IOException Pokud dojde k chybě při zápisu odpovědi.
     */
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

    /**
     * Vyhodnocuje, zda má uživatel roli ADMIN.
     *
     * @param authentication Autentizační objekt aktuálního uživatele.
     * @return True, pokud uživatel má roli ADMIN, jinak false.
     */
    private boolean hasRoleAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    /**
     * Pokusí se převést textovou hodnotu na Long.
     *
     * Pokud převod selže, vrací se null.
     *
     * @param v Textová hodnota z hlavičky.
     * @return Identifikátor hráče jako Long, nebo null při chybě.
     */
    private Long parseLongOrNull(String v) {
        try {
            return Long.parseLong(v.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Zapíše do odpovědi HTTP status 403 s JSON tělem.
     *
     * Metoda se používá při porušení pravidel impersonace.
     *
     * @param response HTTP odpověď.
     * @param message Text chybové zprávy.
     * @throws IOException Pokud dojde k chybě při zápisu odpovědi.
     */
    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.getWriter().write("{\"status\":\"forbidden\",\"message\":\"" + escapeJson(message) + "\"}");
    }

    /**
     * Escapuje text pro bezpečné vložení do JSON odpovědi.
     *
     * @param s Vstupní text.
     * @return Escapovaný text.
     */
    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
