package controllers;

import entities.Medico;
import services.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Medico guardar(@RequestBody Medico medico) {
        return service.guardar(medico);
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
}
