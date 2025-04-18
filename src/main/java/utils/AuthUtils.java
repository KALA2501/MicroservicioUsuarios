package utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtils {

    /**
     * Devuelve el correo del usuario autenticado extraído del SecurityContext.
     */
    public static String getCorreoAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("No hay usuario autenticado en el contexto de seguridad");
        }

        return authentication.getName(); // O (String) authentication.getPrincipal(); si lo prefieres
    }
}