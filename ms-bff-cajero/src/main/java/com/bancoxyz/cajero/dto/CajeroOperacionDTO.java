package com.bancoxyz.cajero.dto;

public record CajeroOperacionDTO(
    String canal, 
    String operacion, 
    double saldoDisponible,
    int operacionesRestantesSesion
) {}