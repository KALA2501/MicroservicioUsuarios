package com.usuarios.demo.controllers;

import com.usuarios.demo.entities.Admin;
import com.usuarios.demo.services.AdminService;
import com.usuarios.demo.services.CentroMedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.usuarios.demo.services.MedicoService;
import com.usuarios.demo.services.PacienteService;

import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.auth.UserRecord;

import java.util.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import com.usuarios.demo.utils.CorregirCustomClaims;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administradores", description = "Endpoints para la gestión de administradores")
public class AdminController {

    @Autowired
    private AdminService service;

    @Autowired
    private CentroMedicoService centroMedicoService;

    @Autowired
    private MedicoService medicoService;

    @Autowired
    private PacienteService pacienteService;


    @GetMapping
    public List<Admin> obtenerTodos() {
        return service.obtenerTodos();
    }

    @GetMapping("/usuarios-firebase")
    public ResponseEntity<Map<String, Object>> obtenerUsuariosFirebaseAgrupados() {
    try {
        System.out.println("\n🔄 Iniciando obtención de usuarios de Firebase...");

        Map<String, Object> response = new HashMap<>();
        Map<String, List<Map<String, Object>>> usuariosPorRol = new HashMap<>();
        Map<String, Integer> conteoPorRol = new HashMap<>();

        // Inicializar las listas para los roles esperados
        String[] roles = { "centro_medico", "medico", "paciente", "sin_rol" };
        for (String rol : roles) {
            usuariosPorRol.put(rol, new ArrayList<>());
            conteoPorRol.put(rol, 0);
        }

        ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);
        System.out.println("📥 Obteniendo usuarios de Firebase...");

        for (ExportedUserRecord user : page.iterateAll()) {
            if (user.getEmail() == null || user.getEmail().equals("admin@kala.com")) {
                continue;
            }

            System.out.println("\n👤 Procesando usuario: " + user.getEmail());
            Map<String, Object> userData = new HashMap<>();
            userData.put("uid", user.getUid());
            userData.put("email", user.getEmail());
            userData.put("disabled", user.isDisabled());
            userData.put("emailVerified", user.isEmailVerified());
            userData.put("displayName", user.getDisplayName());
            userData.put("phoneNumber", user.getPhoneNumber());
            userData.put("creationTime", user.getUserMetadata().getCreationTimestamp());
            userData.put("lastSignInTime", user.getUserMetadata().getLastSignInTimestamp());

            Map<String, Object> claims = user.getCustomClaims();
            String rol = "sin_rol";

            if (claims != null && claims.containsKey("rol")) {
                rol = claims.get("rol").toString().toLowerCase();

                // 🔄 Normalizar rol: convertir "doctor" en "medico"
                if (rol.equals("doctor")) {
                    rol = "medico";
                }
            }

            System.out.println("🔍 Rol detectado (normalizado): " + rol);

            // Asegurarse de que exista la lista para este rol
            usuariosPorRol.computeIfAbsent(rol, k -> new ArrayList<>());
            conteoPorRol.computeIfAbsent(rol, k -> 0);

            usuariosPorRol.get(rol).add(userData);
            conteoPorRol.put(rol, conteoPorRol.get(rol) + 1);
            System.out.println("✅ Usuario agregado al grupo: " + rol);
        }

        response.put("usuariosPorRol", usuariosPorRol);
        response.put("conteoPorRol", conteoPorRol);
        response.put("totalUsuarios", conteoPorRol.values().stream().mapToInt(Integer::intValue).sum());

        System.out.println("\n📊 Resumen de usuarios por rol:");
        usuariosPorRol.forEach((rol, usuarios) -> {
            System.out.println(rol + ": " + usuarios.size() + " usuarios");
            usuarios.forEach(u -> System.out.println("  - " + u.get("email")));
        });

        return ResponseEntity.ok(response);

    } catch (Exception e) {
        System.err.println("❌ Error al obtener usuarios: " + e.getMessage());
        e.printStackTrace();
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Error al obtener usuarios de Firebase");
        errorResponse.put("mensaje", e.getMessage());
        return ResponseEntity.status(500).body(errorResponse);
    }
}

    @Operation(summary = "Eliminar usuario por UID", description = "Elimina un usuario de Firebase y de la base de datos por su UID")
    @ApiResponse(responseCode = "200", description = "Usuario eliminado correctamente")
    @ApiResponse(responseCode = "500", description = "Error al eliminar el usuario")
    @DeleteMapping("/usuarios-firebase/{uid}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable String uid) {
    try {
        UserRecord user = FirebaseAuth.getInstance().getUser(uid);
        String correo = user.getEmail();

        System.out.println("🔄 Iniciando eliminación de usuario: " + correo);

        // 1. Intentar actualizar solicitud si existe
        try {
            service.actualizarSolicitudAlEliminarUsuario(correo);
        } catch (Exception e) {
            System.out.println("⚠️ No se encontró solicitud para actualizar");
        }

        // 2. Obtener rol del usuario
        String rol = "sin_rol";
        Map<String, Object> claims = user.getCustomClaims();
        if (claims != null && claims.containsKey("rol")) {
            rol = claims.get("rol").toString().toLowerCase();
        }

        System.out.println("🔍 Rol detectado para eliminación: " + rol);

        // 3. Eliminar de la base de datos según el rol
        switch (rol) {
            case "centro_medico":
                centroMedicoService.eliminarPorCorreo(correo);
                break;
            case "medico":
                medicoService.eliminarPorCorreo(correo);
                break;
            case "paciente":
                pacienteService.eliminarPorCorreo(correo);
                break;
            default:
                System.out.println("⚠️ Rol no reconocido. No se eliminó en DB.");
        }

        // 4. Eliminar de Firebase
        FirebaseAuth.getInstance().deleteUser(uid);
        System.out.println("✅ Usuario eliminado completamente");

        return ResponseEntity.ok("✅ Usuario eliminado completamente del sistema");

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500)
                .body("❌ Error al eliminar usuario: " + e.getMessage());
    }
}


    @PutMapping("/usuarios-firebase/{uid}/reactivar")
    public ResponseEntity<String> reactivarUsuario(@PathVariable String uid) {
        try {
            FirebaseAuth.getInstance().updateUser(new UserRecord.UpdateRequest(uid).setDisabled(false));
            return ResponseEntity.ok("Usuario reactivado en Firebase");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al reactivar usuario");
        }
    }
   
    @GetMapping("/usuarios-firebase/{uid}/rol")
    public ResponseEntity<Map<String, Object>> obtenerRolUsuario(@PathVariable String uid) {
        try {
            UserRecord user = FirebaseAuth.getInstance().getUser(uid);
            Map<String, Object> claims = user.getCustomClaims();

            Map<String, Object> response = new HashMap<>();
            response.put("uid", uid);
            response.put("email", user.getEmail());
            response.put("rol", claims != null ? claims.get("rol") : "sin_rol");
            response.put("claims", claims);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener rol del usuario");
            errorResponse.put("mensaje", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PutMapping("/usuarios-firebase/{uid}/rol")
    public ResponseEntity<Map<String, Object>> actualizarRolUsuario(
            @PathVariable String uid,
            @RequestBody Map<String, String> request) {
        try {
            String nuevoRol = request.get("rol");
            if (nuevoRol == null || nuevoRol.isEmpty()) {
                throw new IllegalArgumentException("El rol no puede estar vacío");
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("rol", nuevoRol);
            FirebaseAuth.getInstance().setCustomUserClaims(uid, claims);

            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "Rol actualizado correctamente");
            response.put("uid", uid);
            response.put("rol", nuevoRol);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al actualizar rol del usuario");
            errorResponse.put("mensaje", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
   
    // Updated to delegate deletion logic to MedicoService
    @DeleteMapping("/medico-por-correo")
    public ResponseEntity<String> eliminarPorCorreo(@RequestParam String correo) {
        medicoService.eliminarPorCorreo(correo);
        return ResponseEntity.ok("Eliminado");
    }


    @GetMapping("/corregir-claims")
    public ResponseEntity<String> corregirClaims() {
        try {
            new CorregirCustomClaims().corregirClaims();
            return ResponseEntity.ok("✅ Claims corregidos exitosamente");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Error al corregir claims: " + e.getMessage());
        }
    }

}
