package com.bancoxyz.cajero.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/cajero")
public class CajeroBffController {

    private final RestClient restClient;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC_RETIROS = "cajero-retiros-topic";

    public CajeroBffController() {
        this.restClient = RestClient.builder().baseUrl("http://localhost:8081").build();
    }

    // 1. Tolerancia a Fallos: Circuit Breaker para operaciones síncronas de lectura
    @GetMapping("/saldo")
    @CircuitBreaker(name = "coreServiceCB", fallbackMethod = "fallbackSaldo")
    public String consultarSaldoSeguro() {
        // Llama al ms-core a través de Eureka usando el nombre del servicio
        Double saldo = restClient.get()
                .uri("/api/internal/core/saldo")
                .retrieve()
                .body(Double.class);
        return "Saldo actual: $" + saldo;
    }

    // Método Fallback: Se ejecuta si MS-CORE se cae, evidenciando resiliencia
    public String fallbackSaldo(Throwable t) {
        return "Servicio principal no disponible (Circuit Breaker Abierto). Mostrando saldo en caché: $0.0";
    }

    // 2. Arquitectura Orientada a Eventos: Envío asíncrono de transacción (Patrón Saga)
    @PostMapping("/retiro")
    public String procesarRetiroAsincrono(@RequestParam String cuentaId, @RequestParam Double monto) {
        String eventoJson = String.format("{\"cuentaId\":\"%s\", \"monto\":%s, \"operacion\":\"RETIRO\"}", cuentaId, monto);
        
        // Publica el evento en Kafka. El BFF no espera a la base de datos.
        kafkaTemplate.send(TOPIC_RETIROS, eventoJson);
        
        return "Transacción en proceso. El retiro se confirmará asincrónicamente.";
    }
}