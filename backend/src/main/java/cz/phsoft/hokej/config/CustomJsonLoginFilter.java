package cz.phsoft.hokej.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.phsoft.hokej.models.services.AppUserService;
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
 * Vlastní autentizační filtr pro REST přihlášení.
 *
 * Filtr rozšiřuje výchozí {@link UsernamePasswordAuthenticationFilter} tak,
 * aby podporoval přihlášení jak pomocí formuláře, tak pomocí JSON payloadu.
 * Standardní redirect chování Spring Security je nahrazeno JSON odpověďmi
 * a po úspěšném přihlášení se vytváří HTTP session se SecurityContextem.
 *
 * Filtr je určen pro stavový (session-based) způsob autentizace. Pro
 * stateless přístup s JWT by se použil jiný mechanismus.
 */
public class CustomJsonLoginFilter extends UsernamePasswordAuthenticationFilter {

    /**
     * Objekt pro čtení JSON z request body.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Servis pro práci s uživatelskými účty.
     *
     * Používá se pro aktualizaci časových razítek přihlášení
     * po úspěšné autentizaci.
     */
    private final AppUserService appUserService;

    /**
     * Vytvoří filtr pro zadanou login URL.
     *
     * @param loginUrl    URL endpointu pro login, například /api/auth/login
     * @param authManager instance AuthenticationManager používaná pro autentizaci
     * @param appUserService servis pro správu uživatelských účtů
     */
    public CustomJsonLoginFilter(String loginUrl,
                                 AuthenticationManager authManager,
                                 AppUserService appUserService) {
        setFilterProcessesUrl(loginUrl);
        setAuthenticationManager(authManager);
        this.appUserService = appUserService;
    }

    // Pokus o autentizaci (login)

    /**
     * Pokusí se autentizovat uživatele na základě HTTP requestu.
     *
     * Podporované formáty:
     * - {@code application/x-www-form-urlencoded} (klasický form login),
     * - {@code application/json} (SPA frontend).
     *
     * Očekávaná pole:
     * - {@code email} (případně {@code username} u form loginu),
     * - {@code password}.
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

            // Form login (x-www-form-urlencoded)
            if (request.getContentType() != null &&
                    request.getContentType().contains("application/x-www-form-urlencoded")) {

                email = request.getParameter("username");
                password = request.getParameter("password");
            }

            // JSON login (application/json)
            if ((email == null || password == null) &&
                    request.getContentType() != null &&
                    request.getContentType().contains("application/json")) {

                Map<String, String> json =
                        objectMapper.readValue(request.getInputStream(), Map.class);

                email = json.get("email");
                password = json.get("password");
            }

            // Základní validace vstupů
            if (email == null || password == null ||
                    email.isBlank() || password.isBlank()) {

                throw new BadCredentialsException("BE - Chybí přihlašovací údaje");
            }

            // Vytvoření Authentication tokenu
            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(email, password);

            setDetails(request, authRequest);

            // Delegace autentizace na AuthenticationManager
            return this.getAuthenticationManager().authenticate(authRequest);

        } catch (IOException e) {
            // Chyba při čtení JSON body
            throw new RuntimeException(e);
        }
    }

    // Úspěšný login

    /**
     * Zpracuje úspěšnou autentizaci.
     *
     * Do {@link SecurityContextHolder} se uloží autentizace, vytvoří se
     * HTTP session a do ní se uloží {@link org.springframework.security.core.context.SecurityContext}.
     * Následně se klientovi vrací JSON odpověď místo redirectu.
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult)
            throws IOException, ServletException {

        // Aktualizace login timestampů v AppUserEntity
        String email = authResult.getName();
        appUserService.onSuccessfulLogin(email);

        // Nastavení SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authResult);

        // Vytvoření session a uložení kontextu
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

    // Neúspěšný login

    /**
     * Zpracuje neúspěšnou autentizaci.
     *
     * Vrací se HTTP status 401 a JSON odpověď s informací o chybě.
     * Rozlišuje se neaktivovaný účet, neplatné přihlašovací údaje
     * a ostatní chyby při přihlášení.
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
