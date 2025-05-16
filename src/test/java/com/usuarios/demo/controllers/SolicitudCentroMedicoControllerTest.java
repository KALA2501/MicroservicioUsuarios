package com.usuarios.demo.controllers;

import com.usuarios.demo.entities.SolicitudCentroMedico;
import com.usuarios.demo.services.SolicitudCentroMedicoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SolicitudCentroMedicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private SolicitudCentroMedicoService solicitudCentroMedicoService;

    @InjectMocks
    private SolicitudCentroMedicoController solicitudCentroMedicoController;

    private SolicitudCentroMedico solicitud;

    @BeforeEach
    void setUp() {
        // Initialize SolicitudCentroMedico with required fields
        solicitud = new SolicitudCentroMedico();
        solicitud.setCorreo("test@kala.com");
        solicitud.setTelefono("1234567890");
        solicitud.setNombre("Centro Test");
        solicitud.setEstadoSolicitud(SolicitudCentroMedico.EstadoSolicitud.PENDIENTE);
        solicitud.setProcesado(false);
        solicitud.setDireccion("Dirección Test");
        solicitud.setUrlLogo("http://testlogo.com");
    }

    @Test
    void testCrearSolicitud_Conflict() throws Exception {
        // Mock service to simulate conflict (existing email)
        when(solicitudCentroMedicoService.guardarSolicitud(any(SolicitudCentroMedico.class)))
                .thenThrow(new RuntimeException("Ya existe una solicitud con ese correo"));

        // Perform POST request to simulate conflict
        mockMvc.perform(post("/api/solicitudes-centro-medico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\": \"test@kala.com\", \"telefono\": \"1234567890\", \"nombre\": \"Centro Test\", \"estado_solicitud\": \"PENDIENTE\", \"procesado\": false, \"direccion\": \"Dirección Test\", \"url_logo\": \"http://testlogo.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Ya existe una solicitud con ese correo"));
    }

    @Test
    void testEliminarSolicitud_NoEncontrada() throws Exception {
        // Mock service to simulate 'not found' case
        doThrow(new RuntimeException("Solicitud no encontrada")).when(solicitudCentroMedicoService).eliminarPorId(99L);

        // Perform DELETE request
        mockMvc.perform(delete("/api/solicitudes-centro-medico/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No se pudo eliminar la solicitud: Solicitud no encontrada"));
    }

    @Test
    void testRevertirSolicitud_NoEncontrada() throws Exception {
        // Mock service to simulate 'not found' scenario
        doThrow(new RuntimeException("Solicitud no encontrada")).when(solicitudCentroMedicoService).revertirProcesado(99L);

        // Perform PUT request
        mockMvc.perform(put("/api/solicitudes-centro-medico/99/revertir"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Solicitud no encontrada"));
    }
}
