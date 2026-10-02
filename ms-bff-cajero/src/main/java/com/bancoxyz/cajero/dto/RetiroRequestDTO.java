package com.bancoxyz.cajero.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RetiroRequestDTO(
    @NotBlank(message = "El ID de cuenta no puede estar vacío") String cuentaId,
    @NotNull(message = "El monto es obligatorio") @Min(value = 1000, message = "El retiro mínimo es de $1000") Double monto
) {}