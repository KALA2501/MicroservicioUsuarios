package controllers;

import entities.Paciente;
import services.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/pacientes")
@Tag(name = "Pacientes", description = "Endpoints para la gestión de pacientes")
public class PacienteController {

    @Autowired
    private PacienteService service;

    @Operation(
        summary = "Obtener todos los pacientes",
        description = "Retorna una lista de todos los pacientes registrados en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista de pacientes obtenida exitosamente")
    @GetMapping
    public List<Paciente> obtenerTodos() {
        return service.obtenerTodos();
    }

    @Operation(
        summary = "Obtener paciente por ID",
        description = "Retorna el paciente que coincide con el ID proporcionado"
    )
    @ApiResponse(responseCode = "200", description = "Paciente encontrado")
    @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    @GetMapping("/{id}")
    public Optional<Paciente> obtenerPorId(@PathVariable String id) {
        return service.obtenerPorId(id);
    }

    @Operation(
        summary = "Guardar un nuevo paciente",
        description = "Crea y almacena un nuevo paciente a partir de los datos proporcionados"
    )
    @ApiResponse(responseCode = "200", description = "Paciente guardado correctamente")
    @PostMapping
    public Paciente guardar(@RequestBody Paciente paciente) {
        return service.guardar(paciente);
    }

    @Operation(
        summary = "Eliminar paciente",
        description = "Elimina el paciente identificado por el ID proporcionado"
    )
    @ApiResponse(responseCode = "200", description = "Paciente eliminado correctamente")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        service.eliminar(id);
    }
}
