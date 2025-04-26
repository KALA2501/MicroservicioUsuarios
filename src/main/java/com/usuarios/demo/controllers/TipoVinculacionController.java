package com.usuarios.demo.controllers;

import com.usuarios.demo.entities.*;
import com.usuarios.demo.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/tipo-vinculacion")
@Tag(name = "Tipos de Vinculación", description = "Endpoints para la gestión de los tipos de vinculación")
public class TipoVinculacionController {

    @Autowired
    private TipoVinculacionService service;

    @Operation(summary = "Obtener todos los tipos de vinculación", description = "Retorna una lista de todos los tipos de vinculación registrados en el sistema")
    @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente")
    @GetMapping
    public List<TipoVinculacion> obtenerTodos() {
        return service.obtenerTodos();
    }
}
