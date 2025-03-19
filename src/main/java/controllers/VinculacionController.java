package controllers;

import entities.Vinculacion;
import services.VinculacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/vinculacion")
@Tag(name = "Vinculaciones", description = "Endpoints para la gestión de las vinculaciones entre pacientes y médicos")
public class VinculacionController {

    @Autowired
    private VinculacionService service;

    @Operation(
        summary = "Obtener todas las vinculaciones",
        description = "Retorna una lista de todas las vinculaciones registradas en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista de vinculaciones obtenida exitosamente")
    @GetMapping
    public List<Vinculacion> obtenerTodos() {
        return service.obtenerTodos();
    }
}
