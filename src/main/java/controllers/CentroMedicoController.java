package controllers;

import entities.CentroMedico;
import services.CentroMedicoService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Optional<CentroMedico> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @Operation(
        summary = "Crear un centro médico",
        description = "Guarda un centro médico recibido en el cuerpo de la petición"
    )
    @ApiResponse(responseCode = "200", description = "Centro médico guardado correctamente")
    @PostMapping
    public CentroMedico guardar(@RequestBody CentroMedico centroMedico) {
        return service.guardar(centroMedico);
    }

    @Operation(
        summary = "Eliminar un centro médico",
        description = "Elimina el centro médico identificado por el ID proporcionado"
    )
    @ApiResponse(responseCode = "200", description = "Centro médico eliminado correctamente")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
