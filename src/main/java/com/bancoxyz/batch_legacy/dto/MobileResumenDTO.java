package com.bancoxyz.batch_legacy.dto;

public record MobileResumenDTO(
    String canal, 
    String estado, 
    long totalMovimientos
) {}