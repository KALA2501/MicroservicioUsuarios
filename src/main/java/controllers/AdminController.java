package controllers;

import entities.Admin;
import services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.auth.UserRecord;

import java.util.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administradores", description = "Endpoints para la gestión de administradores")
public class AdminController {

    @Autowired
    private AdminService service;

    @GetMapping
    public List<Admin> obtenerTodos() {
        return service.obtenerTodos();
    }
    @GetMapping("/usuarios-firebase")
    public ResponseEntity<Map<String, Object>> obtenerUsuariosFirebaseAgrupados() {
        try {
            Map<String, Object> response = new HashMap<>();
            Map<String, List<Map<String, Object>>> usuariosPorRol = new HashMap<>();
            Map<String, Integer> conteoPorRol = new HashMap<>();
            List<Map<String, Object>> usuariosSinRol = new ArrayList<>();
    
            // Inicializar las listas vacías para cada rol esperado
            usuariosPorRol.put("centro_medico", new ArrayList<>());
            usuariosPorRol.put("doctor", new ArrayList<>());
            usuariosPorRol.put("paciente", new ArrayList<>());
            usuariosPorRol.put("sin_rol", new ArrayList<>());
    
            // Inicializar conteos en 0
            conteoPorRol.put("centro_medico", 0);
            conteoPorRol.put("doctor", 0);
            conteoPorRol.put("paciente", 0);
            conteoPorRol.put("sin_rol", 0);
    
            System.out.println("\n🔥 INICIO: Obteniendo usuarios de Firebase...");
            ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);
            int totalUsuarios = 0;
            
            for (ExportedUserRecord user : page.iterateAll()) {
                totalUsuarios++;
                if (user.getEmail() == null || user.getEmail().equals("admin@kala.com")) {
                    System.out.println("❌ Usuario ignorado: " + (user.getEmail() == null ? "email nulo" : user.getEmail()));
                    continue;
                }
    
                Map<String, Object> userData = new HashMap<>();
                userData.put("uid", user.getUid());
                userData.put("email", user.getEmail());
                userData.put("disabled", user.isDisabled());
                userData.put("emailVerified", user.isEmailVerified());
                userData.put("displayName", user.getDisplayName());
                userData.put("phoneNumber", user.getPhoneNumber());
                userData.put("creationTime", user.getUserMetadata().getCreationTimestamp());
                userData.put("lastSignInTime", user.getUserMetadata().getLastSignInTimestamp());
    
                // Obtener rol de las claims
                String rol = "sin_rol";
                Map<String, Object> claims = user.getCustomClaims();
                System.out.println("\n👤 Procesando usuario: " + user.getEmail());
                System.out.println("-> Custom Claims: " + claims);
                
                if (claims != null && claims.containsKey("rol")) {
                    rol = claims.get("rol").toString();
                    System.out.println("✅ Rol encontrado: " + rol);
                } else {
                    System.out.println("⚠️ No tiene rol asignado");
                    usuariosSinRol.add(userData);
                }
    
                // Asegurarse de que el rol esté en la lista de roles esperados
                if (!usuariosPorRol.containsKey(rol)) {
                    System.out.println("⚠️ Rol no esperado: " + rol + " -> Asignando a sin_rol");
                    rol = "sin_rol";
                }
    
                usuariosPorRol.get(rol).add(userData);
                conteoPorRol.merge(rol, 1, Integer::sum);
                System.out.println("✅ Usuario agregado a rol: " + rol);
            }
    
            // Eliminar la lista de usuarios sin rol del mapa principal
            usuariosPorRol.remove("sin_rol");
            conteoPorRol.remove("sin_rol");
    
            // Imprimir resumen detallado
            System.out.println("\n📊 RESUMEN FINAL:");
            System.out.println("Total usuarios procesados: " + totalUsuarios);
            System.out.println("Usuarios sin rol: " + usuariosSinRol.size());
            System.out.println("\n🔥 DISTRIBUCIÓN POR ROL:");
            usuariosPorRol.forEach((rol, usuarios) -> {
                System.out.println("\nRol: " + rol + " - Cantidad: " + usuarios.size());
                usuarios.forEach(usuario -> 
                    System.out.println("  - " + usuario.get("email") + 
                        " (UID: " + usuario.get("uid") + 
                        ", Disabled: " + usuario.get("disabled") + ")")
                );
            });
    
            // Asegurarnos de que la estructura sea correcta antes de enviar
            response.put("usuariosPorRol", usuariosPorRol);
            response.put("conteoPorRol", conteoPorRol);
            response.put("totalUsuarios", conteoPorRol.values().stream().mapToInt(Integer::intValue).sum());
            response.put("usuariosSinRol", usuariosSinRol);
            response.put("totalUsuariosSinRol", usuariosSinRol.size());
    
            // Verificación final de la estructura
            System.out.println("\n🔥 ESTRUCTURA FINAL DE LA RESPUESTA:");
            System.out.println("usuariosPorRol: " + usuariosPorRol);
            System.out.println("conteoPorRol: " + conteoPorRol);
            System.out.println("totalUsuarios: " + response.get("totalUsuarios"));
            System.out.println("totalUsuariosSinRol: " + response.get("totalUsuariosSinRol"));
    
            return ResponseEntity.ok(response);
    
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al obtener usuarios de Firebase");
            errorResponse.put("mensaje", e.getMessage());
            errorResponse.put("stackTrace", e.getStackTrace());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    

    @DeleteMapping("/usuarios-firebase/{uid}")
    public ResponseEntity<String> eliminarUsuario(@PathVariable String uid) {
        try {
            FirebaseAuth.getInstance().deleteUser(uid);
            return ResponseEntity.ok("Usuario eliminado de Firebase");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al eliminar usuario");
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

}
