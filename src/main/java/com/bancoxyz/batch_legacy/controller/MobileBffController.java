package com.bancoxyz.batch_legacy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import com.bancoxyz.batch_legacy.dto.MobileResumenDTO;

@RestController
@RequestMapping("/api/mobile")
public class MobileBffController {

    // Cliente HTTP moderno para comunicación entre microservicios
    private final RestClient restClient;

    public MobileBffController() {
        this.restClient = RestClient.create();
    }

    @GetMapping("/resumen")
    public MobileResumenDTO getMobileResumen() {
        // 1. Delegación de responsabilidad: Llamada HTTP al microservicio Core interno
        Long total = restClient.get()
            .uri("http://127.0.0.1:8080/api/internal/core/transacciones/count")
                .retrieve()
                .body(Long.class);
        
        long safeTotal = (total != null) ? total : 0;

        // 2. Transformación y Mapeo selectivo mediante DTO
        return new MobileResumenDTO(
                "App Móvil", 
                "Datos ligeros cargados", 
                safeTotal
        );
    }
}