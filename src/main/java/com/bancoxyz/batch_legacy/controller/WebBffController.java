package com.bancoxyz.batch_legacy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bancoxyz.batch_legacy.model.CuentaAnualEntity;
import com.bancoxyz.batch_legacy.repository.CuentaAnualRepository;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/web")
public class WebBffController {

    @Autowired
    private CuentaAnualRepository cuentaRepository; // Se inyecta el repositorio para acceder a la BD

    @GetMapping("/dashboard") // Endpoint para obtener el dashboard del portal web
    public Map<String, Object> getWebDashboard() {
        Map<String, Object> response = new HashMap<>();
        
        // Se consulta la BD para obtener el historial completo de cuentas anuales
        List<CuentaAnualEntity> historial = cuentaRepository.findAll();
        
        // Retorna toda la información para cumplir con la optimización para navegadores
        response.put("canal", "Portal Web Desktop");
        response.put("cliente", "Juan Pérez");
        response.put("historialCompleto", historial); // Envía el array JSON completo
        response.put("totalRegistros", historial.size());
        
        return response;
    }
}