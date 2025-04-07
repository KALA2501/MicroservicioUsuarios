package controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import entities.CentroMedico;
import entities.Medico;
import entities.TipoDocumento;
import services.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/medicos")
@Tag(name = "Médicos", description = "Endpoints para la gestión de médicos")
public class MedicoController {

    @Autowired
    private MedicoService service;

    @Operation(
        summary = "Obtener todos los médicos",
        description = "Retorna una lista de todos los médicos registrados en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista de médicos obtenida correctamente")
    @GetMapping
    public List<Medico> obtenerTodos() {
        return service.obtenerTodos();
    }

    @Operation(
        summary = "Obtener un médico por ID",
        description = "Retorna los detalles del médico identificado por el ID proporcionado"
    )
    @ApiResponse(responseCode = "200", description = "Médico encontrado")
    @ApiResponse(responseCode = "404", description = "Médico no encontrado")
    @GetMapping("/{id}")
    public Optional<Medico> obtenerPorId(@PathVariable String id) {
        return service.obtenerPorId(id);
    }

    @Operation(
        summary = "Guardar un nuevo médico",
        description = "Recibe un objeto médico en el cuerpo de la solicitud y lo almacena en el sistema"
    )
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

    @Operation(
        summary = "Eliminar un médico",
        description = "Elimina el médico identificado por el ID proporcionado"
    )
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


    @Operation(
        summary = "Actualizar información de un médico",
        description = "Modifica los datos de un médico ya registrado"
    )
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

        Optional<TipoDocumento> tipoDoc = service.obtenerTipoDocumentoPorId(medicoActualizado.getTipoDocumento().getId());
        if (tipoDoc.isEmpty()) {
            return ResponseEntity.badRequest().body("Tipo de documento no encontrado");
        }

        medicoActualizado.setCentroMedico(centro.get());
        medicoActualizado.setTipoDocumento(tipoDoc.get());
        medicoActualizado.setPkId(id);

        Medico actualizado = service.guardar(medicoActualizado);
        return ResponseEntity.ok(actualizado);
    }

    @Operation(
        summary = "Listar médicos por centro médico",
        description = "Devuelve todos los médicos asociados al centro médico dado"
    )
    @ApiResponse(responseCode = "200", description = "Médicos obtenidos correctamente")
    @GetMapping("/centro-medico/{idCentro}")
    public List<Medico> obtenerPorCentro(@PathVariable Long idCentro) {
        return service.obtenerPorCentroMedico(idCentro);
    }

    @Operation(
        summary = "Filtrar médicos",
        description = "Busca médicos por nombre, tarjeta profesional o profesión (solo un filtro a la vez)"
    )
    @ApiResponse(responseCode = "200", description = "Lista filtrada exitosamente")
    @GetMapping("/filtrar")
    public ResponseEntity<List<Medico>> filtrarMedicos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String tarjeta,
            @RequestParam(required = false) String profesion) {

        List<Medico> resultados = service.filtrarMedicos(nombre, tarjeta, profesion);
        return ResponseEntity.ok(resultados);
    }
}
