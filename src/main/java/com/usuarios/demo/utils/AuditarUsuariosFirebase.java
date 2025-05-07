package com.usuarios.demo.utils; // Ajusta el paquete donde prefieras

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.ListUsersPage;

import java.util.Map;

/**
 * ⚡ Clase para auditar usuarios de Firebase y verificar sus custom claims.
 */
public class AuditarUsuariosFirebase {

    public void auditarUsuarios() {
        try {
            System.out.println("🚀 Iniciando auditoría de usuarios en Firebase...");

            ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);

            for (ExportedUserRecord user : page.iterateAll()) {
                Map<String, Object> claims = user.getCustomClaims();
                String rol = "sin rol";

                if (claims != null && claims.containsKey("rol")) {
                    rol = claims.get("rol").toString();
                }

                System.out.println("📋 Usuario: " + user.getEmail() + " | Rol: " + rol);
            }

            System.out.println("✅ Auditoría finalizada.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error durante la auditoría: " + e.getMessage());
        }
    }
}
