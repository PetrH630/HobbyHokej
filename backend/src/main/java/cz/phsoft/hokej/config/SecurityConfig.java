package cz.phsoft.hokej.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableMethodSecurity // aktivuje @PreAuthorize
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Value("${app.test-mode:false}")
    private boolean isTestMode;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;

    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        if (isTestMode) {
            // --- Testovací režim: všechno volně pro Postman ---
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        } else {
            // --- Produkční režim: autentizace + role + Basic Auth + FormLogin ---
            http
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            // MATCH
                            .requestMatchers("/api/matches").hasAnyRole("ADMIN","MANAGER")
                            .requestMatchers("/api/matches/upcoming", "/api/matches/past").hasAnyRole("ADMIN","MANAGER")
                            .requestMatchers("/api/matches/**").authenticated()

                            // PLAYER
                            .requestMatchers("/api/players").hasAnyRole("ADMIN","MANAGER")
                            .requestMatchers("/api/players/**").authenticated()

                            // REGISTRATIONS
                            .requestMatchers("/api/registrations/all",
                                    "/api/registrations/for-match/**",
                                    "/api/registrations/no-response/**")
                            .hasAnyRole("ADMIN","MANAGER")
                            .requestMatchers("/api/registrations/**").authenticated()

                            // INACTIVITY
                            .requestMatchers("/api/inactivity/All",
                                    "/api/inactivity/**")
                            .hasAnyRole("ADMIN","MANAGER")
                            .requestMatchers("/api/inactivity/player/**").authenticated()

                            .anyRequest().authenticated()
                    )
                    .formLogin(form -> form
                            .loginPage("/login")
                            .permitAll()
                    )
                    .httpBasic(httpBasic -> {}); // 🔥 JSON 403
        }

        return http.build();
    }
}
