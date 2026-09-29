package com.bancoxyz.mobile.dto;

public record MobileResumenDTO(
    String canal, 
    String estado, 
    long totalMovimientos
) {}