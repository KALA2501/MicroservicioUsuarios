package controllers;
import entities.*;
import services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/comentarios")
public class ComentarioController {
    @Autowired
    private ComentarioService service;

    @GetMapping
    public List<Comentario> obtenerTodos() {
        return service.obtenerTodos();
    }

    @PostMapping
    public Comentario guardar(@RequestBody Comentario comentario) {
        return service.guardar(comentario);
    }
}
