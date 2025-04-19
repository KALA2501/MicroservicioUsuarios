package controllers;

import com.usuarios.demo.security.JwtAuthenticationFilter;
import services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/kafka")
public class KafkaController {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate; // KafkaTemplate for sending messages

    @Autowired
    private JwtService jwtService;

    private static final String TOPIC_NAME = "data_userid"; // Kafka topic name

    /**
     * Endpoint que se llamará siempre que se necesite enviar el userId al Kafka.
     * Este método obtiene el token JWT de las peticiones y extrae el userId para enviarlo a Kafka.
     */
    @GetMapping("/send-userid")
    public String sendUserIdToKafka(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return "Authorization header missing or invalid.";
        }

        final String jwt = authHeader.substring(7); // Remove "Bearer " prefix
        String userId;

        try {
            // Extract userId from JWT
            userId = jwtService.extractUserId(jwt);
        } catch (Exception e) {
            return "❌ Error extracting userId from token: " + e.getMessage();
        }

        // Send the userId to Kafka topic
        kafkaTemplate.send(TOPIC_NAME, userId);
        return "✅ UserId sent to Kafka topic: " + userId;
    }
}
