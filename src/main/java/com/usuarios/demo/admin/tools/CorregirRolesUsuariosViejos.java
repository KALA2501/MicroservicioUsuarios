package com.usuarios.demo.admin.tools;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;

import java.util.HashMap;
import java.util.Map;

/**
 * ⚡ Clase temporal para corregir roles de usuarios antiguos sin `customClaims`.
 * 📌 Ejecutar una sola vez y luego borrar para mantener el código limpio.
 */
public class CorregirRolesUsuariosViejos {

    public void corregirUsuarios() {
        try {
            System.out.println("🚀 Iniciando corrección de roles de usuarios antiguos...");

            asignarRol("paciente1@gmail.com", "paciente");
            asignarRol("medico@gmail.com", "medico");
            asignarRol("paciente@gmail.com", "paciente");

            System.out.println("✅ Corrección completada. Ahora puedes eliminar esta clase.");
        } catch (Exception e) {
            System.err.println("❌ Error durante la corrección: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void asignarRol(String email, String rol) throws Exception {
        // Obtener el usuario por email
        UserRecord user = FirebaseAuth.getInstance().getUserByEmail(email);

        // Crear los nuevos claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", rol);

        // Asignar los claims en Firebase
        FirebaseAuth.getInstance().setCustomUserClaims(user.getUid(), claims);

        System.out.println("✅ Rol '" + rol + "' asignado a " + email);
    }
}
