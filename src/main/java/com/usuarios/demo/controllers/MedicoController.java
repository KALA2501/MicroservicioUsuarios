package com.usuarios.demo.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.usuarios.demo.entities.CentroMedico;
import com.usuarios.demo.entities.Medico;
import com.usuarios.demo.entities.TipoDocumento;
import com.usuarios.demo.repositories.MedicoRepository;
import com.usuarios.demo.services.JwtService;
import com.usuarios.demo.services.MedicoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/medicos")
@Tag(name = "Médicos", description = "Endpoints para la gestión de médicos")
public class MedicoController {

    private final MedicoService service;
    private final JwtService jwtService;
    private final MedicoRepository medicoRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String MEDICO_NO_ENCONTRADO = "Médico no encontrado";
    private static final String TOPIC = "notifications";

    public MedicoController(MedicoService service,
                           JwtService jwtService,
                           MedicoRepository medicoRepository,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.service = service;
        this.jwtService = jwtService;
        this.medicoRepository = medicoRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping
    public List<Medico> obtenerTodos() {
        return service.obtenerTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        return service.obtenerPorId(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(MEDICO_NO_ENCONTRADO));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('centro_medico')")
    public ResponseEntity<?> guardar(@RequestBody Medico medico) {
        try {
            Optional<CentroMedico> centro = service.obtenerCentroPorId(medico.getCentroMedico().getPkId());
            Optional<TipoDocumento> tipoDoc = service.obtenerTipoDocumentoPorId(medico.getTipoDocumento().getId());

            if (centro.isEmpty() || tipoDoc.isEmpty()) {
                return ResponseEntity.badRequest().body("Centro médico o tipo de documento no encontrado");
            }

            if (medico.getCorreo() == null || medico.getCorreo().isBlank()) {
                return ResponseEntity.badRequest().body("Correo del médico es obligatorio");
            }

            // Generar contraseña aleatoria
            String generatedPassword = generateRandomPassword(12);

            // Crear usuario Firebase
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(medico.getCorreo())
                    .setPassword(generatedPassword)
                    .setEmailVerified(false)
                    .setDisabled(false);

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);

            Map<String, Object> claims = new HashMap<>();
            claims.put("rol", "medico");
            FirebaseAuth.getInstance().setCustomUserClaims(userRecord.getUid(), claims);

            medico.setPkId(UUID.randomUUID().toString());
            medico.setCentroMedico(centro.get());
            medico.setTipoDocumento(tipoDoc.get());

            Medico guardado = service.guardar(medico);

            // Enviar email via Kafka
            sendWelcomeEmail(medico.getCorreo(), generatedPassword);

            return ResponseEntity.ok(guardado);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al crear médico: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable String id) {
        try {
            Optional<Medico> medicoOpt = service.obtenerPorId(id);
            if (medicoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MEDICO_NO_ENCONTRADO);
            }

            Medico medico = medicoOpt.get();
            String correo = medico.getCorreo();

            try {
                UserRecord user = FirebaseAuth.getInstance().getUserByEmail(correo);
                FirebaseAuth.getInstance().deleteUser(user.getUid());
            } catch (Exception ignored) {
            }

            service.eliminar(id);
            return ResponseEntity.ok("Médico eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el médico: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Medico medicoActualizado) {
        Optional<Medico> existente = service.obtenerPorId(id);
        if (existente.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(MEDICO_NO_ENCONTRADO);
        }

        Optional<CentroMedico> centro = service.obtenerCentroPorId(medicoActualizado.getCentroMedico().getPkId());
        Optional<TipoDocumento> tipoDoc = service.obtenerTipoDocumentoPorId(medicoActualizado.getTipoDocumento().getId());

        if (centro.isEmpty() || tipoDoc.isEmpty()) {
            return ResponseEntity.badRequest().body("Centro médico o tipo de documento no encontrado");
        }

        medicoActualizado.setCentroMedico(centro.get());
        medicoActualizado.setTipoDocumento(tipoDoc.get());
        medicoActualizado.setPkId(id);

        Medico actualizado = service.guardar(medicoActualizado);
        return ResponseEntity.ok(actualizado);
    }

    @GetMapping("/centro-medico/{idCentro}")
    public List<Medico> obtenerPorCentro(@PathVariable Long idCentro) {
        return service.obtenerPorCentroMedico(idCentro);
    }

    @GetMapping("/filtrar")
    public ResponseEntity<List<Medico>> filtrarMedicos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String tarjeta,
            @RequestParam(required = false) String profesion) {
        return ResponseEntity.ok(service.filtrarMedicos(nombre, tarjeta, profesion));
    }

    @GetMapping("/details")
    public ResponseEntity<?> obtenerDetallesMedico(Principal principal) {
        String email = principal.getName();
        return service.obtenerPorCorreo(email)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(MEDICO_NO_ENCONTRADO));
    }

    @GetMapping("/firebase")
    public ResponseEntity<?> obtenerDetallesMedicoFirebase(@RequestParam String uid) {
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);
            return ResponseEntity.ok(userRecord);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener usuario: " + e.getMessage());
        }
    }

    @GetMapping("/buscar-por-correo")
    public ResponseEntity<?> obtenerPorCorreo(@RequestParam String correo) {
        return service.obtenerPorCorreo(correo)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(MEDICO_NO_ENCONTRADO));
    }

    @GetMapping("/medico-id")
    public ResponseEntity<?> obtenerMedicoId(@RequestHeader("Authorization") String token) {
        try {
            String correoMedico = jwtService.extractUsername(token.replace("Bearer ", ""));
            Medico medico = medicoRepository.findByCorreo(correoMedico)
                    .orElseThrow(() -> new RuntimeException(MEDICO_NO_ENCONTRADO));
            return ResponseEntity.ok(medico.getPkId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener el médico: " + e.getMessage());
        }
    }

    // ==================== Métodos auxiliares ====================

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void sendWelcomeEmail(String email, String password) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> emailPayload = new HashMap<>();
            emailPayload.put("to", email);
            emailPayload.put("subject", "Bienvenido a KALA");
            emailPayload.put("text", String.format("""
                    Hola,

                    Bienvenido a KALA.

                    Una cuenta de médico ha sido creada para ti. Tus credenciales temporales son:

                    Usuario: %s
                    Contraseña: %s

                    Gracias por unirte a KALA.
                    """, email, password));

            String json = mapper.writeValueAsString(emailPayload);
            kafkaTemplate.send(TOPIC, json);
        } catch (Exception e) {
            System.err.println("Error enviando email Kafka: " + e.getMessage());
        }
    }
}