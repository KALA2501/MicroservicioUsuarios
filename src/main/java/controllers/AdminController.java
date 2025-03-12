package controllers;
import entities.*;
import services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminService service;

    @GetMapping
    public List<Admin> obtenerTodos() {
        return service.obtenerTodos();
    }
}
