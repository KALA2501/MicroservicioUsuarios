package com.usuarios.demo.controllers;

import com.usuarios.demo.DemoApplication;
import com.usuarios.demo.entities.CentroMedico;
import com.usuarios.demo.services.CentroMedicoService;
import com.usuarios.demo.services.JwtService;
import com.usuarios.demo.repositories.CentroMedicoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;
import java.util.List;

@SpringBootTest(classes = DemoApplication.class)
@AutoConfigureMockMvc
class CentroMedicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CentroMedicoService service;

    @MockBean
    private CentroMedicoRepository repository;

    @MockBean
    private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(username = "testuser", roles = "USER")  // Simulate authenticated user
    void testObtenerTodos() throws Exception {
        CentroMedico centro = new CentroMedico();
        centro.setNombre("Hospital Central");

        Mockito.when(service.obtenerTodos()).thenReturn(List.of(centro));

        mockMvc.perform(get("/api/centro-medico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Hospital Central"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")  // Simulate authenticated user
    void testObtenerPorId_Encontrado() throws Exception {
        CentroMedico centro = new CentroMedico();
        centro.setPkId(1L);
        centro.setNombre("Clínica Norte");

        Mockito.when(service.obtenerPorId(1L)).thenReturn(Optional.of(centro));

        mockMvc.perform(get("/api/centro-medico/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Clínica Norte"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")  // Simulate authenticated user
    void testObtenerPorId_NoEncontrado() throws Exception {
        Mockito.when(service.obtenerPorId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/centro-medico/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Centro médico no encontrado"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")  // Simulate authenticated user
    void testGuardar_Exito() throws Exception {
        CentroMedico input = new CentroMedico();
        input.setNombre("Nuevo Centro");

        CentroMedico saved = new CentroMedico();
        saved.setPkId(1L);
        saved.setNombre("Nuevo Centro");

        Mockito.when(service.registrarCentroMedico(any(CentroMedico.class))).thenReturn(saved);

        mockMvc.perform(post("/api/centro-medico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pkId").value(1L))
                .andExpect(jsonPath("$.nombre").value("Nuevo Centro"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")  // Simulate authenticated user
    void testGuardar_Error() throws Exception {
        Mockito.when(service.registrarCentroMedico(any(CentroMedico.class)))
                .thenThrow(new RuntimeException("Error de validación"));

        mockMvc.perform(post("/api/centro-medico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CentroMedico())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error al guardar centro médico")));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")  // Simulate authenticated user
    void testEliminar_Exito() throws Exception {
        mockMvc.perform(delete("/api/centro-medico/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Centro médico eliminado correctamente"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")  // Simulate authenticated user
    void testEliminar_Error() throws Exception {
        Mockito.doThrow(new RuntimeException("No existe")).when(service).eliminar(1L);

        mockMvc.perform(delete("/api/centro-medico/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No se pudo eliminar: No existe"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")  // Simulate authenticated user
    void testActualizar_Exito() throws Exception {
        CentroMedico nuevosDatos = new CentroMedico();
        nuevosDatos.setNombre("Actualizado");

        CentroMedico actualizado = new CentroMedico();
        actualizado.setPkId(1L);
        actualizado.setNombre("Actualizado");

        Mockito.when(service.actualizar(eq(1L), any(CentroMedico.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/centro-medico/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nuevosDatos)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Actualizado"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")  // Simulate authenticated user
    void testActualizar_Error() throws Exception {
        Mockito.when(service.actualizar(eq(1L), any(CentroMedico.class)))
                .thenThrow(new RuntimeException("Centro no encontrado"));

        mockMvc.perform(put("/api/centro-medico/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CentroMedico())))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Centro no encontrado"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")  // Simulate authenticated user
    void testBuscarPorCorreo_Encontrado() throws Exception {
        CentroMedico centro = new CentroMedico();
        centro.setCorreo("correo@kala.com");

        Mockito.when(repository.findByCorreo("correo@kala.com")).thenReturn(Optional.of(centro));

        mockMvc.perform(get("/api/centro-medico/buscar-por-correo")
                        .param("correo", "correo@kala.com"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")  // Simulate authenticated user
    void testBuscarPorCorreo_NoEncontrado() throws Exception {
        Mockito.when(repository.findByCorreo("desconocido@kala.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/centro-medico/buscar-por-correo")
                        .param("correo", "desconocido@kala.com"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Centro no encontrado"));
    }
}
