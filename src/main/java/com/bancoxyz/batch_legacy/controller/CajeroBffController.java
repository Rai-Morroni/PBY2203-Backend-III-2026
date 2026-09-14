package com.bancoxyz.batch_legacy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/cajero") // Ruta base para el BFF Cajero
public class CajeroBffController {

    @GetMapping("/operaciones") // Endpoint para obtener datos de operaciones del cajero
    public Map<String, Object> getAtmOperaciones() {
        // BFF Cajero: Interfaz eficiente para operaciones críticas
        Map<String, Object> response = new HashMap<>();
        
        // Datos simulados para la pantalla del cajero
        response.put("canal", "Cajero Automático (Banco Chile)");
        response.put("consultaSaldo", 1500000.00);
        response.put("limiteRetiroDiario", 200000.00);
        response.put("estadoCajero", "OPERATIVO");
        
        return response;
    }
}