package controllers;

import entities.TipoDocumento;
import services.TipoDocumentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
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
}
