package com.bancoxyz.batch_legacy.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import com.bancoxyz.batch_legacy.dto.CajeroOperacionDTO;
import com.bancoxyz.batch_legacy.exception.OperationLimitExceededException;

import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/cajero")
public class CajeroBffController {

    private final RestClient restClient;
    
    // Control de límite de operaciones críticas por usuario autenticado
    private final ConcurrentHashMap<String, Integer> sesionesUsuario = new ConcurrentHashMap<>();
    private static final int MAX_OPERACIONES = 3;

    public CajeroBffController() {
        this.restClient = RestClient.create();
    }

    @GetMapping("/operaciones")
    public CajeroOperacionDTO getAtmOperaciones() {
        // 1. Identificar al usuario desde el contexto de seguridad JWT
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Validación estricta de límite de sesión para Cajeros
        int realizadas = sesionesUsuario.getOrDefault(username, 0);
        if (realizadas >= MAX_OPERACIONES) {
            throw new OperationLimitExceededException(
                    "Límite de operaciones críticas por sesión superado. Tarjeta bloqueada por seguridad.");
        }
        
        // Incrementar contador de operaciones
        sesionesUsuario.put(username, realizadas + 1);

        // 3. Delegación HTTP al servicio Core
        Double saldo = restClient.get()
            .uri("http://127.0.0.1:8080/api/internal/core/saldo")
                .retrieve()
                .body(Double.class);

        double saldoSeguro = (saldo != null) ? saldo : 0.0;

        // 4. Mapeo y respuesta mediante DTO
        return new CajeroOperacionDTO(
                "Cajero Automático (ATM)",
                "Consulta de Saldo Seguro",
                saldoSeguro,
                MAX_OPERACIONES - (realizadas + 1)
        );
    }
}