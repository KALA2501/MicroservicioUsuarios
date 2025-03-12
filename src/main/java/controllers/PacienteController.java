package controllers;
import entities.*;
import services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {
    @Autowired
    private PacienteService service;

    @GetMapping
    public List<Paciente> obtenerTodos() {
        return service.obtenerTodos();
    }

    @GetMapping("/{id}")
    public Optional<Paciente> obtenerPorId(@PathVariable String id) {
        return service.obtenerPorId(id);
    }

    @PostMapping
    public Paciente guardar(@RequestBody Paciente paciente) {
        return service.guardar(paciente);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        service.eliminar(id);
    }
}
