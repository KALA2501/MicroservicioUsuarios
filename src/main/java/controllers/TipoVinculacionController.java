package controllers;
import entities.*;
import services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tipo-vinculacion")
public class TipoVinculacionController {
    @Autowired
    private TipoVinculacionService service;

    @GetMapping
    public List<TipoVinculacion> obtenerTodos() {
        return service.obtenerTodos();
    }
}
