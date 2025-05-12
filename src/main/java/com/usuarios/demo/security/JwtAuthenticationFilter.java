package com.usuarios.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.usuarios.demo.services.JwtService;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

   @Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) throws ServletException, IOException {

    final String authHeader = request.getHeader("Authorization");

    System.out.println("📥 Encabezado Authorization recibido: " + authHeader); // <-- LOG

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        System.out.println("⛔ No se proporcionó un token válido");
        filterChain.doFilter(request, response);
        return;
    }

    final String jwt = authHeader.substring(7);
    String email;

    try {
        email = jwtService.extractUsername(jwt);
        String rol = jwtService.extractFirstAvailableClaim(jwt, "role", "rol");

        System.out.println("🧾 Email extraído del token: " + email); // <-- LOG
        System.out.println("🎭 Rol extraído del token: " + rol);     // <-- LOG

        if (rol != null) {
            rol = rol.trim().toLowerCase();
        } else {
            rol = "sin_rol";
            System.out.println("⚠️ Rol no definido, asignado como 'sin_rol'");
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            var authorities = List.of(new SimpleGrantedAuthority(rol));
            System.out.println("🔐 Authorities asignadas: " + authorities);

            var authToken = new UsernamePasswordAuthenticationToken(email, null, authorities);
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            System.out.println("✅ Usuario autenticado: " + email);
            System.out.println("🛡️ Autorización con rol: " + rol);
        }

    } catch (Exception e) {
        System.err.println("❌ Error al procesar token JWT: " + e.getMessage());
        e.printStackTrace();
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o no verificable");
        return;
    }

    filterChain.doFilter(request, response);
}

}
