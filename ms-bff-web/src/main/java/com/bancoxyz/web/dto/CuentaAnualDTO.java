package com.bancoxyz.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CuentaAnualDTO(
	Long id,
	Long cuentaId,
	LocalDate fecha,
	String transaccion,
	BigDecimal monto,
	String descripcion) {
}
