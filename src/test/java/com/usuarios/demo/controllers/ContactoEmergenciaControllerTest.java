package com.usuarios.demo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usuarios.demo.config.FirebaseMockConfig;
import com.usuarios.demo.config.SecurityConfig;
import com.usuarios.demo.config.TestSecurityConfig;
import com.usuarios.demo.entities.ContactoEmergencia;
import com.usuarios.demo.services.ContactoEmergenciaService;
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

import java.util.List;
import java.util.Optional;

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
public class ContactoEmergenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactoEmergenciaService service;

    private ContactoEmergencia contacto;

    @BeforeEach
    void setUp() {
        contacto = new ContactoEmergencia();
        contacto.setPkId(1L);
        contacto.setNombre("Lucía Ramírez");
        contacto.setTelefono("3101234567");
        contacto.setRelacion("Hermana");
        contacto.setEmail("lucia@prueba.com");
        contacto.setDireccion("Calle Falsa 123");
    }

    @Test
    void testObtenerTodos() throws Exception {
        when(service.obtenerTodos()).thenReturn(List.of(contacto));

        mockMvc.perform(get("/api/contacto-emergencia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Lucía Ramírez"))
                .andExpect(jsonPath("$[0].telefono").value("3101234567"));
    }

    @Test
    void testGuardarContacto() throws Exception {
        when(service.guardar(any(ContactoEmergencia.class))).thenReturn(contacto);

        mockMvc.perform(post("/api/contacto-emergencia/crear")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(contacto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Lucía Ramírez"))
                .andExpect(jsonPath("$.telefono").value("3101234567"));
    }

    @Test
    void testBuscarPorTelefono_Encontrado() throws Exception {
        when(service.buscarPorTelefono("3101234567")).thenReturn(Optional.of(contacto));

        mockMvc.perform(get("/api/contacto-emergencia/por-telefono")
                        .param("telefono", "3101234567"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Lucía Ramírez"))
                .andExpect(jsonPath("$.telefono").value("3101234567"));
    }

    @Test
    void testBuscarPorTelefono_NoEncontrado() throws Exception {
        when(service.buscarPorTelefono("0000000000")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/contacto-emergencia/por-telefono")
                        .param("telefono", "0000000000"))
                .andExpect(status().isNoContent());
    }
}
