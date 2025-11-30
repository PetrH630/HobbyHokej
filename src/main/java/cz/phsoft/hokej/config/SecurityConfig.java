package cz.phsoft.hokej.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // vypnout CSRF pro testování
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // všechno volně
        return http.build();
    }
}

/*
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // vypnout CSRF pro Postman testy
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/players/**").permitAll() // hráči volně
                        .anyRequest().authenticated() // ostatní endpointy vyžadují autentizaci
                )
                .httpBasic(httpBasic -> {}); // správná syntaxe v SS 6
        return http.build();
    }
}
*/