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

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        String email;

        try {
            email = jwtService.extractUsername(jwt);
            String rol;

            // Log the decoded JWT token for debugging purposes
            System.out.println("🔍 Decoded JWT Token: " + jwtService.decodeToken(jwt));

            // Verificar si el usuario es el administrador quemado
            if ("admin@kala.com".equals(email)) { // Usuario quemado
                rol = "ADMIN"; // Asignar rol ADMIN al administrador quemado
            } else {
                rol = jwtService.extractFirstAvailableClaim(jwt, "role", "rol"); // Extraer rol del token para otros usuarios
            }

            rol = rol.trim().toLowerCase(); 

            if (email != null && rol != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var authorities = List.of(new SimpleGrantedAuthority(rol)); 
                System.out.println("🎯 Authorities asignadas: " + authorities);

                var authToken = new UsernamePasswordAuthenticationToken(email, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                System.out.println("✅ Usuario autenticado: " + email);
                System.out.println("🔑 Rol asignado: " + rol);
            }
        } catch (Exception e) {
            System.err.println("❌ Error al procesar el token JWT: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o no verificable");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
