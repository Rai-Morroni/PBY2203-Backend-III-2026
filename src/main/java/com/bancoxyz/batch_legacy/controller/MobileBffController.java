package com.bancoxyz.batch_legacy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/mobile") // Ruta base para el BFF Móvil
public class MobileBffController {

    @GetMapping("/resumen") // Endpoint para obtener datos resumidos para la app móvil
    public Map<String, Object> getMobileResumen() {
        // BFF Móvil: Respuestas ligeras y datos esenciales
        Map<String, Object> response = new HashMap<>();
        
        // Simulando un payload mínimo para optimizar el ancho de banda
        response.put("canal", "App Móvil");
        response.put("saldoDisponible", 1500000.00);
        response.put("ultimaTransaccion", "-$15.000 (Supermercado)");
        
        return response;
    }
}