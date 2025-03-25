package controllers;

import entities.CentroMedico;
import entities.Medico;
import entities.TipoDocumento;
import services.MedicoService;
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
        // Validar existencia del centro médico
        Optional<CentroMedico> centro = service.obtenerCentroPorId(medico.getCentroMedico().getPkId());
        if (centro.isEmpty()) {
            return ResponseEntity.badRequest().body("Centro médico no encontrado");
        }

        // Validar existencia del tipo de documento
        Optional<TipoDocumento> tipoDoc = service.obtenerTipoDocumentoPorId(medico.getTipoDocumento().getId());
        if (tipoDoc.isEmpty()) {
            return ResponseEntity.badRequest().body("Tipo de documento no encontrado");
        }

        // Setear entidades válidas
        medico.setCentroMedico(centro.get());
        medico.setTipoDocumento(tipoDoc.get());

        // Guardar médico
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
    public void eliminar(@PathVariable String id) {
        service.eliminar(id);
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
    
    medicoActualizado.setPkId(id); // Aseguramos que mantenga el mismo ID
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
