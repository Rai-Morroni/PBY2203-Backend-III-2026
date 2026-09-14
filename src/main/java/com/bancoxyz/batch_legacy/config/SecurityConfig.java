package com.bancoxyz.batch_legacy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .authorizeHttpRequests(auth -> auth
                // Autorización específica por cada canal BFF
                .requestMatchers("/api/web/**").hasRole("WEB")
                .requestMatchers("/api/mobile/**").hasRole("MOBILE")
                .requestMatchers("/api/cajero/**").hasRole("CAJERO")
                .anyRequest().authenticated()
            )
            .httpBasic(withDefaults()); // Habilita el envío de tokens de autenticación básicos
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // Autenticación diferenciada para cada cliente
        UserDetails userWeb = User.builder()
            .username("cliente_web")
            .password(passwordEncoder.encode("web123"))
            .roles("WEB")
            .build();

        UserDetails userMobile = User.builder()
            .username("cliente_movil")
            .password(passwordEncoder.encode("movil123"))
            .roles("MOBILE")
            .build();

        UserDetails userCajero = User.builder()
            .username("cliente_cajero")
            .password(passwordEncoder.encode("cajero123"))
            .roles("CAJERO")
            .build();

        return new InMemoryUserDetailsManager(userWeb, userMobile, userCajero);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}