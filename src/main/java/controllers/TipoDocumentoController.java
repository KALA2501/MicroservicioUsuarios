package controllers;
import entities.*;
import services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tipo-documento")
public class TipoDocumentoController {
    @Autowired
    private TipoDocumentoService service;

    @GetMapping
    public List<TipoDocumento> obtenerTodos() {
        return service.obtenerTodos();
    }
}
