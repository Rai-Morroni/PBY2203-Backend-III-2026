package com.bancoxyz.web.dto;

import java.util.List;

public record WebDashboardDTO(
    String canal,
    String cliente,
    int totalRegistros,
    List<CuentaAnualDTO> historialCompleto
) {}