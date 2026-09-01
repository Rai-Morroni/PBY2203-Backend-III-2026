package com.bancoxyz.batch_legacy.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "saldos_intereses")
@Data
public class InteresEntity {
    @Id
    private Long cuentaId;
    private String nombre;
    private BigDecimal saldoInicial;
    private Integer edad;
    private String tipoCuenta;
    private BigDecimal saldoFinal; // Calculado tras aplicar intereses
}