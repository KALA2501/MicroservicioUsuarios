package com.usuarios.demo.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SolicitudCentroMedicoTest {

    @Test
    void testConstructorConArgumentos() {
        SolicitudCentroMedico solicitud = new SolicitudCentroMedico(
                1L,
                "Centro Luz",
                "Av. Principal 123",
                "centro@luz.com",
                "3100000000",
                "http://logo.com/luz.png",
                SolicitudCentroMedico.EstadoSolicitud.ACEPTADA,
                true
        );

        assertEquals(1L, solicitud.getId());
        assertEquals("Centro Luz", solicitud.getNombre());
        assertEquals("Av. Principal 123", solicitud.getDireccion());
        assertEquals("centro@luz.com", solicitud.getCorreo());
        assertEquals("3100000000", solicitud.getTelefono());
        assertEquals("http://logo.com/luz.png", solicitud.getUrlLogo());
        assertEquals(SolicitudCentroMedico.EstadoSolicitud.ACEPTADA, solicitud.getEstadoSolicitud());
        assertTrue(solicitud.isProcesado());
    }

    @Test
    void testSettersYGetters() {
        SolicitudCentroMedico solicitud = new SolicitudCentroMedico();

        solicitud.setId(2L);
        solicitud.setNombre("Centro Vida");
        solicitud.setDireccion("Calle 45 Sur");
        solicitud.setCorreo("vida@centro.com");
        solicitud.setTelefono("3201234567");
        solicitud.setUrlLogo("http://vida.com/logo.png");
        solicitud.setEstadoSolicitud(SolicitudCentroMedico.EstadoSolicitud.RECHAZADA);
        solicitud.setProcesado(false);

        assertEquals(2L, solicitud.getId());
        assertEquals("Centro Vida", solicitud.getNombre());
        assertEquals("Calle 45 Sur", solicitud.getDireccion());
        assertEquals("vida@centro.com", solicitud.getCorreo());
        assertEquals("3201234567", solicitud.getTelefono());
        assertEquals("http://vida.com/logo.png", solicitud.getUrlLogo());
        assertEquals(SolicitudCentroMedico.EstadoSolicitud.RECHAZADA, solicitud.getEstadoSolicitud());
        assertFalse(solicitud.isProcesado());
    }

    @Test
    void testConstructorVacioYValoresPorDefecto() {
        SolicitudCentroMedico solicitud = new SolicitudCentroMedico();
        assertNotNull(solicitud);
        assertEquals(SolicitudCentroMedico.EstadoSolicitud.PENDIENTE, solicitud.getEstadoSolicitud());
        assertFalse(solicitud.isProcesado());
    }

    @Test
    void testEnumEstadoSolicitud() {
        assertEquals("PENDIENTE", SolicitudCentroMedico.EstadoSolicitud.PENDIENTE.name());
        assertEquals("ACEPTADA", SolicitudCentroMedico.EstadoSolicitud.ACEPTADA.name());
        assertEquals("RECHAZADA", SolicitudCentroMedico.EstadoSolicitud.RECHAZADA.name());
    }
}
