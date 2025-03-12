package controllers;
import entities.*;
import services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/medicos")
public class MedicoController {
    @Autowired
    private MedicoService service;

    @GetMapping
    public List<Medico> obtenerTodos() {
        return service.obtenerTodos();
    }

    @GetMapping("/{id}")
    public Optional<Medico> obtenerPorId(@PathVariable String id) {
        return service.obtenerPorId(id);
    }

    @PostMapping
    public Medico guardar(@RequestBody Medico medico) {
        return service.guardar(medico);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable String id) {
        service.eliminar(id);
    }
}