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
@RequestMapping("/api/comentarios")
@Tag(name = "Comentarios", description = "Endpoints para la gestión de comentarios")
public class ComentarioController {

    @Autowired
    private ComentarioService service;

    @Operation(summary = "Obtener todos los comentarios", description = "Retorna una lista de todos los comentarios almacenados en el sistema")
    @ApiResponse(responseCode = "200", description = "Lista de comentarios obtenida exitosamente")
    @GetMapping
    public List<Comentario> obtenerTodos() {
        return service.obtenerTodos();
    }

    @Operation(summary = "Guardar un comentario", description = "Recibe un objeto comentario en el cuerpo de la solicitud y lo almacena en el sistema")
    @ApiResponse(responseCode = "200", description = "Comentario guardado correctamente")
    @PostMapping
    public Comentario guardar(@RequestBody Comentario comentario) {
        return service.guardar(comentario);
    }
}
