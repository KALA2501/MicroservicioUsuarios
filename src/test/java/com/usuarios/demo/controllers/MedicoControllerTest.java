package com.usuarios.demo.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.usuarios.demo.config.FirebaseMockConfig;
import com.usuarios.demo.config.TestSecurityConfig;
import com.usuarios.demo.entities.*;
import com.usuarios.demo.repositories.MedicoRepository;
import com.usuarios.demo.services.JwtService;
import com.usuarios.demo.services.MedicoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import({FirebaseMockConfig.class, TestSecurityConfig.class})
@WithMockUser(username = "medico@example.com", authorities = {"centro_medico", "ROLE_MEDICO"})
public class MedicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicoService medicoService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private MedicoRepository medicoRepository;

    @MockBean
    private FirebaseAuth firebaseAuth;

    private Medico mockMedico;

    @BeforeEach
    void setup() {
        mockMedico = new Medico();
        mockMedico.setPkId("medico123");
        mockMedico.setCorreo("medico@example.com");
        mockMedico.setNombre("Dr. House");

        when(jwtService.extractUsername("fake-jwt")).thenReturn("medico@example.com");
        when(jwtService.extractStringClaim("fake-jwt", "rol")).thenReturn("centro_medico");
        when(jwtService.extractFirstAvailableClaim("fake-jwt", "rol", "custom:rol"))
            .thenReturn("centro_medico");
        when(medicoService.obtenerPorCorreo("medico@example.com")).thenReturn(Optional.of(mockMedico));
        when(medicoRepository.findByCorreo("medico@example.com")).thenReturn(Optional.of(mockMedico));
        when(medicoService.obtenerPorId("medico123")).thenReturn(Optional.of(mockMedico));
    }

    @Test
    void testObtenerTodos() throws Exception {
        when(medicoService.obtenerTodos()).thenReturn(Collections.singletonList(mockMedico));

        mockMvc.perform(get("/api/medicos")
                        .header("Authorization", "Bearer fake-jwt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correo").value("medico@example.com"));
    }

    @Test
    void testObtenerPorId_Found() throws Exception {
        mockMvc.perform(get("/api/medicos/medico123")
                        .header("Authorization", "Bearer fake-jwt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("medico@example.com"));
    }

    @Test
    void testObtenerPorId_NotFound() throws Exception {
        when(medicoService.obtenerPorId("noexist")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/medicos/noexist")
                        .header("Authorization", "Bearer fake-jwt"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testObtenerDetallesMedico() throws Exception {
        mockMvc.perform(get("/api/medicos/details")
                        .header("Authorization", "Bearer fake-jwt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("medico@example.com"));
    }

    @Test
    void testObtenerPorCorreo_Found() throws Exception {
        mockMvc.perform(get("/api/medicos/buscar-por-correo")
                        .param("correo", "medico@example.com")
                        .header("Authorization", "Bearer fake-jwt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Dr. House"));
    }

    @Test
    void testObtenerPorCorreo_NotFound() throws Exception {
        when(medicoService.obtenerPorCorreo("notfound@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/medicos/buscar-por-correo")
                        .param("correo", "notfound@example.com")
                        .header("Authorization", "Bearer fake-jwt"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testObtenerMedicoId() throws Exception {
        mockMvc.perform(get("/api/medicos/medico-id")
                        .header("Authorization", "Bearer fake-jwt"))
                .andExpect(status().isOk())
                .andExpect(content().string("medico123"));
    }

    @Test
    void testGuardarMedico_Success() throws Exception {
        Medico nuevo = new Medico();
        nuevo.setPkId(UUID.randomUUID().toString());
        nuevo.setCorreo("nuevo@medico.com");
        nuevo.setNombre("Nuevo Médico");

        when(medicoService.obtenerCentroPorId(any())).thenReturn(Optional.of(new CentroMedico()));
        when(medicoService.obtenerTipoDocumentoPorId(any())).thenReturn(Optional.of(new TipoDocumento()));
        when(medicoService.guardar(any(Medico.class))).thenReturn(nuevo);

        UserRecord mockUserRecord = Mockito.mock(UserRecord.class);
        try (MockedStatic<FirebaseAuth> firebaseAuthStatic = Mockito.mockStatic(FirebaseAuth.class)) {
            firebaseAuthStatic.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);
            when(firebaseAuth.createUser(any())).thenReturn(mockUserRecord);

            mockMvc.perform(post("/api/medicos")
                            .header("Authorization", "Bearer fake-jwt")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "correo": "nuevo@medico.com",
                                  "nombre": "Nuevo Médico",
                                  "centroMedico": { "pkId": 1 },
                                  "tipoDocumento": { "id": 1 }
                                }
                            """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.correo").value("nuevo@medico.com"));
        }
    }

    @Test
    void testGuardarMedico_CorreoFaltante() throws Exception {
        when(medicoService.obtenerCentroPorId(any())).thenReturn(Optional.of(new CentroMedico()));
        when(medicoService.obtenerTipoDocumentoPorId(any())).thenReturn(Optional.of(new TipoDocumento()));

        mockMvc.perform(post("/api/medicos")
                        .header("Authorization", "Bearer fake-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "nombre": "Sin Correo",
                              "centroMedico": { "pkId": 1 },
                              "tipoDocumento": { "id": 1 }
                            }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Correo del médico es obligatorio"));
    }

    @Test
    void testActualizarMedico_Success() throws Exception {
        when(medicoService.obtenerCentroPorId(any())).thenReturn(Optional.of(new CentroMedico()));
        when(medicoService.obtenerTipoDocumentoPorId(any())).thenReturn(Optional.of(new TipoDocumento()));
        when(medicoService.guardar(any(Medico.class))).thenReturn(mockMedico);

        mockMvc.perform(put("/api/medicos/medico123")
                        .header("Authorization", "Bearer fake-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "correo": "medico@example.com",
                              "nombre": "Dr. House",
                              "centroMedico": { "pkId": 1 },
                              "tipoDocumento": { "id": 1 }
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Dr. House"));
    }

    @Test
    void testEliminarMedico_Success() throws Exception {
        when(medicoService.obtenerPorId("medico123")).thenReturn(Optional.of(mockMedico));

        mockMvc.perform(delete("/api/medicos/medico123")
                        .header("Authorization", "Bearer fake-jwt"))
                .andExpect(status().isOk())
                .andExpect(content().string("Médico eliminado correctamente"));
    }

    @Test
    void testObtenerPorCentro() throws Exception {
        when(medicoService.obtenerPorCentroMedico(1L)).thenReturn(Collections.singletonList(mockMedico));

        mockMvc.perform(get("/api/medicos/centro-medico/1")
                        .header("Authorization", "Bearer fake-jwt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correo").value("medico@example.com"));
    }

    @Test
    void testFiltrarMedicos() throws Exception {
        when(medicoService.filtrarMedicos("House", null, null)).thenReturn(Collections.singletonList(mockMedico));

        mockMvc.perform(get("/api/medicos/filtrar")
                        .param("nombre", "House")
                        .header("Authorization", "Bearer fake-jwt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Dr. House"));
    }

    @Test
    void testObtenerDetallesMedicoFirebase() throws Exception {
        UserRecord mockUserRecord = Mockito.mock(UserRecord.class);
        when(mockUserRecord.getEmail()).thenReturn("test@user.com");

        try (MockedStatic<FirebaseAuth> firebaseAuthStatic = Mockito.mockStatic(FirebaseAuth.class)) {
            firebaseAuthStatic.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);
            when(firebaseAuth.getUser("fakeUid")).thenReturn(mockUserRecord);

            mockMvc.perform(get("/api/medicos/firebase")
                            .param("uid", "fakeUid")
                            .header("Authorization", "Bearer fake-jwt"))
                    .andExpect(status().isOk());
        }
    }
}
