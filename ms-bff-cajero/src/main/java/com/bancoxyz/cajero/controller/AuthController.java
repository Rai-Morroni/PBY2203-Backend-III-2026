package com.bancoxyz.cajero.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.bancoxyz.cajero.dto.LoginRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RestClient authServerClient;

    public AuthController(
            RestClient.Builder restClientBuilder,
            @Value("${bank.auth-server-base-url:http://localhost:9000}") String authServerBaseUrl) {
        this.authServerClient = restClientBuilder.baseUrl(authServerBaseUrl).build();
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        TokenResponse response = authServerClient.post()
                .uri("/api/auth/login")
                .body(request)
                .retrieve()
                .body(TokenResponse.class);

        if (response == null || response.token() == null || response.token().isBlank()) {
            throw new IllegalStateException("Auth server returned an empty token response");
        }
        return Map.of("token", response.token());
    }

    private record TokenResponse(String token) {
    }

    @ExceptionHandler(HttpClientErrorException.Unauthorized.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "error", "invalid_grant",
                        "error_description", "Credenciales inválidas"));
    }
}
