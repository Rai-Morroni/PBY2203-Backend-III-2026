package com.bancoxyz.batch_legacy.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transacciones")
@Data
public class TransaccionEntity {
    @Id
    private Long id;
    private LocalDate fecha;
    private BigDecimal monto;
    private String tipo;
    private String estado; // NORMAL o ANOMALIA
}