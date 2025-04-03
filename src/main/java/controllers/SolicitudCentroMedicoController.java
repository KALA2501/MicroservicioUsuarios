package controllers;

import entities.SolicitudCentroMedico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.SolicitudCentroMedicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes-centro-medico")
public class SolicitudCentroMedicoController {

    @Autowired
    private SolicitudCentroMedicoService service;

    @PostMapping
    public ResponseEntity<?> crearSolicitud(@RequestBody SolicitudCentroMedico solicitud) {
        try {
            SolicitudCentroMedico guardada = service.guardarSolicitud(solicitud);
            return ResponseEntity.ok(guardada);
        } catch (RuntimeException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

        @PutMapping("/{id}/revertir")
    public ResponseEntity<?> revertirProcesado(@PathVariable Long id) {
        try {
            service.revertirProcesado(id);
            return ResponseEntity.ok("✅ Solicitud revertida");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @Operation(
        summary = "Eliminar una solicitud de centro médico",
        description = "Elimina una solicitud de centro médico por su ID"
    )
    @ApiResponse(responseCode = "200", description = "Solicitud eliminada correctamente")
    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarSolicitud(@PathVariable Long id) {
        try {
            service.eliminarPorId(id);
            return ResponseEntity.ok("Solicitud eliminada correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se pudo eliminar la solicitud: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/procesar")
    public ResponseEntity<String> procesarSolicitud(
            @PathVariable Long id,
            @RequestParam("rol") String rol) {
        service.procesarYCrearUsuario(id, rol);
        return ResponseEntity.ok("Usuario creado con rol: " + rol);
    }
    

    @GetMapping
    public ResponseEntity<List<SolicitudCentroMedico>> listar() {
        return ResponseEntity.ok(service.obtenerSolicitudes());
    }
}
