package com.bancoxyz.web.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import com.bancoxyz.web.dto.CuentaAnualDTO;
import com.bancoxyz.web.dto.WebDashboardDTO;

import java.util.List;

@RestController
@RequestMapping("/api/web")
public class WebBffController {

    private final RestClient restClient;

    public WebBffController(
            RestClient.Builder restClientBuilder,
            @Value("${bank.services.transacciones.base-url}") String transaccionesServiceBaseUrl) {
        this.restClient = restClientBuilder.baseUrl(transaccionesServiceBaseUrl).build();
    }

    @GetMapping("/dashboard")
    @CircuitBreaker(name = "transaccionesServiceCB", fallbackMethod = "fallbackDashboard")
    public ResponseEntity<WebDashboardDTO> getWebDashboard() {
        // Comunicación HTTP síncrona hacia la capa interna/microservicio
        List<CuentaAnualDTO> historial = restClient.get()
                .uri("/api/internal/transacciones/historial")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CuentaAnualDTO>>() {});

        List<CuentaAnualDTO> seguroHistorial = (historial != null) ? historial : List.of();

        // Transformación y entrega del payload estructurado
        WebDashboardDTO dashboard = new WebDashboardDTO(
                "Portal Web Desktop",
                "Juan Pérez",
                seguroHistorial.size(),
                seguroHistorial
        );
        
        // Contrato claro devolviendo HTTP 200 OK con el DTO
        return ResponseEntity.ok(dashboard); 
    }

    // Método Fallback que mantiene el contrato del DTO pero alerta de la degradación
    public ResponseEntity<WebDashboardDTO> fallbackDashboard(Throwable t) {
        WebDashboardDTO fallbackData = new WebDashboardDTO(
                "Portal Web Desktop",
                "Servicio degradado temporalmente (Circuit Breaker Abierto)",
                0,
                List.of()
        );
        // Retorna HTTP 503 Service Unavailable para no enmascarar el error
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackData); 
    }
}