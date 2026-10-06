package com.bancoxyz.transacciones.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transaccion_entity")
@Data
public class TransaccionEntity {
    @Id
    private Long id;
    private LocalDate fecha;
    private BigDecimal monto;
    private String tipo;
    private String estado; // NORMAL o ANOMALIA
}