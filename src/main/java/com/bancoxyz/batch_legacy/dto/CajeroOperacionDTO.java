package com.bancoxyz.batch_legacy.dto;

public record CajeroOperacionDTO(
    String canal, 
    String operacion, 
    double saldoDisponible,
    int operacionesRestantesSesion
) {}