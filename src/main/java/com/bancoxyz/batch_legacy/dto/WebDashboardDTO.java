package com.bancoxyz.batch_legacy.dto;

import com.bancoxyz.batch_legacy.model.CuentaAnualEntity;
import java.util.List;

public record WebDashboardDTO(
    String canal,
    String cliente,
    int totalRegistros,
    List<CuentaAnualEntity> historialCompleto
) {}