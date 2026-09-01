package com.bancoxyz.batch_legacy.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransaccionCsv {
    private Long id;
    private String fecha;
    private BigDecimal monto;
    private String tipo;
}