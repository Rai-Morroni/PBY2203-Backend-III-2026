package com.bancoxyz.batch_legacy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/web") // Ruta base para el BFF Web
public class WebBffController {

    @GetMapping("/dashboard") // Endpoint para obtener datos del dashboard web
    public Map<String, Object> getWebDashboard() {
        // BFF Web: Proporciona datos completos para interfaces complejas
        Map<String, Object> response = new HashMap<>();
        
        // Simulacion de datos completa para el navegador
        response.put("canal", "Portal Web Desktop");
        response.put("cliente", "Juan Pérez");
        response.put("saldoTotal", 1500000.00);
        response.put("historialCompleto", "Listado detallado de 100 transacciones legacy...");
        response.put("ofertasPreaprobadas", "Crédito Automotriz preaprobado por $10.000.000");
        response.put("alertasSeguridad", "Último inicio de sesión: Hoy, Mac OS, IP 192.168.1.55");
        
        return response;
    }
}