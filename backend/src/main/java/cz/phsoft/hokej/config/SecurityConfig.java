package cz.phsoft.hokej.config;

import cz.phsoft.hokej.player.repositories.PlayerRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import cz.phsoft.hokej.user.services.AppUserService;

import java.util.List;

/**
 * Hlavní konfigurace Spring Security pro backend aplikace.
 *
 * Třída zajišťuje nastavení autentizace, autorizace endpointů,
 * login mechanizmu, správy session a CORS.
 * Používá produkční JSON login a rolově řízený přístup.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PlayerRepository playerRepository;

    /**
     * Příznak demo režimu.
     *
     * Pokud je {@code app.demo-mode=true}, některé operace mohou být
     * omezeny na úrovni service vrstvy.
     */
    @Value("${app.demo-mode:false}")
    private boolean isDemoMode;

    /**
     * Seznam povolených originů pro CORS.
     *
     * Příklad:
     * app.cors.allowed-origins=http://localhost:5173,https://hokej.phsoft.cz
     */
    @Value("${app.cors.allowed-origins:http://localhost:5173,https://hokej.phsoft.cz}")
    private String allowedOrigins;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          PlayerRepository playerRepository) {
        this.userDetailsService = userDetailsService;
        this.playerRepository = playerRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authManager,
                                                   AppUserService appUserService,
                                                   DaoAuthenticationProvider authProvider) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .authenticationProvider(authProvider)

                .authorizeHttpRequests(auth -> {

                    /*
                     * Povolení CORS preflight requestů.
                     */
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                    /*
                     * Veřejné endpointy.
                     */
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

                    /*
                     * DEMO režim: umožní číst demo notifikace bez přihlášení.
                     */
                    if (isDemoMode) {
                        auth.requestMatchers("/api/demo/notifications/**").permitAll();
                    }

                    /*
                     * Admin-only endpointy.
                     */
                    auth.requestMatchers("/api/admin/seasons/**").hasRole("ADMIN");
                    auth.requestMatchers("/api/email/test/**").hasRole("ADMIN");
                    auth.requestMatchers("/api/debug/me").hasRole("ADMIN");
                    auth.requestMatchers("/api/test/**").hasRole("ADMIN");

                    /*
                     * Admin + Manager endpointy.
                     */
                    auth.requestMatchers("/api/matches/admin/**").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers("/api/players/admin/**").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers("/api/registrations/admin/**").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers("/api/inactivity/admin/**").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers("/api/notifications/admin/**").hasAnyRole("ADMIN", "MANAGER");

                    /*
                     * Zbytek API pouze pro přihlášené.
                     */
                    auth.requestMatchers("/api/**").authenticated();
                    auth.anyRequest().authenticated();
                })

                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                .addFilterAt(
                        new CustomJsonLoginFilter("/api/auth/login", authManager, appUserService),
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

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = List.of(allowedOrigins.split("\\s*,\\s*"));
        configuration.setAllowedOrigins(origins);

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
                List.of("Content-Type", "Authorization", "X-Requested-With")
        );

        configuration.setAllowCredentials(true);

        configuration.setExposedHeaders(List.of("Set-Cookie"));

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}