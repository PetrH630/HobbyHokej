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
import org.springframework.security.config.http.SessionCreationPolicy;


import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Value("${app.test-mode:false}")
    private boolean isTestMode;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // Password encoder
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Authentication provider
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Security filter chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .cors(cors -> {
                });

        if (isTestMode) {
            // Test mode - všechno povoleno a HTTP Basic pro Postman
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .httpBasic();
        } else {
            // Produkce - REST login přes CustomJsonLoginFilter
            http
                    .authenticationProvider(authenticationProvider())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/auth/register").permitAll()
                            .requestMatchers("/api/login").permitAll()
                            .requestMatchers("/api/logout").permitAll()
                            .requestMatchers("/api/matches").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers("/api/matches/upcoming", "/api/matches/past").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers("/api/matches/**").authenticated()
                            .requestMatchers("/api/players").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers("/api/players/**").authenticated()
                            .requestMatchers("/api/registrations/all",
                                    "/api/registrations/for-match/**",
                                    "/api/registrations/no-response/**").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers("/api/registrations/**").authenticated()
                            .requestMatchers("/api/inactivity/All",
                                    "/api/inactivity/**").hasAnyRole("ADMIN", "MANAGER")
                            .requestMatchers("/api/inactivity/player/**").authenticated()
                            .anyRequest().authenticated()
                    )
                    //  TADY PŘESNĚ
                    .sessionManagement(sm ->
                            sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    )
                    //  a TEPRVE PAK login filter
                    .addFilterAt(
                            new CustomJsonLoginFilter("/api/login", authManager),
                            UsernamePasswordAuthenticationFilter.class
                    )
                    .logout(logout -> logout
                            .logoutUrl("/api/logout")
                            .deleteCookies("JSESSIONID")
                            .logoutSuccessHandler((request, response, auth) -> {
                                response.setContentType("application/json");
                                response.setCharacterEncoding("UTF-8");
                                response.getWriter().write("{\"status\":\"ok\",\"message\":\"Odhlášeno\"}");
                            })
                    );
        }

        return http.build();
    }

    // CORS pro React dev server a cookies
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
