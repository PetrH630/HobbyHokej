package cz.phsoft.hokej.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Vlastní autentizační filtr pro REST přihlášení (JSON / FORM).
 *
 * ÚČEL:
 * <ul>
 *     <li>umožňuje přihlášení pomocí JSON (application/json),</li>
 *     <li>zachovává podporu klasického form loginu (x-www-form-urlencoded),</li>
 *     <li>nahrazuje standardní redirect chování Spring Security JSON odpověďmi,</li>
 *     <li>vytváří HTTP session a ukládá SecurityContext (stateful autentizace).</li>
 * </ul>
 *
 * PROČ JE POTŘEBA:
 * <ul>
 *     <li>{@link UsernamePasswordAuthenticationFilter} standardně očekává
 *         pouze form data,</li>
 *     <li>SPA frontend (React / Vite) posílá JSON,</li>
 *     <li>REST API nemá používat redirecty, ale strukturované JSON odpovědi.</li>
 * </ul>
 *
 * BEZPEČNOST:
 * <ul>
 *     <li>autentizace probíhá výhradně přes {@link AuthenticationManager},</li>
 *     <li>heslo se nikdy neloguje ani nevrací klientovi,</li>
 *     <li>po úspěchu je vytvořen {@link org.springframework.security.core.context.SecurityContext}
 *         uložený v HTTP session.</li>
 * </ul>
 *
 * POZNÁMKA:
 * <ul>
 *     <li>tento filtr je určen pro stateful (session-based) autentizaci,</li>
 *     <li>pro JWT / stateless přístup by byl nahrazen jiným řešením.</li>
 * </ul>
 */
public class CustomJsonLoginFilter extends UsernamePasswordAuthenticationFilter {

    /**
     * Jackson ObjectMapper pro čtení JSON z request body.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Konstruktor filtru.
     *
     * @param loginUrl    URL endpointu pro login (např. /api/auth/login)
     * @param authManager Spring Security {@link AuthenticationManager}
     */
    public CustomJsonLoginFilter(String loginUrl, AuthenticationManager authManager) {
        setFilterProcessesUrl(loginUrl);
        setAuthenticationManager(authManager);
    }

    // =====================================================
    // POKUS O AUTENTIZACI (LOGIN)
    // =====================================================

    /**
     * Pokusí se autentizovat uživatele na základě requestu.
     *
     * Podporované formáty:
     * <ul>
     *     <li>application/x-www-form-urlencoded</li>
     *     <li>application/json</li>
     * </ul>
     *
     * Očekávaná pole:
     * <ul>
     *     <li>email (username)</li>
     *     <li>password</li>
     * </ul>
     *
     * @throws AuthenticationException při chybě přihlášení
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException {

        try {
            String email = null;
            String password = null;

            // -------------------------------------------------
            // FORM LOGIN (x-www-form-urlencoded)
            // -------------------------------------------------
            if (request.getContentType() != null &&
                    request.getContentType().contains("application/x-www-form-urlencoded")) {

                email = request.getParameter("username");
                password = request.getParameter("password");
            }

            // -------------------------------------------------
            // JSON LOGIN (application/json)
            // -------------------------------------------------
            if ((email == null || password == null) &&
                    request.getContentType() != null &&
                    request.getContentType().contains("application/json")) {

                Map<String, String> json =
                        objectMapper.readValue(request.getInputStream(), Map.class);

                email = json.get("email");
                password = json.get("password");
            }

            // -------------------------------------------------
            // VALIDACE VSTUPŮ
            // -------------------------------------------------
            if (email == null || password == null ||
                    email.isBlank() || password.isBlank()) {

                throw new BadCredentialsException("BE - Chybí přihlašovací údaje");
            }

            // -------------------------------------------------
            // VYTVOŘENÍ AUTHENTICATION TOKENU
            // -------------------------------------------------
            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(email, password);

            setDetails(request, authRequest);

            // delegace autentizace na AuthenticationManager
            return this.getAuthenticationManager().authenticate(authRequest);

        } catch (IOException e) {
            // chyba při čtení JSON body (malformed JSON apod.)
            throw new RuntimeException(e);
        }
    }

    // =====================================================
    // ÚSPĚŠNÝ LOGIN
    // =====================================================

    /**
     * Zavolá se po úspěšné autentizaci.
     *
     * Co se zde děje:
     * <ul>
     *     <li>uloží se {@link Authentication} do {@link SecurityContextHolder},</li>
     *     <li>vytvoří se HTTP session (pokud neexistuje),</li>
     *     <li>do session se uloží SPRING_SECURITY_CONTEXT,</li>
     *     <li>vrátí se JSON odpověď místo redirectu.</li>
     * </ul>
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult)
            throws IOException, ServletException {

        // nastavení SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authResult);

        // vytvoření session a uložení kontextu
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );

        // JSON odpověď
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("user", authResult.getName());

        objectMapper.writeValue(response.getWriter(), result);
    }

    // =====================================================
    // NEÚSPĚŠNÝ LOGIN
    // =====================================================

    /**
     * Zavolá se při neúspěšném přihlášení.
     *
     * Vrací JSON odpověď s HTTP 401.
     *
     * Rozlišuje:
     * <ul>
     *     <li>neaktivovaný účet,</li>
     *     <li>neplatné přihlašovací údaje,</li>
     *     <li>ostatní chyby autentizace.</li>
     * </ul>
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed)
            throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("status", "error");

        if (failed.getCause() instanceof cz.phsoft.hokej.exceptions.AccountNotActivatedException) {
            result.put("message", failed.getCause().getMessage());
        } else if (failed instanceof BadCredentialsException) {
            result.put("message", "BE - Neplatné přihlašovací údaje");
        } else {
            result.put("message", "BE - Chyba při přihlášení");
        }

        objectMapper.writeValue(response.getWriter(), result);
    }
}
