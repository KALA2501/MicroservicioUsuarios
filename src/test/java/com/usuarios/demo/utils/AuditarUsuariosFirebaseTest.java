package com.usuarios.demo.utils;

import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ListUsersPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;

import static org.mockito.Mockito.*;

class AuditarUsuariosFirebaseTest {

    private AuditarUsuariosFirebase auditor;

    @BeforeEach
    void setup() {
        auditor = new AuditarUsuariosFirebase();
    }

    @Test
    void testAuditarUsuarios_WithRoles() throws Exception {
        ExportedUserRecord user1 = mock(ExportedUserRecord.class);
        ExportedUserRecord user2 = mock(ExportedUserRecord.class);

        when(user1.getEmail()).thenReturn("user1@kala.com");
        when(user1.getCustomClaims()).thenReturn(Map.of("rol", "medico"));

        when(user2.getEmail()).thenReturn("user2@kala.com");
        when(user2.getCustomClaims()).thenReturn(Collections.emptyMap());

        List<ExportedUserRecord> mockUsers = Arrays.asList(user1, user2);

        ListUsersPage mockPage = mock(ListUsersPage.class);
        when(mockPage.iterateAll()).thenReturn(mockUsers);

        FirebaseAuth mockAuth = mock(FirebaseAuth.class);
        when(mockAuth.listUsers(null)).thenReturn(mockPage);

        try (MockedStatic<FirebaseAuth> authStatic = Mockito.mockStatic(FirebaseAuth.class)) {
            authStatic.when(FirebaseAuth::getInstance).thenReturn(mockAuth);

            // Ejecutar el método de auditoría
            auditor.auditarUsuarios();
        }
    }

    @Test
    void testAuditarUsuarios_WithException() throws Exception {
        FirebaseAuth mockAuth = mock(FirebaseAuth.class);
        when(mockAuth.listUsers(null)).thenThrow(new RuntimeException("🔥 Boom!"));

        try (MockedStatic<FirebaseAuth> authStatic = Mockito.mockStatic(FirebaseAuth.class)) {
            authStatic.when(FirebaseAuth::getInstance).thenReturn(mockAuth);

            // Este no lanza, pero sí loguea la excepción
            auditor.auditarUsuarios();
        }
    }
}
