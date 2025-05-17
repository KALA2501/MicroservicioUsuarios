package com.usuarios.demo.controllers;

import com.usuarios.demo.config.FirebaseMockConfig;
import com.usuarios.demo.config.SecurityConfig;
import com.usuarios.demo.config.TestSecurityConfig;
import com.usuarios.demo.entities.TipoVinculacion;
import com.usuarios.demo.services.TipoVinculacionService;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
public class TipoVinculacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TipoVinculacionService service;

    private TipoVinculacion tipo;

    @BeforeEach
    void setup() {
        tipo = new TipoVinculacion();
        tipo.setId("VINC1");
        tipo.setTipo("Medico");
        tipo.setDescripcion("Tratamiento General");
    }

    @Test
    void testObtenerTodos() throws Exception {
        when(service.obtenerTodos()).thenReturn(List.of(tipo));

        mockMvc.perform(get("/api/tipo-vinculacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("VINC1"))
                .andExpect(jsonPath("$[0].tipo").value("Medico"))
                .andExpect(jsonPath("$[0].descripcion").value("Tratamiento General"));
    }

}
