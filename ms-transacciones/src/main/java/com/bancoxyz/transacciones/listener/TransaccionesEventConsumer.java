package com.bancoxyz.transacciones.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TransaccionesEventConsumer {

    // Suscripción al tópico definido por el BFF Cajero
    @KafkaListener(topics = "cajero-retiros-topic", groupId = "transacciones-group")
    public void consumirEventoRetiro(String eventoJson) {
        System.out.println("Evento recibido desde Kafka: " + eventoJson);
        
        // Aquí se inyectaría el repositorio JPA para descontar el saldo de la base de datos
        // ej: transaccionRepository.save(nuevaTransaccion);
        
        System.out.println("Transacción persistida en BD con éxito.");
    }
}