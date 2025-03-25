package controllers;

import entities.Vinculacion;
import services.VinculacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;


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
            Vinculacion vinculacion = vinculacionService.crearVinculacion(pacienteId, medicoId, tipoVinculacionId);
            return ResponseEntity.ok(vinculacion);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al vincular: " + e.getMessage());
        }
    }
}

