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
 * Třída zajišťuje nastavení autentizace, autorizace endpointů,
 * login mechanizmu, správy session a CORS. Konfigurace podporuje
 * testovací režim pro vývoj a produkční režim s REST loginem
 * a rolově řízeným přístupem.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * Příznak testovacího režimu.
     *
     * Pokud je {@code app.test-mode=true}, aplikace povoluje všechny
     * endpointy a používá HTTP Basic autentizaci. Režim je určen pro
     * lokální vývoj a testování v Postmanu bez potřeby řešit session.
     */
    @Value("${app.test-mode:false}")
    private boolean isTestMode;

    /**
     * Příznak demo režimu.
     *
     * Pokud je {@code app.demo-mode=true}, aplikace běží se stejným
     * security nastavením jako v produkci, ale některé operace
     * (např. změna hesla, mazání dat) mohou být na úrovni service
     * blokované.
     */
    @Value("${app.demo-mode:false}")
    private boolean isDemoMode;


    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // Password encoder

    /**
     * Encoder pro hashování a ověřování hesel pomocí BCrypt.
     *
     * Používá se při registraci, změně hesla i při samotné autentizaci.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Authentication provider

    /**
     * {@link DaoAuthenticationProvider} pro načítání uživatelů z databáze.
     *
     * Poskytuje napojení na {@link CustomUserDetailsService} a
     * {@link BCryptPasswordEncoder}. Používá se při loginu přes
     * {@link AuthenticationManager}.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // Authentication manager

    /**
     * Vytváří {@link AuthenticationManager} používaný při autentizaci.
     *
     * Manager je předáván do {@link CustomJsonLoginFilter}.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Security filter chain

    /**
     * Hlavní bezpečnostní konfigurace HTTP vrstvy.
     *
     * Řeší:
     * - vypnutí CSRF pro REST API,
     * - povolení CORS pro SPA frontend,
     * - autorizaci endpointů,
     * - login (CustomJsonLoginFilter),
     * - logout a správu session.
     *
     * V testovacím režimu povoluje všechny endpointy s HTTP Basic
     * autentizací. V produkčním režimu aplikuje detailní pravidla
     * pro jednotlivé endpointy a používá JSON login.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authManager) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .cors(cors -> {
                    // Použije corsConfigurationSource()
                });

        if (isTestMode) {
            // Testovací režim: vše povoleno, HTTP Basic

            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .httpBasic();

        }
        else {
            // Produkční režim

            http
                    .authenticationProvider(authenticationProvider())

                    .authorizeHttpRequests(auth -> {

                        // Veřejné endpointy
                        auth.requestMatchers(
                                "/api/auth/register",
                                "/api/auth/verify",
                                "/api/auth/login",
                                "/api/auth/logout",
                                "/api/auth/forgotten-password",
                                "/api/auth/forgotten-password/info",
                                "/api/auth/forgotten-password/reset",
                                "/api/auth/reset-password",
                                "/error",
                                "/favicon.ico",
                                "/public/**",
                                "/api/inactivity/admin/me/**"
                        ).permitAll();

                        // DEMO režim: umožní číst demo notifikace bez přihlášení
                        if (isDemoMode) {
                            auth.requestMatchers("/api/demo/notifications/**").permitAll();

                        }

                        // Debug a test
                        auth.requestMatchers(
                                "/api/debug/me",
                                "/api/test/**"
                        ).hasRole("ADMIN");

                        // Testovací e-maily
                        auth.requestMatchers("/api/email/test/**").hasRole("ADMIN");

                        // Admin / manager endpointy
                        auth.requestMatchers("/api/admin/seasons/**").hasRole("ADMIN");
                        auth.requestMatchers("/api/matches/admin/**").hasAnyRole("ADMIN", "MANAGER");
                        auth.requestMatchers("/api/players/admin/**").hasAnyRole("ADMIN", "MANAGER");
                        auth.requestMatchers("/api/registrations/admin/**").hasAnyRole("ADMIN", "MANAGER");
                        auth.requestMatchers("/api/inactivity/admin/**").hasAnyRole("ADMIN", "MANAGER");

                        // Zbytek API pouze pro přihlášené
                        auth.requestMatchers("/api/**").authenticated();
                        auth.anyRequest().authenticated();
                    })

                    .sessionManagement(sm ->
                            sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    )

                    .addFilterAt(
                            new CustomJsonLoginFilter("/api/auth/login", authManager),
                            UsernamePasswordAuthenticationFilter.class
                    )

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

    // CORS konfigurace

    /**
     * CORS konfigurace pro SPA frontend (React / Vite).
     *
     * Povoluje původ {@code http://localhost:5173}, základní HTTP metody
     * a hlavičky. Umožňuje přenos cookies (JSESSIONID) pro session-based
     * autentizaci.
     *
     * V produkci se má konfigurace doplnit o produkční domény.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
