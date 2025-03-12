package controllers;
import entities.*;
import services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/contacto-emergencia")
public class ContactoEmergenciaController {
    @Autowired
    private ContactoEmergenciaService service;

    @GetMapping
    public List<ContactoEmergencia> obtenerTodos() {
        return service.obtenerTodos();
    }

    @PostMapping
    public ContactoEmergencia guardar(@RequestBody ContactoEmergencia contactoEmergencia) {
        return service.guardar(contactoEmergencia);
    }
}