package com.bancoxyz.batch_legacy.processor;

import com.bancoxyz.batch_legacy.model.InteresCsv;
import com.bancoxyz.batch_legacy.model.InteresEntity;
import org.springframework.batch.item.ItemProcessor;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class InteresProcessor implements ItemProcessor<InteresCsv, InteresEntity> {

    @Override
    public InteresEntity process(InteresCsv item) throws Exception {
        InteresEntity entity = new InteresEntity();
        
        // Mapeo seguro contra nulos (Tolerancia a fallos)
        if(item.getCuenta_id() == null) {
            throw new IllegalArgumentException("El ID de cuenta no puede ser nulo"); 
        }
        
        entity.setCuentaId(item.getCuenta_id());
        entity.setNombre(item.getNombre());
        entity.setEdad(item.getEdad());
        
        String tipoStr = item.getTipo() != null ? item.getTipo().toUpperCase().trim() : "DESCONOCIDO";
        entity.setTipoCuenta(tipoStr);
        
        BigDecimal saldoInicial = item.getSaldo() != null ? item.getSaldo() : BigDecimal.ZERO;
        entity.setSaldoInicial(saldoInicial);

        // Reglas de cálculo de intereses
        BigDecimal saldoFinal;
        if ("AHORRO".equals(tipoStr)) {
            // Ahorro: 3% de interés a favor
            saldoFinal = saldoInicial.multiply(new BigDecimal("1.03"));
        } else if ("PRESTAMO".equals(tipoStr)) {
             // Préstamo: 5% de interés en contra (aumenta la deuda)
            saldoFinal = saldoInicial.multiply(new BigDecimal("1.05"));
        } else {
             // Otro tipo de cuenta: sin interés
            saldoFinal = saldoInicial;
        }

        // Redondear a 2 decimales para formato financiero
        entity.setSaldoFinal(saldoFinal.setScale(2, RoundingMode.HALF_UP));

        return entity;
    }
}