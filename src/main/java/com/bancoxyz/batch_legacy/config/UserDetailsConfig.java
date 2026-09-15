package com.bancoxyz.batch_legacy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserDetailsConfig {

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails userWeb = User.builder()
            .username("cliente_web")
            .password(passwordEncoder.encode("web123"))
            .roles("WEB").build();
            
        UserDetails userMobile = User.builder()
            .username("cliente_movil")
            .password(passwordEncoder.encode("movil123"))
            .roles("MOBILE").build();
            
        UserDetails userCajero = User.builder()
            .username("cliente_cajero")
            .password(passwordEncoder.encode("cajero123"))
            .roles("CAJERO").build();

        return new InMemoryUserDetailsManager(userWeb, userMobile, userCajero);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}