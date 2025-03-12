package controllers;
import entities.*;
import services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vinculacion")
public class VinculacionController {
    @Autowired
    private VinculacionService service;

    @GetMapping
    public List<Vinculacion> obtenerTodos() {
        return service.obtenerTodos();
    }
}
