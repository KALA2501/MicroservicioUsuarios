package controllers;

import entities.ContactoEmergencia;
import services.ContactoEmergenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/contacto-emergencia")
public class ContactoEmergenciaController {

    @Autowired
    private ContactoEmergenciaService service;

    @Operation(
        summary = "Obtener todos los contactos de emergencia",
        description = "Retorna una lista de todos los contactos de emergencia registrados."
    )
    @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente")
    @GetMapping
    public List<ContactoEmergencia> obtenerTodos() {
        return service.obtenerTodos();
    }

    @Operation(
        summary = "Guardar un contacto de emergencia",
        description = "Recibe un objeto ContactoEmergencia en el cuerpo de la petición y lo almacena."
    )
    @ApiResponse(responseCode = "200", description = "Contacto guardado correctamente")
    @PostMapping
    public ContactoEmergencia guardar(@RequestBody ContactoEmergencia contactoEmergencia) {
        return service.guardar(contactoEmergencia);
    }
}
