package controllers;
import entities.*;
import services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/centro-medico")
public class CentroMedicoController {
    @Autowired
    private CentroMedicoService service;

    @GetMapping
    public List<CentroMedico> obtenerTodos() {
        return service.obtenerTodos();
    }

    @GetMapping("/{id}")
    public Optional<CentroMedico> obtenerPorId(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @PostMapping
    public CentroMedico guardar(@RequestBody CentroMedico centroMedico) {
        return service.guardar(centroMedico);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.eliminar(id);
    }
}
