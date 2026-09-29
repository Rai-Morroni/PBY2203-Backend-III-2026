package com.bancoxyz.core.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bancoxyz.core.model.CuentaAnualEntity;
import com.bancoxyz.core.model.InteresEntity;
import com.bancoxyz.core.repository.CuentaAnualRepository;
import com.bancoxyz.core.repository.InteresRepository;
import com.bancoxyz.core.repository.TransaccionRepository;

import java.util.List;

@RestController
@RequestMapping("/api/internal/core")
public class CoreController {

    @Autowired
    private CuentaAnualRepository cuentaAnualRepository;

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private InteresRepository interesRepository;

    // Endpoint interno para obtener el historial completo (Consumido por BFF Web)
    @GetMapping("/historial")
    public List<CuentaAnualEntity> getHistorialCompleto() {
        return cuentaAnualRepository.findAll();
    }

    // Endpoint interno para contar transacciones (Consumido por BFF Móvil)
    @GetMapping("/transacciones/count")
    public long getTransaccionesCount() {
        return transaccionRepository.count();
    }

    // Endpoint interno para obtener saldos (Consumido por BFF Cajero)
@GetMapping("/saldo")
    public Double getSaldo() {
        List<InteresEntity> lista = interesRepository.findAll();
        if (!lista.isEmpty() && lista.get(0).getSaldoFinal() != null) {
            return lista.get(0).getSaldoFinal().doubleValue();
        }
        return 0.0;
    }
}