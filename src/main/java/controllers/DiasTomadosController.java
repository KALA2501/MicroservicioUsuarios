package controllers;

import entities.DiasTomados;
import services.DiasTomadosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/dias-tomados")
@Tag(name = "Días Tomados", description = "Endpoints para la gestión de los días tomados")
public class DiasTomadosController {

    @Autowired
    private DiasTomadosService service;

    @Operation(
        summary = "Obtener todos los días tomados",
        description = "Retorna una lista de todos los registros de días tomados almacenados en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente")
    @GetMapping
    public List<DiasTomados> obtenerTodos() {
        return service.obtenerTodos();
    }
}
