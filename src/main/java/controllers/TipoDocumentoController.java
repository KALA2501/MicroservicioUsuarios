package controllers;

import entities.TipoDocumento;
import services.TipoDocumentoService;
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
@RequestMapping("/api/tipo-documento")
@Tag(name = "Tipos de Documento", description = "Endpoints para la gestión de tipos de documento")
public class TipoDocumentoController {

    @Autowired
    private TipoDocumentoService service;

    @Operation(
        summary = "Obtener todos los tipos de documento",
        description = "Retorna una lista de todos los tipos de documento registrados en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista de tipos de documento obtenida exitosamente")
    @GetMapping
    public List<TipoDocumento> obtenerTodos() {
        return service.obtenerTodos();
    }

    @Operation(
        summary = "Obtener tipo de documento por ID",
        description = "Retorna un tipo de documento según su identificador"
    )
    @ApiResponse(responseCode = "200", description = "Tipo de documento encontrado")
    @ApiResponse(responseCode = "404", description = "Tipo de documento no encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable String id) {
        Optional<TipoDocumento> tipo = service.obtenerPorId(id);
        return tipo.isPresent() ?
                ResponseEntity.ok(tipo.get()) :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tipo de documento no encontrado");
    }

    @Operation(
        summary = "Crear un tipo de documento",
        description = "Crea y guarda un nuevo tipo de documento"
    )
    @ApiResponse(responseCode = "201", description = "Tipo de documento creado correctamente")
    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody TipoDocumento tipoDocumento) {
        try {
            TipoDocumento guardado = service.guardar(tipoDocumento);
            return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al crear tipo de documento: " + e.getMessage());
        }
    }

    @Operation(
        summary = "Eliminar un tipo de documento",
        description = "Elimina un tipo de documento por ID"
    )
    @ApiResponse(responseCode = "200", description = "Tipo de documento eliminado correctamente")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        try {
            service.eliminar(id);
            return ResponseEntity.ok("Tipo de documento eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se pudo eliminar: " + e.getMessage());
        }
    }
}
