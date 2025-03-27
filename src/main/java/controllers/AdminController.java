package controllers;

import entities.Admin;
import services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ListUsersPage;

import java.util.ArrayList;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administradores", description = "Endpoints para la gestión de administradores")
public class AdminController {

    @Autowired
    private AdminService service;

    @Operation(
        summary = "Obtener todos los administradores",
        description = "Retorna una lista de todos los administradores registrados en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente")
    @GetMapping
    public List<Admin> obtenerTodos() {
        return service.obtenerTodos();
    }

    @GetMapping("/usuarios-firebase")
public ResponseEntity<List<String>> obtenerUsuariosFirebase() {
    try {
        List<String> correos = new ArrayList<>();

        ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);
        for (ExportedUserRecord user : page.iterateAll()) {
            String email = user.getEmail();
            if (!email.equals("admin@kala.com")) {
                correos.add(email);
            }
        }

        return ResponseEntity.ok(correos);
    } catch (Exception e) {
        return ResponseEntity.status(500).body(null);
    }
}

}
