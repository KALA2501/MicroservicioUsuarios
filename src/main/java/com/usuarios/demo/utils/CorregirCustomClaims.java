package com.usuarios.demo.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.auth.ExportedUserRecord;
import java.util.HashMap;
import java.util.Map;

public class CorregirCustomClaims {

    public void corregirClaims() throws Exception {
        ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);

        for (ExportedUserRecord user : page.iterateAll()) {
            Map<String, Object> claims = user.getCustomClaims();

            if (claims != null && claims.containsKey("rol")) {
                // ⚡ Encontramos un claim "rol" mal escrito
                String valorRol = claims.get("rol").toString();
                System.out.println("Corrigiendo claim para usuario: " + user.getEmail() + ", rol: " + valorRol);

                // Crear un nuevo mapa de claims correctos
                Map<String, Object> nuevosClaims = new HashMap<>(claims);
                nuevosClaims.remove("rol"); // Eliminar el viejo
                nuevosClaims.put("rol", valorRol); // Agregar el correcto

                // Actualizar claims en Firebase
                FirebaseAuth.getInstance().setCustomUserClaims(user.getUid(), nuevosClaims);

                System.out.println("✅ Claim corregido para: " + user.getEmail());
            }
        }

        System.out.println("🎯 Corrección de claims completada.");
    }
}
