package com.usuarios.demo.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ListUsersPage;
import com.google.firebase.auth.ExportedUserRecord;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CorregirCustomClaims {

    private static final Logger logger = LoggerFactory.getLogger(CorregirCustomClaims.class);

    public void corregirClaims() throws Exception {
        ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);

        for (ExportedUserRecord user : page.iterateAll()) {
            Map<String, Object> claims = user.getCustomClaims();
            Map<String, Object> nuevosClaims = new HashMap<>();

            // 1. Extraer cualquier rol existente
            String rolDetectado = null;
            if (claims != null) {
                if (claims.containsKey("rol")) {
                    rolDetectado = claims.get("rol").toString().toLowerCase();
                } else if (claims.containsKey("role")) {
                    rolDetectado = claims.get("role").toString().toLowerCase();
                }
            }

            // 2. Normalizar rol si aplica
            if ("doctor".equals(rolDetectado)) {
                rolDetectado = "medico";
            }

            // 3. Si encontramos un rol válido, lo actualizamos
            if (rolDetectado != null) {
                nuevosClaims.put("rol", rolDetectado);
                FirebaseAuth.getInstance().setCustomUserClaims(user.getUid(), nuevosClaims);
                logger.info("✅ Claim corregido para: {} → {}", user.getEmail(), rolDetectado);
            } else {
                logger.warn("⚠️ Usuario sin rol: {}", user.getEmail());
            }
        }

        logger.info("🎯 Corrección de claims completada.");
    }
}
