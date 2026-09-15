package com.bancoxyz.batch_legacy.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bancoxyz.batch_legacy.config.JwtUtil;
import com.bancoxyz.batch_legacy.model.LoginRequest;

@RestController
@RequestMapping("/api/auth") // Ruta base para la autenticación. Único endpoint público que no requerirá token, ya que su trabajo es generarlo.
public class AuthController {

    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserDetailsService userDetailsService;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/login") // Endpoint para el login
    public Map<String, String> login(@RequestBody LoginRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        
        if (passwordEncoder.matches(request.password(), userDetails.getPassword())) { // Se compara la contraseña ingresada con la almacenada en la base de datos
            String token = jwtUtil.generateToken(userDetails);
            return Map.of("token", token);
        }
        throw new RuntimeException("Credenciales inválidas");
    }
}