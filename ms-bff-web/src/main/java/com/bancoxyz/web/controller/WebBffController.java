package com.bancoxyz.web.controller;

import org.springframework.core.ParameterizedTypeReference;
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

    public WebBffController() {
        this.restClient = RestClient.create();
    }

    @GetMapping("/dashboard")
    public WebDashboardDTO getWebDashboard() {
        // Comunicación HTTP síncrona hacia la capa interna/microservicio
        List<CuentaAnualDTO> historial = restClient.get()
            .uri("http://127.0.0.1:8081/api/internal/core/historial")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CuentaAnualDTO>>() {});

        List<CuentaAnualDTO> seguroHistorial = (historial != null) ? historial : List.of();

        // Transformación y entrega del payload estructurado
        return new WebDashboardDTO(
                "Portal Web Desktop",
                "Juan Pérez",
                seguroHistorial.size(),
                seguroHistorial
        );
    }
}