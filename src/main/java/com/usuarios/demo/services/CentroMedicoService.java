package com.usuarios.demo.services;

import com.usuarios.demo.entities.CentroMedico;
import com.usuarios.demo.exceptions.CentroMedicoException;
import com.usuarios.demo.repositories.CentroMedicoRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CentroMedicoService {
    private static final Logger logger = LoggerFactory.getLogger(CentroMedicoService.class);

    private final CentroMedicoRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public CentroMedicoService(CentroMedicoRepository repository, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<CentroMedico> obtenerTodos() {
        return repository.findAll();
    }

    public Optional<CentroMedico> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    public CentroMedico guardar(CentroMedico centroMedico) {
        return repository.save(centroMedico);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    public CentroMedico actualizar(Long id, CentroMedico nuevosDatos) {
        Optional<CentroMedico> existente = repository.findById(id);
        if (existente.isPresent()) {
            CentroMedico centro = existente.get();
            centro.setNombre(nuevosDatos.getNombre());
            centro.setDireccion(nuevosDatos.getDireccion());
            centro.setTelefono(nuevosDatos.getTelefono());
            centro.setUrlLogo(nuevosDatos.getUrlLogo());
            return repository.save(centro);
        } else {
            throw new CentroMedicoException("Centro médico no encontrado con ID: " + id);
        }
    }

    public boolean existePorCorreo(String correo) {
        return repository.existsByCorreo(correo);
    }

    @Transactional
    public CentroMedico registrarCentroMedico(CentroMedico centro) {
        // Validate required fields
        if (centro.getNombre() == null || centro.getCorreo() == null || centro.getTelefono() == null) {
            throw new CentroMedicoException("Faltan datos obligatorios");
        }

        // Check for duplicate email
        if (repository.existsByCorreo(centro.getCorreo())) {
            throw new CentroMedicoException("Centro ya existe con ese correo");
        }

        // Save to DB
        CentroMedico guardado = repository.save(centro);

        try {
            // Create user in Firebase
            logger.info("Creating Firebase user for: {}", guardado.getCorreo());
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(guardado.getCorreo())
                    .setPassword("KalaTemporal123")
                    .setEmailVerified(false)
                    .setDisabled(false);

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);

            // Assign custom claims
            Map<String, Object> claims = new HashMap<>();
            claims.put("rol", "centro_medico");
            FirebaseAuth.getInstance().setCustomUserClaims(userRecord.getUid(), claims);

            logger.info("✅ Firebase user created and role assigned: {}", guardado.getCorreo());

            // Build email content
            String welcomeText = String.format(
                "Welcome to KALA!\n\nYour login is: %s\nYour temporary password is: KalaTemporal123\n\nPlease change your password after logging in.",
                guardado.getCorreo()
            );

            String jsonPayload = String.format(
                "{\"to\": \"%s\", \"subject\": \"Welcome to KALA 🎉\", \"text\": \"%s\"}",
                guardado.getCorreo(),
                welcomeText.replace("\n", "\\n")
            );

            // Send message to Kafka
            kafkaTemplate.send("notifications", jsonPayload);
            logger.info("📬 Sent welcome email to Kafka for: {}", guardado.getCorreo());

        } catch (Exception e) {
            logger.error("❌ Error creating Firebase user, rolling back DB...");

            repository.deleteById(guardado.getPkId());

            String errorMessage = "Error registering Centro Médico: " +
                    (e.getMessage() != null ? e.getMessage() : "Unknown error") +
                    "\nDetails: " + e.toString();

            throw new CentroMedicoException(errorMessage, e);
        }

        return guardado;
    }

    @Transactional
    public void eliminarPorCorreo(String correo) {
        try {
            Optional<CentroMedico> centro = repository.findByCorreo(correo);
            if (centro.isPresent()) {
                repository.delete(centro.get());
                logger.info("✅ Centro médico eliminado correctamente: {}", correo);
            } else {
                logger.warn("⚠️ No se encontró centro médico con el correo: {}", correo);
            }
        } catch (Exception e) {
            logger.error("❌ Error al eliminar centro médico: {}", e.getMessage(), e);
            throw new CentroMedicoException("Error al eliminar centro médico con correo: " + correo, e);
        }
    }
}
