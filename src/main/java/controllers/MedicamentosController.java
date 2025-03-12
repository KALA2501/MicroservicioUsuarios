package controllers;
import entities.*;
import services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/medicamentos")
public class MedicamentosController {
    @Autowired
    private MedicamentosService service;

    @GetMapping
    public List<Medicamentos> obtenerTodos() {
        return service.obtenerTodos();
    }
}
