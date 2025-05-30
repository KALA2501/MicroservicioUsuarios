package com.usuarios.demo.controllers;

import com.usuarios.demo.entities.*;
import com.usuarios.demo.services.*;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.usuarios.demo.repositories.VinculacionRepository;
import com.usuarios.demo.repositories.MedicoRepository;
import com.usuarios.demo.services.VinculacionService;


import java.util.List;
import java.util.Map;



@RestController
@RequestMapping("/api/vinculacion")
@Tag(name = "Vinculaciones", description = "Endpoints para vincular pacientes con médicos")
public class VinculacionController {

    @Autowired
    private VinculacionService vinculacionService;

    @Operation(summary = "Vincular paciente con médico")
    @PostMapping
    public ResponseEntity<?> crearVinculacion(
            @RequestParam String pacienteId,
            @RequestParam String medicoId,
            @RequestParam String tipoVinculacionId) {

        try {
            System.out.println("📥 Datos recibidos para crear vinculación:");
            System.out.println("Paciente ID: " + pacienteId);
            System.out.println("Médico ID: " + medicoId);
            System.out.println("Tipo de Vinculación ID: " + tipoVinculacionId);

            Vinculacion vinculacion = vinculacionService.crearVinculacion(pacienteId, medicoId, tipoVinculacionId);
            return ResponseEntity.ok(vinculacion);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al vincular: " + e.getMessage());
        }
    }

    @Operation(summary = "Listar pacientes vinculados a un médico", description = "Devuelve los pacientes vinculados al médico usando su número de tarjeta profesional")
    @ApiResponse(responseCode = "200", description = "Lista de vinculaciones obtenida correctamente")
    @GetMapping("/medico")
    public ResponseEntity<?> obtenerVinculacionesPorMedico(@RequestParam String tarjetaProfesional) {
    try {
        List<Map<String, String>> pacientes = vinculacionService.obtenerVinculacionesPorMedico(tarjetaProfesional);
        return ResponseEntity.ok(pacientes);
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
    }
}


    @Operation(summary = "Listar médicos vinculados a un paciente", description = "Devuelve los médicos vinculados a un paciente según tipo y número de documento")
    @ApiResponse(responseCode = "200", description = "Lista de vinculaciones obtenida correctamente")
    @GetMapping("/paciente")
    public ResponseEntity<?> obtenerVinculacionesPorPaciente(
            @RequestParam String tipoDocumento,
            @RequestParam String idDocumento) {
        try {
            List<Map<String, String>> vinculaciones = vinculacionService.obtenerVinculacionesPorPaciente(tipoDocumento,
                    idDocumento);
            return ResponseEntity.ok(vinculaciones);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<?> actualizarVinculacion(
            @RequestParam String pacienteId,
            @RequestParam String medicoId,
            @RequestParam String tipoVinculacionId,
            @RequestParam String nuevoTipoVinculacionId) {
        try {
            VinculacionId id = new VinculacionId(pacienteId, medicoId);
            Vinculacion actualizada = vinculacionService.actualizarVinculacion(id, nuevoTipoVinculacionId);
            return ResponseEntity.ok(actualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping
    @Operation(summary = "Eliminar vinculación entre paciente y médico")
    public ResponseEntity<?> eliminarVinculacion(@RequestParam String pacienteId, @RequestParam String medicoId) {
        try {
            vinculacionService.eliminarVinculacion(pacienteId, medicoId);
            return ResponseEntity.ok("Vinculación eliminada exitosamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vinculación no encontrada: " + e.getMessage());
        }
    }

}
