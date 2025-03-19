package controllers;

import entities.Medicamentos;
import services.MedicamentosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/medicamentos")
@Tag(name = "Medicamentos", description = "Endpoints para la gestión de medicamentos")
public class MedicamentosController {

    @Autowired
    private MedicamentosService service;

    @Operation(
        summary = "Obtener todos los medicamentos",
        description = "Retorna una lista con todos los medicamentos registrados en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista de medicamentos obtenida correctamente")
    @GetMapping
    public List<Medicamentos> obtenerTodos() {
        return service.obtenerTodos();
    }
}
