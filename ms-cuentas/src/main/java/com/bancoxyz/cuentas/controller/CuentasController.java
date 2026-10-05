package com.bancoxyz.cuentas.controller;

import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bancoxyz.cuentas.model.InteresEntity;
import com.bancoxyz.cuentas.repository.InteresRepository;

import java.util.List;

@RestController
@RequestMapping("/api/internal/cuentas")
public class CuentasController {

    @Autowired
    private InteresRepository interesRepository;

    // Resiliencia: Reintenta la conexión a BD si falla
    @GetMapping("/saldo")
    @Retry(name = "dbRetry", fallbackMethod = "fallbackDbConnection")
    public Double getSaldo() {
        List<InteresEntity> lista = interesRepository.findAll();
        if (!lista.isEmpty() && lista.get(0).getSaldoFinal() != null) {
            return lista.get(0).getSaldoFinal().doubleValue();
        }
        return 0.0;
    }

    // Fallback interno si la BD está caída tras los reintentos
    public Double fallbackDbConnection(Throwable t) {
        throw new RuntimeException("Error interno: Base de datos no disponible."); 
        // Esta excepción será atrapada por tu ApiExceptionHandler global
    }
}