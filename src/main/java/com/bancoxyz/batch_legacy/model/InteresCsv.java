package com.bancoxyz.batch_legacy.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InteresCsv {
    private Long cuenta_id;
    private String nombre;
    private BigDecimal saldo;
    private Integer edad;
    private String tipo;
}