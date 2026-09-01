package com.bancoxyz.batch_legacy.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CuentaAnualCsv {
    private Long cuenta_id;
    private String fecha;
    private String transaccion;
    private BigDecimal monto;
    private String descripcion;
}