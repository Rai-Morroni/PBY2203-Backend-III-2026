package com.bancoxyz.batch_legacy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bancoxyz.batch_legacy.model.TransaccionEntity;
import com.bancoxyz.batch_legacy.repository.TransaccionRepository;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/mobile") // Endpoint base para el BFF móvil
public class MobileBffController {

    @Autowired
    private TransaccionRepository transaccionRepository; // Se inyecta el repositorio para acceder a la BD

    @GetMapping("/resumen") // Endpoint para obtener un resumen ligero de transacciones para la app móvil
    public Map<String, Object> getMobileResumen() {
        Map<String, Object> response = new HashMap<>();
        
        // Se obtienen los datos, pero se procesan para que el JSON sea más pequeño
        List<TransaccionEntity> transacciones = transaccionRepository.findAll();
        
        response.put("canal", "App Móvil");
        response.put("estado", "Datos ligeros cargados");
        response.put("totalMovimientos", transacciones.size());
        // El payload queda reducido, optimizando la red móvil.
        
        return response;
    }
}