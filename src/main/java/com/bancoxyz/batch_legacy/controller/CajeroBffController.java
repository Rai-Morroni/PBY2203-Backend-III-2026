package com.bancoxyz.batch_legacy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bancoxyz.batch_legacy.model.InteresEntity;
import com.bancoxyz.batch_legacy.repository.InteresRepository;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/cajero") // Endpoint base para el BFF del cajero automático
public class CajeroBffController {

    @Autowired
    private InteresRepository interesRepository;

    @GetMapping("/operaciones") // Endpoint para obtener operaciones del cajero automático
    public Map<String, Object> getAtmOperaciones() {
        Map<String, Object> response = new HashMap<>();
        
        List<InteresEntity> saldos = interesRepository.findAll(); // Simula obtener los saldos de las cuentas para el cajero
        
        response.put("canal", "Cajero Automático (Banco Chile)");
        response.put("operacion", "Consulta de Saldo");
        
        if (!saldos.isEmpty()) {
            // Simula obtener el saldo de la primera cuenta encontrada para el cajero
            response.put("saldoDisponible", saldos.get(0).getSaldoFinal());
        } else {
            response.put("saldoDisponible", 0);
        }
        
        return response;
    }
}