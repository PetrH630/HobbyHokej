package cz.phsoft.hokej.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// filtr pro REST login
public class CustomJsonLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomJsonLoginFilter(String loginUrl, AuthenticationManager authManager) {
        setFilterProcessesUrl(loginUrl);
        setAuthenticationManager(authManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // Podporujeme x-www-form-urlencoded i JSON
            String email = null;
            String password = null;

// Podporujeme x-www-form-urlencoded
            if ("application/x-www-form-urlencoded".equals(request.getContentType())) {
                email = request.getParameter("username");
                password = request.getParameter("password");
            }

// Podporujeme JSON
            if ((email == null || password == null) &&
                    request.getContentType() != null &&
                    request.getContentType().contains("application/json")) {
                Map<String, String> json = objectMapper.readValue(request.getInputStream(), Map.class);
                email = json.get("email");
                password = json.get("password");
            }


            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(email, password);
            setDetails(request, authRequest);

            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult)
            throws IOException, ServletException {

        // Uložení do SecurityContextHolder
        SecurityContextHolder.getContext().setAuthentication(authResult);

        // Vytvoření session a uložení SPRING_SECURITY_CONTEXT
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

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("status", "error");
        result.put("message", "Neplatné přihlašovací údaje");

        objectMapper.writeValue(response.getWriter(), result);
    }
}
