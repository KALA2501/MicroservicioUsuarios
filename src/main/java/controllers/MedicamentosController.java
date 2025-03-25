package controllers;

import entities.Medicamentos;
import services.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/medicamentos")
@Tag(name = "Medicamentos", description = "Gestión de medicamentos de pacientes")
public class MedicamentosController {

    @Autowired
    private PacienteService pacienteService;

    @Operation(summary = "Guardar paciente con medicamentos (nuevo)")
    @PostMapping("/nuevo")
    public ResponseEntity<?> guardarConMedicamentos(
        @RequestParam String pacienteId,
        @RequestBody List<Medicamentos> medicamentos) {
        try {
            var paciente = pacienteService.obtenerPorId(pacienteId)
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            var guardado = pacienteService.guardarConMedicamentos(paciente, medicamentos);
            return ResponseEntity.ok(guardado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Actualizar medicamentos de un paciente")
    @PutMapping("/actualizar")
    public ResponseEntity<?> actualizarMedicamentos(
        @RequestParam String pacienteId,
        @RequestBody List<Medicamentos> medicamentos) {
        try {
            var actualizado = pacienteService.actualizarMedicamentos(pacienteId, medicamentos);
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Eliminar un medicamento por ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarMedicamento(@PathVariable Long id) {
        try {
            pacienteService.eliminarMedicamento(id);
            return ResponseEntity.ok("Medicamento eliminado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Listar medicamentos de un paciente")
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<?> obtenerMedicamentosDePaciente(@PathVariable String pacienteId) {
        try {
            var medicamentos = pacienteService.obtenerMedicamentosDePaciente(pacienteId);
            return ResponseEntity.ok(medicamentos);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
    }
}
