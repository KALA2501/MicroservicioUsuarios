package controllers;
import entities.*;
import services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dias-tomados")
public class DiasTomadosController {
    @Autowired
    private DiasTomadosService service;

    @GetMapping
    public List<DiasTomados> obtenerTodos() {
        return service.obtenerTodos();
    }
}
