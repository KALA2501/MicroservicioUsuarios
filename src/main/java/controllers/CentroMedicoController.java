package controllers;

import entities.CentroMedico;
import services.CentroMedicoService;
import repositories.CentroMedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/centro-medico")
@Tag(name = "Centros Médicos", description = "Endpoints para la gestión de centros médicos")
public class CentroMedicoController {

    @Autowired
    private CentroMedicoService service;

    @Autowired
    private CentroMedicoRepository centroMedicoRepository;

    @Operation(
        summary = "Obtener todos los centros médicos",
        description = "Retorna una lista de todos los centros médicos registrados en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente")
    @GetMapping
    public List<CentroMedico> obtenerTodos() {
        return service.obtenerTodos();
    }

    @Operation(
        summary = "Obtener centro médico por ID",
        description = "Retorna un centro médico a partir del identificador proporcionado"
    )
    @ApiResponse(responseCode = "200", description = "Centro médico encontrado")
    @ApiResponse(responseCode = "404", description = "Centro médico no encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        Optional<CentroMedico> centro = service.obtenerPorId(id);
        if (centro.isPresent()) {
            return ResponseEntity.ok(centro.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Centro médico no encontrado");
        }
    }

    @Operation(
        summary = "Crear un centro médico",
        description = "Guarda un centro médico recibido en el cuerpo de la petición"
    )
    @ApiResponse(responseCode = "201", description = "Centro médico guardado correctamente")
    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody CentroMedico centroMedico) {
        try {
            CentroMedico guardado = service.registrarCentroMedico(centroMedico);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al guardar centro médico: " + e.getMessage());
        }
    }

    @Operation(
        summary = "Eliminar un centro médico",
        description = "Elimina el centro médico identificado por el ID proporcionado"
    )
    @ApiResponse(responseCode = "200", description = "Centro médico eliminado correctamente")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            service.eliminar(id);
            return ResponseEntity.ok("Centro médico eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se pudo eliminar: " + e.getMessage());
        }
    }

    @Operation(
    summary = "Actualizar información de un centro médico",
    description = "Permite editar el nombre, dirección, teléfono y logo del centro médico"
    )
    @ApiResponse(responseCode = "200", description = "Centro médico actualizado correctamente")
    @ApiResponse(responseCode = "404", description = "Centro médico no encontrado")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody CentroMedico nuevosDatos) {
        try {
            CentroMedico actualizado = service.actualizar(id, nuevosDatos);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al actualizar centro médico: " + e.getMessage());
        }
    }

    @GetMapping("/buscar-por-correo")
    public ResponseEntity<?> obtenerPorCorreo(@RequestParam String correo) {
        Optional<CentroMedico> centro = centroMedicoRepository.findByCorreo(correo);
        if (centro.isPresent()) {
            return ResponseEntity.ok(centro.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Centro no encontrado");
    }


}
