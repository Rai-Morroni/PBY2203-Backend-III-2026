package com.bancoxyz.batch_legacy.controller;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import com.bancoxyz.batch_legacy.dto.WebDashboardDTO;
import com.bancoxyz.batch_legacy.model.CuentaAnualEntity;

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
        List<CuentaAnualEntity> historial = restClient.get()
            .uri("http://127.0.0.1:8080/api/internal/core/historial")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CuentaAnualEntity>>() {});

        List<CuentaAnualEntity> seguroHistorial = (historial != null) ? historial : List.of();

        // Transformación y entrega del payload estructurado
        return new WebDashboardDTO(
                "Portal Web Desktop",
                "Juan Pérez",
                seguroHistorial.size(),
                seguroHistorial
        );
    }
}