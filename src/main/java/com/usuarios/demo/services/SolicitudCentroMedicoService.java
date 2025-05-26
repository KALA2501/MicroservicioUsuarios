package com.usuarios.demo.services;

import com.usuarios.demo.entities.*;
import com.usuarios.demo.repositories.*;
import com.usuarios.demo.exceptions.CentroMedicoException;

import org.springframework.stereotype.Service;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;

import java.security.SecureRandom;
import java.util.*;

import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SolicitudCentroMedicoService {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudCentroMedicoService.class);
    private static final String TOPIC = "notifications";

    private final SolicitudCentroMedicoRepository repository;
    private final CentroMedicoRepository centroMedicoRepository;
    private final FirebaseAuth firebaseAuth;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public SolicitudCentroMedicoService(
            SolicitudCentroMedicoRepository repository,
            CentroMedicoRepository centroMedicoRepository,
            FirebaseAuth firebaseAuth,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.repository = repository;
        this.centroMedicoRepository = centroMedicoRepository;
        this.firebaseAuth = firebaseAuth;
        this.kafkaTemplate = kafkaTemplate;
    }

    public SolicitudCentroMedico guardarSolicitud(SolicitudCentroMedico solicitud) {
        if (solicitud.getCorreo() == null || solicitud.getTelefono() == null) {
            throw new IllegalArgumentException("Correo y teléfono no pueden ser nulos");
        }

        if (repository.existsByCorreo(solicitud.getCorreo())) {
            throw new RuntimeException("Ya existe una solicitud con ese correo");
        }
        if (repository.existsByTelefono(solicitud.getTelefono())) {
            throw new RuntimeException("Ya existe una solicitud con ese teléfono");
        }

        return repository.save(solicitud);
    }

    public List<SolicitudCentroMedico> obtenerSolicitudes() {
        return repository.findAll();
    }

    public void marcarComoProcesado(Long id) {
        SolicitudCentroMedico s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        s.setProcesado(true);
        repository.save(s);
    }

    public void procesarYCrearUsuario(Long id, String rol) {
        logger.info("Procesando solicitud con ID: " + id);
        SolicitudCentroMedico solicitud = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        solicitud.setProcesado(true);
        repository.save(solicitud);

        String generatedPassword = generateRandomPassword(12);

        try {
            firebaseAuth.getUserByEmail(solicitud.getCorreo());
            logger.info("✅ Usuario ya existía en Firebase");
        } catch (FirebaseAuthException e) {
            if (e.getAuthErrorCode().name().equals("USER_NOT_FOUND")) {
                try {
                    CreateRequest request = new CreateRequest()
                            .setEmail(solicitud.getCorreo())
                            .setPassword(generatedPassword)
                            .setEmailVerified(false)
                            .setDisabled(false);

                    firebaseAuth.createUser(request);
                    logger.info("✅ Usuario creado en Firebase");
                } catch (FirebaseAuthException ex) {
                    if (ex.getAuthErrorCode().name().equals("EMAIL_EXISTS")) {
                        try {
                            firebaseAuth.getUserByEmail(solicitud.getCorreo());
                            logger.warn("⚠️ Usuario ya existía, obtenido después de EMAIL_EXISTS");
                        } catch (FirebaseAuthException finalEx) {
                            throw new CentroMedicoException("No se pudo obtener el usuario tras EMAIL_EXISTS: " + finalEx.getMessage());
                        }
                    } else {
                        throw new CentroMedicoException("No se pudo crear el usuario: " + ex.getMessage());
                    }
                }
            } else {
                throw new CentroMedicoException("Error al obtener usuario: " + e.getMessage());
            }
        }

        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("rol", rol);
            String uid = firebaseAuth.getUserByEmail(solicitud.getCorreo()) != null
                    ? firebaseAuth.getUserByEmail(solicitud.getCorreo()).getUid()
                    : null;
            if (uid != null) {
                firebaseAuth.setCustomUserClaims(uid, claims);
                logger.info("✅ Rol asignado en Firebase: {}", rol);
            }
        } catch (Exception ex) {
            throw new CentroMedicoException("No se pudo asignar el rol en Firebase: " + ex.getMessage(), ex);
        }

        if (rol.equalsIgnoreCase("CENTRO_MEDICO")) {
            CentroMedico nuevo = new CentroMedico();
            nuevo.setNombre(solicitud.getNombre());
            nuevo.setCorreo(solicitud.getCorreo());
            nuevo.setDireccion(solicitud.getDireccion());
            nuevo.setTelefono(solicitud.getTelefono());
            nuevo.setUrlLogo(solicitud.getUrlLogo());
            centroMedicoRepository.save(nuevo);
            logger.info("✅ Centro médico guardado: " + nuevo.getCorreo());

            // Enviar mensaje Kafka
            Map<String, String> emailPayload = new HashMap<>();
            emailPayload.put("to", solicitud.getCorreo());
            emailPayload.put("subject", "Bienvenido a KALA");
            emailPayload.put("text", String.format("""
                    Bienvenido a KALA,

                    Tu usuario y contraseña son los siguientes:
                    %s
                    %s

                    El equipo de KALA ya activo su cuenta de Centro Medico.
                    Gracias por unirte a KALA.
                    """, solicitud.getCorreo(), generatedPassword));

            try {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(emailPayload);
                kafkaTemplate.send(TOPIC, json);
                logger.info("📨 Email de bienvenida enviado vía Kafka");
            } catch (JsonProcessingException e) {
                logger.error("❌ Error al serializar el mensaje de Kafka: {}", e.getMessage());
            }
        }
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#%$";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    @Transactional
    public void revertirProcesado(Long id) {
        SolicitudCentroMedico solicitud = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        logger.info("🔄 Iniciando proceso de reversión para: " + solicitud.getCorreo());

        boolean existeEnBaseDatos = centroMedicoRepository.existsByCorreo(solicitud.getCorreo());
        logger.info(existeEnBaseDatos ? "✅ Existe en base de datos" : "⚠️ No existe en base de datos");

        boolean existeEnFirebase = false;
        try {
            logger.info("Invocando getUserByEmail con correo: " + solicitud.getCorreo());
            firebaseAuth.getUserByEmail(solicitud.getCorreo());
            existeEnFirebase = true;
            logger.info("✅ Existe en Firebase");
        } catch (FirebaseAuthException e) {
            logger.info("⚠️ No existe en Firebase");
        }

        try {
            if (existeEnBaseDatos) {
                centroMedicoRepository.deleteByCorreo(solicitud.getCorreo());
                logger.info("✅ Eliminado de la base de datos");
            }
            if (existeEnFirebase) {
                eliminarDeFirebase(solicitud.getCorreo());
            }

            solicitud.setProcesado(false);
            repository.save(solicitud);
            logger.info("✅ Solicitud marcada como no procesada");

        } catch (Exception e) {
            logger.error("❌ Error durante la reversión: {}", e.getMessage());
            throw new CentroMedicoException("Error durante la reversión: " + e.getMessage(), e);
        }
    }

    private void eliminarDeFirebase(String correo) {
        try {
            UserRecord user = firebaseAuth.getUserByEmail(correo);
            if (user != null) {
                firebaseAuth.deleteUser(user.getUid());
                logger.info("✅ Eliminado de Firebase");
            }
        } catch (FirebaseAuthException ex) {
            logger.warn("⚠️ Error al intentar eliminar de Firebase: {}", ex.getMessage());
        }
    }

    public void eliminarSolicitud(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Solicitud no encontrada");
        }
        repository.deleteById(id);
    }

    public void eliminarPorId(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Solicitud no encontrada");
        }
        repository.deleteById(id);
    }
}
