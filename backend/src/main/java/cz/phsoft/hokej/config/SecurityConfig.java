package cz.phsoft.hokej.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Hlavní konfigurace Spring Security pro backend aplikace.
 *
 * ZODPOVĚDNOST:
 * <ul>
 *     <li>nastavení autentizace (CustomUserDetailsService + BCrypt),</li>
 *     <li>nastavení autorizace endpointů (role / přihlášení),</li>
 *     <li>konfigurace login mechanismu (CustomJsonLoginFilter),</li>
 *     <li>session management (stateful přístup),</li>
 *     <li>CORS konfigurace pro SPA frontend (React / Vite).</li>
 * </ul>
 *
 * REŽIMY PROVOZU:
 * <ul>
 *     <li><b>test-mode = true</b> → vše povoleno, HTTP Basic (Postman, vývoj),</li>
 *     <li><b>test-mode = false</b> → produkční režim, REST login + role.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // povolí @PreAuthorize, @Secured, @RolesAllowed
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * Přepínač testovacího režimu:
     *
     * app.test-mode=true
     *
     * Použití:
     * - lokální vývoj
     * - Postman bez řešení session / loginu
     */
    @Value("${app.test-mode:false}")
    private boolean isTestMode;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // =====================================================
    // 1) PASSWORD ENCODER
    // =====================================================

    /**
     * BCrypt encoder pro ukládání a ověřování hesel.
     *
     * Používá se:
     * - při registraci
     * - při změně hesla
     * - při autentizaci (login)
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =====================================================
    // 2) AUTHENTICATION PROVIDER
    // =====================================================

    /**
     * DaoAuthenticationProvider:
     *
     * - načítá uživatele z databáze pomocí CustomUserDetailsService
     * - ověřuje heslo pomocí BCryptPasswordEncoder
     *
     * Používá se při loginu přes AuthenticationManager.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // =====================================================
    // 3) AUTHENTICATION MANAGER
    // =====================================================

    /**
     * AuthenticationManager:
     *
     * - centrální bod autentizace ve Spring Security
     * - používá se v CustomJsonLoginFilter
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // =====================================================
    // 4) SECURITY FILTER CHAIN
    // =====================================================

    /**
     * Hlavní bezpečnostní konfigurace HTTP vrstvy.
     *
     * Řeší:
     * <ul>
     *     <li>CSRF / CORS</li>
     *     <li>autorizaci endpointů</li>
     *     <li>login (CustomJsonLoginFilter)</li>
     *     <li>logout</li>
     *     <li>session management</li>
     * </ul>
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authManager) throws Exception {

        // REST API → CSRF vypnuto (řešeno přes session + CORS)
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> { /* používá corsConfigurationSource() */ });

        if (isTestMode) {
            // =================================================
            // TEST MODE
            // =================================================
            //
            // - všechny endpointy povoleny
            // - HTTP Basic autentizace
            // - žádný JSON login filtr
            //
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .httpBasic();

        } else {
            // =================================================
            // PRODUKČNÍ REŽIM
            // =================================================

            http
                    // napojení na CustomUserDetailsService + BCrypt
                    .authenticationProvider(authenticationProvider())

                    // -------------------------------
                    // AUTORIZACE ENDPOINTŮ
                    // -------------------------------
                    .authorizeHttpRequests(auth -> auth

                            // ===== VEŘEJNÉ ENDPOINTY =====
                            .requestMatchers(
                                    "/api/auth/register",
                                    "/api/auth/verify",
                                    "/api/auth/login",
                                    "/api/auth/logout"
                            ).permitAll()

                            // ===== DEBUG / TEST =====
                            .requestMatchers(
                                    "/api/debug/me",
                                    "/api/test/**"
                            ).hasRole("ADMIN")

                            // testovací emaily
                            .requestMatchers("/api/email/test/**").hasRole("ADMIN")

                            // ===== ADMIN / MANAGER =====
                            .requestMatchers("/api/admin/seasons/**").hasRole("ADMIN")
                            .requestMatchers("/api/matches/admin/**").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers("/api/players/admin/**").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers("/api/registrations/admin/**").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers("/api/inactivity/admin/**").hasAnyRole("ADMIN", "MANAGER")

                            // ===== ZBYTEK API =====
                            .requestMatchers("/api/**").authenticated()

                            .anyRequest().authenticated()
                    )

                    // -------------------------------
                    // SESSION MANAGEMENT
                    // -------------------------------
                    .sessionManagement(sm ->
                            sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    )

                    // -------------------------------
                    // CUSTOM JSON LOGIN
                    // -------------------------------
                    .addFilterAt(
                            new CustomJsonLoginFilter("/api/auth/login", authManager),
                            UsernamePasswordAuthenticationFilter.class
                    )

                    // -------------------------------
                    // LOGOUT
                    // -------------------------------
                    .logout(logout -> logout
                            .logoutUrl("/api/auth/logout")
                            .deleteCookies("JSESSIONID")
                            .logoutSuccessHandler((request, response, auth) -> {
                                request.getSession().removeAttribute("CURRENT_PLAYER_ID");
                                request.getSession().removeAttribute("CURRENT_SEASON_ID");
                                request.getSession().removeAttribute("CURRENT_SEASON_CUSTOM");
                                response.setContentType("application/json");
                                response.setCharacterEncoding("UTF-8");
                                response.getWriter()
                                        .write("{\"status\":\"ok\",\"message\":\"Odhlášeno\"}");
                            })
                    );
        }

        return http.build();
    }

    // =====================================================
    // 5) CORS KONFIGURACE
    // =====================================================

    /**
     * CORS konfigurace pro SPA frontend (React / Vite).
     *
     * - povoluje http://localhost:5173
     * - umožňuje cookies (JSESSIONID)
     * - povoluje běžné HTTP metody
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
