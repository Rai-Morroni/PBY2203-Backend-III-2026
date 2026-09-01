package com.bancoxyz.batch_legacy.processor;

import com.bancoxyz.batch_legacy.model.TransaccionCsv;
import com.bancoxyz.batch_legacy.model.TransaccionEntity;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

public class TransaccionProcessor implements ItemProcessor<TransaccionCsv, TransaccionEntity> {

    // Lista de formatos de fecha soportados para parseo dinámico
    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    @Override
    public TransaccionEntity process(TransaccionCsv item) throws Exception {
        TransaccionEntity entity = new TransaccionEntity();
        entity.setId(item.getId());
        entity.setTipo(item.getTipo());

        // 1. Parseo de Fechas Dinamico con múltiples formatos
        LocalDate parsedDate = parseDate(item.getFecha());
        if (parsedDate == null) {
             // Si el formato de fechas es desconocido, podemos decidir si queremos manejarlo de manera diferente,
             // por ejemplo, lanzar una excepción o asignar un valor por defecto.             
             throw new IllegalArgumentException("Unknown date format: " + item.getFecha());
        }
        entity.setFecha(parsedDate);

        // 2. Tolerancia a fallos: Interceptar montos nulos
        if (item.getMonto() == null) {
            entity.setMonto(BigDecimal.ZERO); // Asignar un valor por defecto
            entity.setEstado("ANOMALIA_NULO");
            return entity;
        }

        entity.setMonto(item.getMonto());

        // 3. Regla de negocio: Detectar montos negativos o excesivos
        if (item.getMonto().compareTo(BigDecimal.ZERO) < 0 || item.getMonto().compareTo(new BigDecimal("1000000")) > 0) {
            entity.setEstado("ANOMALIA");
        } else {
            entity.setEstado("NORMAL");
        }

        return entity;
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException e) {
                // Ignorar y probar el siguiente formato
            }
        }
        return null; // Si ningún formato coincide, devolvemos null para manejarlo en el método process
    }
}