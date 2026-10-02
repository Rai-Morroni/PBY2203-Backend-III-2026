package com.bancoxyz.cajero.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import java.util.concurrent.atomic.AtomicInteger;
import com.bancoxyz.cajero.exception.OperationLimitExceededException;

import com.bancoxyz.cajero.dto.CajeroOperacionDTO;
import com.bancoxyz.cajero.dto.RetiroRequestDTO;
import com.bancoxyz.cajero.dto.TransaccionResponseDTO;

@RestController
@RequestMapping("/api/cajero")
public class CajeroBffController {

    private final RestClient restClient;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC_RETIROS = "cajero-retiros-topic";

    // Contadores atómicos para los límites de operaciones
    private final AtomicInteger contadorConsultas = new AtomicInteger(0);
    private static final int LIMITE_CONSULTAS = 3; // Límite de consultas de saldo por sesión

    private final AtomicInteger contadorRetiros = new AtomicInteger(0);
    private static final int LIMITE_RETIROS = 1; // Límite de retiro por sesión

    public CajeroBffController() {
        this.restClient = RestClient.builder().baseUrl("http://localhost:8081").build();
    }

    // 1. Tolerancia a Fallos: Circuit Breaker y contrato DTO
    @GetMapping("/saldo")
    @CircuitBreaker(name = "coreServiceCB", fallbackMethod = "fallbackSaldo")
    public ResponseEntity<CajeroOperacionDTO> consultarSaldoSeguro() {
        
        // Control de límite de consultas
        if (contadorConsultas.incrementAndGet() > LIMITE_CONSULTAS) {
            throw new OperationLimitExceededException("Ha excedido el límite de " + LIMITE_CONSULTAS + " consultas diarias de saldo.");
        }

        Double saldo = restClient.get()
                .uri("/api/internal/core/saldo")
                .retrieve()
                .body(Double.class);

        int intentosRestantes = Math.max(0, LIMITE_CONSULTAS - contadorConsultas.get());

        CajeroOperacionDTO response = new CajeroOperacionDTO(
                "Cajero Automático",
                "Consulta de Saldo Seguro",
                saldo != null ? saldo : 0.0,
                intentosRestantes 
        );
        
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<CajeroOperacionDTO> fallbackSaldo(Throwable t) {
        int intentosRestantes = Math.max(0, LIMITE_CONSULTAS - contadorConsultas.get());

        CajeroOperacionDTO fallbackData = new CajeroOperacionDTO(
                "Cajero Automático - MODO DEGRADADO",
                "Servicio principal no disponible (Circuit Breaker Abierto)",
                0.0,
                intentosRestantes
        );
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackData);
    }

    // 2. Arquitectura de Eventos: Endpoint protegido con @Valid y DTO de entrada
    @PostMapping("/retiro")
    public ResponseEntity<TransaccionResponseDTO> procesarRetiroAsincrono(
            @Valid @RequestBody RetiroRequestDTO request) {
        
        // Control de límite de retiros
        if (contadorRetiros.incrementAndGet() > LIMITE_RETIROS) {
            throw new OperationLimitExceededException("Ha excedido el límite de " + LIMITE_RETIROS + " retiro diario permitido.");
        }

        String eventoJson = String.format("{\"cuentaId\":\"%s\", \"monto\":%s, \"operacion\":\"RETIRO\"}", 
                request.cuentaId(), request.monto());

        // Publica el evento en Kafka
        kafkaTemplate.send(TOPIC_RETIROS, eventoJson);

        TransaccionResponseDTO response = new TransaccionResponseDTO(
                "Transacción en proceso. El retiro se confirmará asincrónicamente."
        );
        return ResponseEntity.ok(response);
    }
}