package controllers;

import entities.SolicitudCentroMedico;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import services.SolicitudCentroMedicoService;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes-centro-medico")
@CrossOrigin("*")
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

    @PutMapping("/{id}/procesar")
    public ResponseEntity<?> marcarProcesado(@PathVariable Long id) {
        try {
            service.procesarYCrearUsuario(id);
            return ResponseEntity.ok("Solicitud marcada como procesada y usuario creado");
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }



    @GetMapping
    public ResponseEntity<List<SolicitudCentroMedico>> listar() {
        return ResponseEntity.ok(service.obtenerSolicitudes());
    }
}
