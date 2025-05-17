package com.usuarios.demo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.usuarios.demo.config.FirebaseMockConfig;
import com.usuarios.demo.config.SecurityConfig;
import com.usuarios.demo.config.TestSecurityConfig;
import com.usuarios.demo.entities.*;
import com.usuarios.demo.repositories.*;
import com.usuarios.demo.services.JwtService;
import com.usuarios.demo.services.PacienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({FirebaseMockConfig.class, TestSecurityConfig.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ComponentScan(excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                SecurityConfig.class,
                com.usuarios.demo.security.JwtAuthenticationFilter.class
        })
})
public class PacienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PacienteService pacienteService;

    @MockBean
    private ContactoEmergenciaRepository contactoEmergenciaRepository;

    @MockBean
    private CentroMedicoRepository centroMedicoRepository;

    @MockBean
    private TipoDocumentoRepository tipoDocumentoRepository;

    @MockBean
    private FirebaseAuth firebaseAuth;

    @MockBean
    private UserRecord userRecord;

    @MockBean
    private VinculacionRepository vinculacionRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private MedicoRepository medicoRepository;

    @MockBean
    private TipoVinculacionRepository tipoVinculacionRepository;

    private final String pacienteId = "paciente123";
    private final String email = "testuser@example.com";

    @BeforeEach
    void setup() {
        Paciente paciente = new Paciente();
        paciente.setPkId(pacienteId);
        paciente.setEmail(email);

        when(pacienteService.obtenerPorId(pacienteId)).thenReturn(Optional.of(paciente));
        when(pacienteService.obtenerTodos()).thenReturn(List.of(paciente));
        when(pacienteService.buscarPorCorreo(email)).thenReturn(paciente); // For testObtenerMiPerfil
    }

    @Test
    void testObtenerTodos() throws Exception {
        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testObtenerPorId_Found() throws Exception {
        mockMvc.perform(get("/api/pacientes/" + pacienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void testObtenerPorId_NotFound() throws Exception {
        when(pacienteService.obtenerPorId("noexist")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/pacientes/noexist"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testObtenerMiPerfil() throws Exception {
        mockMvc.perform(get("/api/pacientes/mi-perfil")
                        .with(user(email)))
                .andExpect(status().isOk())
                .andExpect(content().string(pacienteId));
    }

    @Test
    void testGuardarPaciente() throws Exception {
        String uniqueEmail = "test." + System.currentTimeMillis() + "@correo.com";

        Map<String, Object> pacienteJson = new HashMap<>();
        pacienteJson.put("pkId", 1L);
        pacienteJson.put("nombre", "Juan");
        pacienteJson.put("apellido", "Pérez");
        pacienteJson.put("idDocumento", "123456789");
        pacienteJson.put("telefono", "3210000000");
        pacienteJson.put("email", uniqueEmail);
        pacienteJson.put("etapa", 1);
        pacienteJson.put("codigoCIE", 100);
        pacienteJson.put("fechaNacimiento", "2000-01-01");
        pacienteJson.put("genero", "masculino");
        pacienteJson.put("direccion", "Calle Falsa 123");
        pacienteJson.put("urlImagen", "url");
        pacienteJson.put("tipoDocumento", Map.of("id", "CC"));
        pacienteJson.put("centroMedico", Map.of("pkId", 1L));
        pacienteJson.put("contactoEmergencia", Map.of("pkId", 1L, "telefono", "3210000001"));

        // Simulate non-existing patient by email
        when(pacienteService.buscarPorCorreo(uniqueEmail)).thenThrow(new RuntimeException("Not found"));

        when(contactoEmergenciaRepository.findByTelefono("3210000001")).thenReturn(Optional.empty());
        when(contactoEmergenciaRepository.findById(1L)).thenReturn(Optional.of(new ContactoEmergencia()));
        when(centroMedicoRepository.findById(1L)).thenReturn(Optional.of(new CentroMedico()));
        when(tipoDocumentoRepository.findById("CC")).thenReturn(Optional.of(new TipoDocumento()));

        // Mock FirebaseAuth: simulate user not found
        doThrow(new RuntimeException("User not found")).when(firebaseAuth).getUserByEmail(anyString());

        when(firebaseAuth.createUser(any(UserRecord.CreateRequest.class))).thenReturn(userRecord);
        when(userRecord.getUid()).thenReturn("mocked-uid");
        doNothing().when(firebaseAuth).setCustomUserClaims(eq("mocked-uid"), anyMap());

        Paciente pacienteGuardado = new Paciente();
        pacienteGuardado.setPkId("123");
        when(pacienteService.guardarConValidacion(any())).thenReturn(pacienteGuardado);

        mockMvc.perform(post("/api/pacientes/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(pacienteJson)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pkId").value("123"));
    }

    @Test
    void testObtenerPacientesPorCentro() throws Exception {
        Paciente paciente = new Paciente();
        paciente.setPkId("id123");
        when(pacienteService.obtenerPorCentroMedico(1L)).thenReturn(List.of(paciente));

        mockMvc.perform(get("/api/pacientes/centro-medico/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pkId").value("id123"));
    }

    @Test
    void testObtenerMedicosVinculados() throws Exception {
        Vinculacion vinculacion = mock(Vinculacion.class);
        Medico medico = new Medico();
        medico.setNombre("Dr. Extraño");
        medico.setEspecialidad("Neurocirugía");

        when(vinculacion.getMedico()).thenReturn(medico);
        when(vinculacion.getFechaVinculado()).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(vinculacionRepository.findByPaciente_PkId("paciente123")).thenReturn(List.of(vinculacion));

        mockMvc.perform(get("/api/pacientes/paciente123/medicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Dr. Extraño"));
    }

    @Test
    void testObtenerPacientesDelMedico() throws Exception {
        Paciente paciente = new Paciente();
        paciente.setPkId("pac123");

        when(jwtService.extractUsername("fake-jwt")).thenReturn("medico@kala.com");
        when(pacienteService.obtenerPacientesDelMedico("fake-jwt")).thenReturn(List.of(paciente));

        mockMvc.perform(get("/api/pacientes/del-medico")
                        .header("Authorization", "Bearer fake-jwt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pkId").value("pac123"));
    }

        @Test
        void testRegistrarPacienteCompleto() throws Exception {
        String uniqueEmail = "paciente." + System.currentTimeMillis() + "@correo.com";

        Map<String, Object> requestJson = new HashMap<>();
        requestJson.put("nombre", "Ana");
        requestJson.put("apellido", "Ramírez");
        requestJson.put("idDocumento", "987654321");
        requestJson.put("telefono", "3001234567");
        requestJson.put("email", uniqueEmail);
        requestJson.put("direccion", "Cra 50 #20-30");
        requestJson.put("genero", "femenino");
        requestJson.put("urlImagen", "https://image.png");
        requestJson.put("etapa", 2);
        requestJson.put("codigoCIE", 200);
        requestJson.put("fechaNacimiento", "1995-05-20");

        // Related entities
        requestJson.put("tipoDocumento", Map.of("id", "CC"));
        requestJson.put("centroMedico", Map.of("pkId", 1L));
        requestJson.put("contactoEmergencia", Map.of("pkId", 1L, "telefono", "3110000000"));
        requestJson.put("medico", Map.of("pkId", "med123"));
        requestJson.put("tipoVinculacion", Map.of("id", "VINC1"));

        // Mocks for validation and saving
        when(pacienteService.buscarPorCorreo(uniqueEmail)).thenThrow(new RuntimeException("not found"));
        when(contactoEmergenciaRepository.findById(1L)).thenReturn(Optional.of(new ContactoEmergencia()));
        when(medicoRepository.findById("med123")).thenReturn(Optional.of(new Medico()));
        when(tipoVinculacionRepository.findById("VINC1")).thenReturn(Optional.of(new TipoVinculacion()));
        when(centroMedicoRepository.findById(1L)).thenReturn(Optional.of(new CentroMedico()));
        when(tipoDocumentoRepository.findById("CC")).thenReturn(Optional.of(new TipoDocumento()));

        // Firebase mocks
        doThrow(new RuntimeException("not found")).when(firebaseAuth).getUserByEmail(anyString());
        when(firebaseAuth.createUser(any(UserRecord.CreateRequest.class))).thenReturn(userRecord);
        when(userRecord.getUid()).thenReturn("firebase-mocked-id");
        doNothing().when(firebaseAuth).setCustomUserClaims(eq("firebase-mocked-id"), anyMap());

        // Final save
        Paciente savedPaciente = new Paciente();
        savedPaciente.setPkId("saved-id");
        when(pacienteService.guardarConValidacion(any(Paciente.class), any(Medico.class), any(TipoVinculacion.class)))
                .thenReturn(savedPaciente);

        mockMvc.perform(post("/api/pacientes/registrar-completo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestJson)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pkId").value("saved-id"));
        }

         @Test
        void testObtenerMedicosVinculados_NotFound() throws Exception {
        when(vinculacionRepository.findByPaciente_PkId("sinMedicos")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/pacientes/sinMedicos/medicos"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("El paciente no tiene médicos vinculados."));
        }

        @Test
        void testGuardarPaciente_ExistePaciente() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("email", "existente@correo.com");

        when(pacienteService.buscarPorCorreo("existente@correo.com")).thenReturn(new Paciente());

        mockMvc.perform(post("/api/pacientes/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(data)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Ya existe un paciente con este correo."));
        }

        @Test
        void testGuardarPaciente_ExisteContacto() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("email", "nuevo@correo.com");
        data.put("contactoEmergencia", Map.of("telefono", "12345678"));

        when(pacienteService.buscarPorCorreo("nuevo@correo.com")).thenThrow(new RuntimeException("not found"));
        when(contactoEmergenciaRepository.findByTelefono("12345678")).thenReturn(Optional.of(new ContactoEmergencia()));

        mockMvc.perform(post("/api/pacientes/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(data)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Ya existe un contacto de emergencia con este teléfono."));
        }

        @Test
        void testRegistrarPacienteCompleto_MedicoNoEncontrado() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("email", "paciente@correo.com");
        data.put("medico", Map.of("pkId", "inexistente"));

        when(pacienteService.buscarPorCorreo("paciente@correo.com")).thenThrow(new RuntimeException("not found"));
        when(medicoRepository.findById("inexistente")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/pacientes/registrar-completo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(data)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Médico no encontrado")));
        }


        @Test
        void testObtenerPacientesDelMedico_InternalError() throws Exception {
        when(jwtService.extractUsername("bad-token")).thenReturn("medico@kala.com");
        when(pacienteService.obtenerPacientesDelMedico("bad-token")).thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/api/pacientes/del-medico")
                        .header("Authorization", "Bearer bad-token"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error")));
        }


}