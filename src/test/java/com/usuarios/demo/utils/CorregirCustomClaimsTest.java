package com.usuarios.demo.utils;

import com.google.firebase.auth.ExportedUserRecord;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.ListUsersPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.*;

import static org.mockito.Mockito.*;

class CorregirCustomClaimsTest {

    private CorregirCustomClaims corregirCustomClaims;

    @BeforeEach
    void setUp() {
        corregirCustomClaims = new CorregirCustomClaims();
    }

    @Test
    void testCorregirClaims_variosCasos() throws Exception {
        // Mock de ExportedUserRecord 1: con claim "rol" = doctor
        ExportedUserRecord user1 = mock(ExportedUserRecord.class);
        when(user1.getUid()).thenReturn("uid1");
        when(user1.getEmail()).thenReturn("user1@kala.com");
        when(user1.getCustomClaims()).thenReturn(Map.of("rol", "doctor"));

        // Mock de ExportedUserRecord 2: con claim "role" = paciente
        ExportedUserRecord user2 = mock(ExportedUserRecord.class);
        when(user2.getUid()).thenReturn("uid2");
        when(user2.getEmail()).thenReturn("user2@kala.com");
        when(user2.getCustomClaims()).thenReturn(Map.of("role", "paciente"));

        // Mock de ExportedUserRecord 3: sin claims
        ExportedUserRecord user3 = mock(ExportedUserRecord.class);
        when(user3.getUid()).thenReturn("uid3");
        when(user3.getEmail()).thenReturn("user3@kala.com");
        when(user3.getCustomClaims()).thenReturn(null);

        // Simular el iterador
        Iterable<ExportedUserRecord> users = List.of(user1, user2, user3);

        ListUsersPage page = mock(ListUsersPage.class);
        when(page.iterateAll()).thenReturn(users);

        try (MockedStatic<FirebaseAuth> mockedFirebase = mockStatic(FirebaseAuth.class)) {
            FirebaseAuth mockAuth = mock(FirebaseAuth.class);
            mockedFirebase.when(FirebaseAuth::getInstance).thenReturn(mockAuth);
            when(mockAuth.listUsers(null)).thenReturn(page);

            corregirCustomClaims.corregirClaims();

            // Verifica que se llamaron las correcciones para user1 y user2 (pero no para user3)
            verify(mockAuth).setCustomUserClaims(eq("uid1"), eq(Map.of("rol", "medico")));  // doctor → medico
            verify(mockAuth).setCustomUserClaims(eq("uid2"), eq(Map.of("rol", "paciente"))); // role → paciente
            verify(mockAuth, never()).setCustomUserClaims(eq("uid3"), any());
        }
    }
}
