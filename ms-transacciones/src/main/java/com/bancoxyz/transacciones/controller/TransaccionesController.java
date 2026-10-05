package com.bancoxyz.transacciones.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bancoxyz.transacciones.model.CuentaAnualEntity;
import com.bancoxyz.transacciones.repository.CuentaAnualRepository;
import com.bancoxyz.transacciones.repository.TransaccionRepository;

import java.util.List;

@RestController
@RequestMapping("/api/internal/transacciones")
public class TransaccionesController {

    @Autowired
    private CuentaAnualRepository cuentaAnualRepository;

    @Autowired
    private TransaccionRepository transaccionRepository;

    // Endpoint interno para obtener el historial (Consumido por BFF Web)
    @GetMapping("/historial")
    public List<CuentaAnualEntity> getHistorialCompleto() {
        return cuentaAnualRepository.findAll();
    }

    // Endpoint interno para contar transacciones (Consumido por BFF Móvil)
    @GetMapping("/transacciones/count")
    public long getTransaccionesCount() {
        return transaccionRepository.count();
    }
}