package cz.phsoft.hokej.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.phsoft.hokej.user.services.AppUserService;
import cz.phsoft.hokej.user.exceptions.AccountNotActivatedException;
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
 * Filtr rozšiřuje výchozí implementaci Spring Security tak, aby bylo
 * podporováno přihlášení pomocí JSON payloadu i klasického formuláře.
 * Po úspěšné autentizaci se vytváří HTTP session a vrací se JSON odpověď.
 *
 * Filtr je určen pro stavový způsob autentizace založený na session.
 */
public class CustomJsonLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AppUserService appUserService;

    public CustomJsonLoginFilter(String loginUrl,
                                 AuthenticationManager authManager,
                                 AppUserService appUserService) {
        setFilterProcessesUrl(loginUrl);
        setAuthenticationManager(authManager);
        this.appUserService = appUserService;
    }

    /**
     * Pokusí se autentizovat uživatele na základě HTTP requestu.
     *
     * Podporuje formát application/x-www-form-urlencoded a application/json.
     *
     * @param request  HTTP požadavek
     * @param response HTTP odpověď
     * @return autentizace
     * @throws AuthenticationException při chybě přihlášení
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException {

        try {
            String email = null;
            String password = null;

            if (request.getContentType() != null &&
                    request.getContentType().contains("application/x-www-form-urlencoded")) {

                email = request.getParameter("username");
                password = request.getParameter("password");
            }

            if ((email == null || password == null) &&
                    request.getContentType() != null &&
                    request.getContentType().contains("application/json")) {

                Map<String, String> json =
                        objectMapper.readValue(request.getInputStream(), Map.class);

                email = json.get("email");
                password = json.get("password");
            }

            if (email == null || password == null ||
                    email.isBlank() || password.isBlank()) {

                throw new BadCredentialsException("BE - Chybí přihlašovací údaje");
            }

            System.out.println("LOGIN contentType=" + request.getContentType());
            System.out.println("LOGIN email='" + email + "'");

            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(email, password);

            setDetails(request, authRequest);

            return this.getAuthenticationManager().authenticate(authRequest);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Zpracuje úspěšnou autentizaci.
     *
     * Nastaví SecurityContext, vytvoří HTTP session a vrátí JSON odpověď.
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult)
            throws IOException, ServletException {

        String email = authResult.getName();
        appUserService.onSuccessfulLogin(email);

        SecurityContextHolder.getContext().setAuthentication(authResult);

        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("user", authResult.getName());

        objectMapper.writeValue(response.getWriter(), result);
    }

    /**
     * Zpracuje neúspěšnou autentizaci.
     *
     * Vrací HTTP status 401 a JSON odpověď s chybovou zprávou.
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

        if (failed.getCause() instanceof AccountNotActivatedException) {
            result.put("message", failed.getCause().getMessage());
        } else if (failed instanceof BadCredentialsException) {
            result.put("message", "BE - Neplatné přihlašovací údaje");
        } else {
            result.put("message", "BE - Chyba při přihlášení");
        }
        failed.printStackTrace();
        objectMapper.writeValue(response.getWriter(), result);
    }
}