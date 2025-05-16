package com.usuarios.demo.controllers;

import com.google.firebase.auth.*;
import com.usuarios.demo.config.FirebaseMockConfig;
import com.usuarios.demo.config.SecurityConfig;
import com.usuarios.demo.config.TestSecurityConfig;
import com.usuarios.demo.controllers.AdminController;
import com.usuarios.demo.entities.Admin;
import com.usuarios.demo.services.AdminService;
import com.usuarios.demo.services.CentroMedicoService;
import com.usuarios.demo.services.MedicoService;
import com.usuarios.demo.services.PacienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.mockito.MockedStatic;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({FirebaseMockConfig.class, TestSecurityConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ComponentScan(excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
})
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FirebaseAuth firebaseAuth;

    @MockBean
    private AdminService adminService;

    @MockBean
    private CentroMedicoService centroMedicoService;

    @MockBean
    private MedicoService medicoService;

    @MockBean
    private PacienteService pacienteService;

    private static MockedStatic<FirebaseAuth> firebaseAuthStaticMock;

    private final String uid = "uid123";

    @BeforeAll
    void mockFirebaseStatic() {
        firebaseAuthStaticMock = mockStatic(FirebaseAuth.class);
        firebaseAuthStaticMock.when(FirebaseAuth::getInstance).thenAnswer(invocation -> firebaseAuth);
    }

    @AfterAll
    void closeFirebaseStatic() {
        firebaseAuthStaticMock.close();
    }

    @BeforeEach
    void setupFirebaseMocks() throws Exception {
        UserRecord userRecord = mock(UserRecord.class);
        when(userRecord.getEmail()).thenReturn("testuser@example.com");
        when(userRecord.getDisplayName()).thenReturn("Test User");
        when(userRecord.getCustomClaims()).thenReturn(Map.of("rol", "medico"));
        when(firebaseAuth.getUser(uid)).thenReturn(userRecord);
        when(firebaseAuth.updateUser(any(UserRecord.UpdateRequest.class))).thenReturn(userRecord);
        doNothing().when(firebaseAuth).deleteUser(uid);
        doNothing().when(firebaseAuth).setCustomUserClaims(eq(uid), anyMap());

        ExportedUserRecord exportedUser = mock(ExportedUserRecord.class);
        when(exportedUser.getUid()).thenReturn(uid);
        when(exportedUser.getEmail()).thenReturn("medico@kala.com");
        when(exportedUser.getCustomClaims()).thenReturn(Map.of("rol", "medico"));
        when(exportedUser.getDisplayName()).thenReturn("Dr. House");
        when(exportedUser.getPhoneNumber()).thenReturn("+1234567890");
        when(exportedUser.isDisabled()).thenReturn(false);
        when(exportedUser.isEmailVerified()).thenReturn(true);

        UserMetadata metadata = mock(UserMetadata.class);
        when(metadata.getCreationTimestamp()).thenReturn(123456789L);
        when(metadata.getLastSignInTimestamp()).thenReturn(987654321L);
        when(exportedUser.getUserMetadata()).thenReturn(metadata);

        ListUsersPage listUsersPage = mock(ListUsersPage.class);
        when(listUsersPage.iterateAll()).thenReturn(List.of(exportedUser));
        when(firebaseAuth.listUsers(null)).thenReturn(listUsersPage);
    }

    // Tests for the existing functionality

    @Test
    void getAllAdmins() throws Exception {
        List<Admin> admins = List.of(new Admin("a1", "Admin One"), new Admin("a2", "Admin Two"));
        when(adminService.obtenerTodos()).thenReturn(admins);

        mockMvc.perform(get("/api/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getFirebaseUsersGrouped() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios-firebase"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteoPorRol.medico").value(1));
    }

    @Test
    void deleteUser() throws Exception {
        mockMvc.perform(delete("/api/admin/usuarios-firebase/" + uid))
                .andExpect(status().isOk())
                .andExpect(content().string("✅ Usuario eliminado completamente del sistema"));
    }

    @Test
    void deleteUserWithError() throws Exception {
        // Simulating an error while deleting the user
        doThrow(new RuntimeException("Firebase deletion failed")).when(firebaseAuth).deleteUser(uid);

        mockMvc.perform(delete("/api/admin/usuarios-firebase/" + uid))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("❌ Error al eliminar usuario: Firebase deletion failed"));
    }

    @Test
    void reactivateUser() throws Exception {
        mockMvc.perform(put("/api/admin/usuarios-firebase/" + uid + "/reactivar"))
                .andExpect(status().isOk())
                .andExpect(content().string("Usuario reactivado en Firebase"));
    }

    @Test
    void getUserRole() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios-firebase/" + uid + "/rol"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("medico"));
    }

    @Test
    void updateUserRole() throws Exception {
        mockMvc.perform(put("/api/admin/usuarios-firebase/" + uid + "/rol")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rol\": \"paciente\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("paciente"));
    }

    @Test
    void deleteByEmail() throws Exception {
        String email = "medico@correo.com";
        doNothing().when(medicoService).eliminarPorCorreo(email);

        mockMvc.perform(delete("/api/admin/medico-por-correo")
                        .param("correo", email))
                .andExpect(status().isOk())
                .andExpect(content().string("Eliminado"));
    }

    @Test
    void correctClaims() throws Exception {
        mockMvc.perform(get("/api/admin/corregir-claims"))
                .andExpect(status().isOk());
    }

    // Test when no Firebase users are returned
    @Test
    void getFirebaseUsersGrouped_NoUsers() throws Exception {
        // Simulating no users in Firebase
        when(firebaseAuth.listUsers(null)).thenReturn(mock(ListUsersPage.class));

        mockMvc.perform(get("/api/admin/usuarios-firebase"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteoPorRol.paciente").value(0))
                .andExpect(jsonPath("$.conteoPorRol.medico").value(0))
                .andExpect(jsonPath("$.conteoPorRol.centro_medico").value(0))
                .andExpect(jsonPath("$.conteoPorRol.sin_rol").value(0));
    }


    // Test when the user doesn't exist in the Firebase database
    @Test
    void deleteUser_NotFound() throws Exception {
        // Simulating an error where FirebaseAuthException is thrown when user is not found
        FirebaseAuthException authException = mock(FirebaseAuthException.class);
        when(authException.getMessage()).thenReturn("User not found");
        doThrow(authException).when(firebaseAuth).deleteUser(uid);

        mockMvc.perform(delete("/api/admin/usuarios-firebase/" + uid))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("❌ Error al eliminar usuario: User not found"));
    }
}