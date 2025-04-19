package com.usuarios.demo.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    // Escucha los mensajes del tópico 'data_userid'
    @KafkaListener(topics = "data_userid", groupId = "group_id")
    public void listen(String userId) {
        System.out.println("✅ Recibido userId desde Kafka: " + userId);
        
        // Aquí puedes hacer cualquier procesamiento necesario con el userId recibido
    }
}
