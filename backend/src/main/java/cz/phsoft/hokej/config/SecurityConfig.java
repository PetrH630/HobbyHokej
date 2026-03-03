package cz.phsoft.hokej.config;

import cz.phsoft.hokej.player.repositories.PlayerRepository;
import cz.phsoft.hokej.user.services.AppUserService;
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

import java.util.List;

/**
 * Hlavní konfigurace Spring Security pro backend aplikace.
 *
 * V této třídě se konfiguruje autentizace uživatelů, autorizace endpointů,
 * správa HTTP session, CORS politika a napojení vlastního login filtru.
 * Třída představuje centrální bezpečnostní konfiguraci celé aplikace
 * a propojuje bezpečnostní infrastrukturu se service vrstvou.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PlayerRepository playerRepository;

    /**
     * Příznak demo režimu aplikace.
     *
     * Pokud je hodnota nastavena na true, mohou být některé operace
     * omezeny nebo zpřístupněny pouze v demonstračním režimu.
     */
    @Value("${app.demo-mode:false}")
    private boolean isDemoMode;

    /**
     * Seznam povolených originů pro CORS.
     *
     * Hodnota se načítá z aplikační konfigurace a může obsahovat
     * více domén oddělených čárkou.
     */
    @Value("${app.cors.allowed-origins:http://localhost:5173,https://hokej.phsoft.cz}")
    private String allowedOrigins;

    /**
     * Vytváří instanci bezpečnostní konfigurace.
     *
     * @param userDetailsService služba pro načítání uživatele z databáze
     * @param playerRepository   repozitář hráčů používaný v rámci bezpečnostní logiky
     */
    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          PlayerRepository playerRepository) {
        this.userDetailsService = userDetailsService;
        this.playerRepository = playerRepository;
    }

    /**
     * Registruje PasswordEncoder používaný pro hashování hesel.
     *
     * Používá se algoritmus BCrypt, který je doporučený pro produkční
     * použití v rámci Spring Security.
     *
     * @return instance PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Konfiguruje DaoAuthenticationProvider.
     *
     * Provider propojuje Spring Security s implementací UserDetailsService
     * a zajišťuje ověřování hesel pomocí PasswordEncoder.
     *
     * @param passwordEncoder encoder pro porovnávání hesel
     * @return nakonfigurovaný DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * Zpřístupňuje AuthenticationManager z konfigurace Spring Security.
     *
     * @param authConfig konfigurace autentizace
     * @return instance AuthenticationManager
     * @throws Exception pokud dojde k chybě při získávání manageru
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Hlavní konfigurace bezpečnostního filtračního řetězce.
     *
     * V této metodě se nastavují pravidla autorizace endpointů,
     * správa session, registrace vlastního login filtru a logout logika.
     *
     * @param http              konfigurace HttpSecurity
     * @param authManager       authentication manager
     * @param appUserService    služba pro práci s uživateli
     * @param authProvider      poskytovatel autentizace
     * @return nakonfigurovaný SecurityFilterChain
     * @throws Exception při chybě konfigurace
     */
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

                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

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

                    if (isDemoMode) {
                        auth.requestMatchers("/api/demo/notifications/**").permitAll();
                    }

                    auth.requestMatchers("/api/admin/seasons/**").hasRole("ADMIN");
                    auth.requestMatchers("/api/email/test/**").hasRole("ADMIN");
                    auth.requestMatchers("/api/debug/me").hasRole("ADMIN");
                    auth.requestMatchers("/api/test/**").hasRole("ADMIN");

                    auth.requestMatchers("/api/matches/admin/**").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers("/api/players/admin/**").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers("/api/registrations/admin/**").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers("/api/inactivity/admin/**").hasAnyRole("ADMIN", "MANAGER");
                    auth.requestMatchers("/api/notifications/admin/**").hasAnyRole("ADMIN", "MANAGER");

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

    /**
     * Konfiguruje CORS politiku aplikace.
     *
     * Povolené originy se načítají z aplikační konfigurace.
     * Povolené jsou standardní HTTP metody používané REST API.
     *
     * @return konfigurace CORS pro celý backend
     */
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