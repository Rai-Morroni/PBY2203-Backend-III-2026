package com.bancoxyz.batch_legacy.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "estados_cuenta_anual")
@Data
public class CuentaAnualEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long cuentaId;
    private LocalDate fecha;
    private String transaccion;
    private BigDecimal monto;
    private String descripcion;
}