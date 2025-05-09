package com.usuarios.demo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usuarios.demo.entities.*;
import com.usuarios.demo.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import scala.collection.concurrent.Map;
import java.util.HashMap;
import java.util.Optional;


@RestController
@RequestMapping("/api/contacto-emergencia")
public class ContactoEmergenciaController {

    @Autowired
    private ContactoEmergenciaService service;

    @Autowired
    private PacienteService pacienteService;

    @Operation(summary = "Obtener todos los contactos de emergencia", description = "Retorna una lista de todos los contactos de emergencia registrados.")
    @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente")
    @GetMapping
    public List<ContactoEmergencia> obtenerTodos() {
        return service.obtenerTodos();
    }

    @Operation(summary = "Guardar un contacto de emergencia", description = "Recibe un objeto ContactoEmergencia en el cuerpo de la petición y lo almacena.")
    @ApiResponse(responseCode = "200", description = "Contacto guardado correctamente")
    @PostMapping("/crear")
    public ContactoEmergencia guardar(@RequestBody ContactoEmergencia contactoEmergencia) {
        return service.guardar(contactoEmergencia);
    }


    @GetMapping("/por-telefono")
    public ResponseEntity<?> buscarPorTelefono(@RequestParam String telefono) {
        Optional<ContactoEmergencia> existente = service.buscarPorTelefono(telefono);
        return existente.map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.noContent().build());
    }


    


}
