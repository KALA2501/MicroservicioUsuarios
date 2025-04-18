package controllers;

import entities.Vinculacion;
import entities.VinculacionId;
import services.VinculacionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

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
            Vinculacion vinculacion = vinculacionService.crearVinculacion(pacienteId, medicoId, tipoVinculacionId);
            return ResponseEntity.ok(vinculacion);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Error al vincular: " + e.getMessage());
        }
    }
    
    @Operation(
        summary = "Listar pacientes vinculados a un médico",
        description = "Devuelve los pacientes vinculados al médico usando su número de tarjeta profesional"
    )
    @ApiResponse(responseCode = "200", description = "Lista de vinculaciones obtenida correctamente")
    @GetMapping("/medico")
    public ResponseEntity<List<Map<String, String>>> obtenerVinculacionesPorMedico(
            @RequestParam String tarjetaProfesional) {
        
        List<Map<String, String>> vinculaciones = vinculacionService.obtenerVinculacionesPorMedico(tarjetaProfesional);
        return ResponseEntity.ok(vinculaciones);
    }

    @Operation(summary = "Listar médicos vinculados a un paciente",
           description = "Devuelve los médicos vinculados a un paciente según tipo y número de documento")
    @ApiResponse(responseCode = "200", description = "Lista de vinculaciones obtenida correctamente")
    @GetMapping("/paciente")
    public ResponseEntity<?> obtenerVinculacionesPorPaciente(
            @RequestParam String tipoDocumento,
            @RequestParam String idDocumento) {
        try {
            List<Map<String, String>> vinculaciones = vinculacionService.obtenerVinculacionesPorPaciente(tipoDocumento, idDocumento);
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
    public ResponseEntity<?> eliminarVinculacion(
        @RequestParam String pacienteId,
        @RequestParam String medicoId,
        @RequestParam String tipoVinculacionId) {
    
        try {
            VinculacionId id = new VinculacionId(pacienteId, medicoId);
            vinculacionService.eliminarVinculacion(id);
            return ResponseEntity.ok("Vinculación eliminada correctamente.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
    }    

}

