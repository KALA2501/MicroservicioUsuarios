package com.usuarios.demo.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.usuarios.demo.entities.*;
import com.usuarios.demo.services.*;
import com.usuarios.demo.repositories.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.security.Principal;

@RestController
@RequestMapping("/api/medicos")
@Tag(name = "Médicos", description = "Endpoints para la gestión de médicos")
public class MedicoController {

    @Autowired
    private MedicoService service;
    private JwtService jwtService;
    private MedicoRepository medicoRepository;

    @Operation(summary = "Obtener todos los médicos", description = "Retorna una lista de todos los médicos registrados en el sistema")
    @ApiResponse(responseCode = "200", description = "Lista de médicos obtenida correctamente")
    @GetMapping
    public List<Medico> obtenerTodos() {
        return service.obtenerTodos();
    }

    @Operation(summary = "Obtener un médico por ID", description = "Retorna los detalles del médico identificado por el ID proporcionado")
    @ApiResponse(responseCode = "200", description = "Médico encontrado")
    @ApiResponse(responseCode = "404", description = "Médico no encontrado")
    @GetMapping("/{id}")
    public Optional<Medico> obtenerPorId(@PathVariable String id) {
        return service.obtenerPorId(id);
    }

    @Operation(summary = "Guardar un nuevo médico", description = "Recibe un objeto médico en el cuerpo de la solicitud y lo almacena en el sistema")
    @ApiResponse(responseCode = "200", description = "Médico guardado correctamente")
    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody Medico medico) {
        try {
            Optional<CentroMedico> centro = service.obtenerCentroPorId(medico.getCentroMedico().getPkId());
            if (centro.isEmpty()) {
                return ResponseEntity.badRequest().body("Centro médico no encontrado");
            }

            Optional<TipoDocumento> tipoDoc = service.obtenerTipoDocumentoPorId(medico.getTipoDocumento().getId());
            if (tipoDoc.isEmpty()) {
                return ResponseEntity.badRequest().body("Tipo de documento no encontrado");
            }

            if (medico.getCorreo() == null || medico.getCorreo().isBlank()) {
                return ResponseEntity.badRequest().body("Correo del médico es obligatorio");
            }

            // Crear usuario en Firebase Authentication
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(medico.getCorreo())
                    .setPassword("medico123") // contraseña temporal
                    .setEmailVerified(false)
                    .setDisabled(false);

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);

            // Asignar custom claim (rol: medico)
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", "medico");
            FirebaseAuth.getInstance().setCustomUserClaims(userRecord.getUid(), claims);

            // Guardar en base de datos
            medico.setPkId(UUID.randomUUID().toString());
            medico.setCentroMedico(centro.get());
            medico.setTipoDocumento(tipoDoc.get());

            Medico guardado = service.guardar(medico);
            return ResponseEntity.ok(guardado);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al crear médico: " + e.getMessage());
        }
    }

    @Operation(summary = "Eliminar un médico", description = "Elimina el médico identificado por el ID proporcionado")
    @ApiResponse(responseCode = "200", description = "Médico eliminado correctamente")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable String id) {
        try {
            service.eliminar(id);
            return ResponseEntity.ok("Médico eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error al eliminar el médico: " + e.getMessage());
        }
    }

    @Operation(summary = "Actualizar información de un médico", description = "Modifica los datos de un médico ya registrado")
    @ApiResponse(responseCode = "200", description = "Médico actualizado correctamente")
    @ApiResponse(responseCode = "404", description = "Médico no encontrado")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id, @RequestBody Medico medicoActualizado) {
        Optional<Medico> existente = service.obtenerPorId(id);
        if (existente.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Médico no encontrado");
        }

        Optional<CentroMedico> centro = service.obtenerCentroPorId(medicoActualizado.getCentroMedico().getPkId());
        if (centro.isEmpty()) {
            return ResponseEntity.badRequest().body("Centro médico no encontrado");
        }

        Optional<TipoDocumento> tipoDoc = service
                .obtenerTipoDocumentoPorId(medicoActualizado.getTipoDocumento().getId());
        if (tipoDoc.isEmpty()) {
            return ResponseEntity.badRequest().body("Tipo de documento no encontrado");
        }

        medicoActualizado.setCentroMedico(centro.get());
        medicoActualizado.setTipoDocumento(tipoDoc.get());
        medicoActualizado.setPkId(id);

        Medico actualizado = service.guardar(medicoActualizado);
        return ResponseEntity.ok(actualizado);
    }

    @Operation(summary = "Listar médicos por centro médico", description = "Devuelve todos los médicos asociados al centro médico dado")
    @ApiResponse(responseCode = "200", description = "Médicos obtenidos correctamente")
    @GetMapping("/centro-medico/{idCentro}")
    public List<Medico> obtenerPorCentro(@PathVariable Long idCentro) {
        return service.obtenerPorCentroMedico(idCentro);
    }

    @Operation(summary = "Filtrar médicos", description = "Busca médicos por nombre, tarjeta profesional o profesión (solo un filtro a la vez)")
    @ApiResponse(responseCode = "200", description = "Lista filtrada exitosamente")
    @GetMapping("/filtrar")
    public ResponseEntity<List<Medico>> filtrarMedicos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String tarjeta,
            @RequestParam(required = false) String profesion) {

        List<Medico> resultados = service.filtrarMedicos(nombre, tarjeta, profesion);
        return ResponseEntity.ok(resultados);
    }

    @GetMapping("/details")
    public ResponseEntity<?> obtenerDetallesMedico(Principal principal) {
        String email = principal.getName();
        Optional<Medico> medico = service.obtenerPorCorreo(email);

        if (medico.isPresent()) {
            System.out.println("✅ Médico encontrado: " + medico.get().getCorreo());
            System.out.println("✅ URL de imagen: " + medico.get().getUrlImagen());
            return ResponseEntity.ok(medico.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Médico no encontrado");
        }
    }

    @GetMapping("/firebase")
    public ResponseEntity<?> obtenerDetallesMedicoFirebase(@RequestParam String uid) {
        try {
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);
            if (userRecord != null) {
                return ResponseEntity.ok(userRecord);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado en Firebase");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener usuario: " + e.getMessage());
        }
    }

    @Operation(summary = "Buscar médico por correo", description = "Devuelve el médico cuyo correo coincida con el valor dado")
    @ApiResponse(responseCode = "200", description = "Médico encontrado")
    @ApiResponse(responseCode = "404", description = "Médico no encontrado")
    @GetMapping("/buscar-por-correo")
    public ResponseEntity<?> obtenerPorCorreo(@RequestParam String correo) {
        System.out.println("📧 Recibiendo correo: " + correo);
        Optional<Medico> medico = service.obtenerPorCorreo(correo);
        if (medico.isPresent()) {
            System.out.println("🧠 Médico encontrado: " + medico.get().getNombre());
            System.out.println("🖼️ Imagen: " + medico.get().getUrlImagen());
            return ResponseEntity.ok(medico.get());
        } else {
            System.out.println("❌ Médico no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Médico no encontrado");
        }
    }

    @Operation(summary = "Obtener ID del médico autenticado", description = "Devuelve el ID del médico asociado al usuario autenticado")
    @GetMapping("/medico-id")
    public ResponseEntity<?> obtenerMedicoId(@RequestHeader("Authorization") String token) {
        try {
            // Extraer el correo del médico desde el token
            String correoMedico = jwtService.extractUsername(token.replace("Bearer ", ""));

            // Buscar el médico asociado al correo extraído
            Medico medico = medicoRepository.findByCorreo(correoMedico)
                    .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

            // Devolver el ID del médico
            return ResponseEntity.ok(medico.getPkId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener el médico: " + e.getMessage());
        }
    }

}
