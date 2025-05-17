package com.usuarios.demo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usuarios.demo.config.FirebaseMockConfig;
import com.usuarios.demo.config.SecurityConfig;
import com.usuarios.demo.config.TestSecurityConfig;
import com.usuarios.demo.entities.TipoDocumento;
import com.usuarios.demo.services.TipoDocumentoService;
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

import java.util.Optional;
import java.util.List;

import static org.mockito.Mockito.*;
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
public class TipoDocumentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TipoDocumentoService service;

    private TipoDocumento tipo;

    @BeforeEach
    void setup() {
        tipo = new TipoDocumento();
        tipo.setId("CC");
        tipo.setTipo("Cédula de Ciudadanía");
    }

    @Test
    void testObtenerTodos() throws Exception {
        when(service.obtenerTodos()).thenReturn(List.of(tipo));

        mockMvc.perform(get("/api/tipo-documento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("CC"))
                .andExpect(jsonPath("$[0].tipo").value("Cédula de Ciudadanía"));
    }

    @Test
    void testObtenerPorId_Encontrado() throws Exception {
        when(service.obtenerPorId("CC")).thenReturn(Optional.of(tipo));

        mockMvc.perform(get("/api/tipo-documento/CC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("CC"))
                .andExpect(jsonPath("$.tipo").value("Cédula de Ciudadanía"));
    }

    @Test
    void testObtenerPorId_NoEncontrado() throws Exception {
        when(service.obtenerPorId("TI")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tipo-documento/TI"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Tipo de documento no encontrado"));
    }

    @Test
    void testGuardarTipoDocumento() throws Exception {
        when(service.guardar(any(TipoDocumento.class))).thenReturn(tipo);

        mockMvc.perform(post("/api/tipo-documento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(tipo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("CC"))
                .andExpect(jsonPath("$.tipo").value("Cédula de Ciudadanía"));
    }

    @Test
    void testGuardarTipoDocumento_Error() throws Exception {
        when(service.guardar(any())).thenThrow(new RuntimeException("Error de prueba"));

        mockMvc.perform(post("/api/tipo-documento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(tipo)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Error al crear tipo de documento")));
    }

    @Test
    void testEliminarTipoDocumento_Exito() throws Exception {
        doNothing().when(service).eliminar("CC");

        mockMvc.perform(delete("/api/tipo-documento/CC"))
                .andExpect(status().isOk())
                .andExpect(content().string("Tipo de documento eliminado correctamente"));
    }

    @Test
    void testEliminarTipoDocumento_Error() throws Exception {
        doThrow(new RuntimeException("No encontrado")).when(service).eliminar("TI");

        mockMvc.perform(delete("/api/tipo-documento/TI"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No se pudo eliminar")));
    }
}
