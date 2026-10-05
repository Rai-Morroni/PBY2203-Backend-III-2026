package com.bancoxyz.mobile.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import com.bancoxyz.mobile.dto.MobileResumenDTO;

@RestController
@RequestMapping("/api/mobile")
public class MobileBffController {

    private final RestClient restClient;

    public MobileBffController() {
        // Apunta al microservicio de negocio
        this.restClient = RestClient.builder().baseUrl("http://localhost:8081").build();
    }

    @GetMapping("/resumen")
    @CircuitBreaker(name = "coreServiceCB", fallbackMethod = "fallbackResumen")
    public ResponseEntity<MobileResumenDTO> getResumen() {
        // Consumo real (Aquí iría la llamada restClient.get()...)
        MobileResumenDTO resumen = new MobileResumenDTO("App Móvil", "Datos cargados", 150);
        return ResponseEntity.ok(resumen); // HTTP 200 OK
    }

    // El Fallback debe mantener la misma firma y retornar el mismo tipo de DTO
    public ResponseEntity<MobileResumenDTO> fallbackResumen(Throwable t) {
        MobileResumenDTO fallbackData = new MobileResumenDTO("App Móvil", "Servicio temporalmente degradado", 0);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackData); // HTTP 503
    }
}