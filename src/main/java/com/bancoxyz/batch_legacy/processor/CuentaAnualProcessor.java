package com.bancoxyz.batch_legacy.processor;

import com.bancoxyz.batch_legacy.model.CuentaAnualCsv;
import com.bancoxyz.batch_legacy.model.CuentaAnualEntity;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class CuentaAnualProcessor implements ItemProcessor<CuentaAnualCsv, CuentaAnualEntity> {

    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList( // Lista de formatos de fecha permitidos
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    @Override
    public CuentaAnualEntity process(CuentaAnualCsv item) throws Exception {
        CuentaAnualEntity entity = new CuentaAnualEntity();
        
        if (item.getCuenta_id() == null) {
             throw new IllegalArgumentException("El ID de cuenta es obligatorio para el estado anual");
        }
        // Validar que el monto no sea nulo, si es nulo, asignar BigDecimal.ZERO
        entity.setCuentaId(item.getCuenta_id());
        entity.setTransaccion(item.getTransaccion());
        entity.setDescripcion(item.getDescripcion());
        
        LocalDate parsedDate = parseDate(item.getFecha());
        if (parsedDate == null) {
            throw new IllegalArgumentException("Formato de fecha inválido en estado anual: " + item.getFecha());
        }
        entity.setFecha(parsedDate);

        BigDecimal monto = item.getMonto() != null ? item.getMonto() : BigDecimal.ZERO;
        entity.setMonto(monto);

        return entity;
    }

    // Método para parsear la fecha con múltiples formatos
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        for (DateTimeFormatter formatter : FORMATTERS) {
            try { return LocalDate.parse(dateStr, formatter); } 
            catch (DateTimeParseException ignored) {}
        }
        return null;
    }
}