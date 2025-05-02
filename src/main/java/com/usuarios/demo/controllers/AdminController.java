package com.usuarios.demo.controllers;

import com.usuarios.demo.entities.Admin;
import com.usuarios.demo.services.AdminService;
import com.usuarios.demo.services.CentroMedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.auth.UserRecord;

import java.util.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;

import com.usuarios.demo.utils.CorregirCustomClaims;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administradores", description = "Endpoints para la gestión de administradores")
public class AdminController {

    @Autowired
    private AdminService service;

    @Autowired
    private CentroMedicoService centroMedicoService;

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

            // Inicializar las listas para cada rol
            String[] roles = { "centro_medico", "doctor", "medico", "paciente", "sin_rol" };
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
                System.out.println("🔍 Claims del usuario: " + claims);
                String rol = "sin_rol";
                if (claims != null && claims.containsKey("rol")) {
                    rol = claims.get("rol").toString();
                }

                if (!usuariosPorRol.containsKey(rol)) {
                    usuariosPorRol.put(rol, new ArrayList<>());
                    conteoPorRol.put(rol, 0);
                }

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
            // 1. Obtener información del usuario antes de eliminarlo
            UserRecord user = FirebaseAuth.getInstance().getUser(uid);
            String correo = user.getEmail();

            System.out.println("🔄 Iniciando proceso de eliminación para usuario: " + correo);

            // 2. Actualizar la solicitud primero
            try {
                service.actualizarSolicitudAlEliminarUsuario(correo);
                System.out.println("✅ Solicitud actualizada correctamente");
            } catch (Exception e) {
                System.out.println("⚠️ No se encontró solicitud para actualizar");
            }

            // 3. Eliminar de la base de datos MySQL
            try {
                centroMedicoService.eliminarPorCorreo(correo);
                System.out.println("✅ Usuario eliminado de MySQL");
            } catch (Exception e) {
                System.out.println("⚠️ Error al eliminar de MySQL: " + e.getMessage());
            }

            // 4. Finalmente eliminar de Firebase
            FirebaseAuth.getInstance().deleteUser(uid);
            System.out.println("✅ Usuario eliminado de Firebase");

            return ResponseEntity.ok("✅ Usuario eliminado completamente del sistema");

        } catch (Exception e) {
            System.err.println("❌ Error en el proceso de eliminación: " + e.getMessage());
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

    @Operation(summary = "Eliminar usuario por correo electrónico", description = "Elimina un usuario de Firebase y de la base de datos por su correo electrónico")
    @ApiResponse(responseCode = "200", description = "Usuario eliminado correctamente")
    @ApiResponse(responseCode = "500", description = "Error al eliminar el usuario")
    @DeleteMapping("/usuarios-firebase/email/{email}")
    public ResponseEntity<String> eliminarUsuarioPorEmail(@PathVariable String email) {
        try {
            // Eliminar de Firebase
            UserRecord user = FirebaseAuth.getInstance().getUserByEmail(email);
            FirebaseAuth.getInstance().deleteUser(user.getUid());

            // Eliminar de la base de datos
            service.eliminarUsuarioDeBaseDeDatos(email);

            return ResponseEntity.ok("✅ Usuario eliminado de Firebase y base de datos");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Error al eliminar usuario: " + e.getMessage());
        }
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
