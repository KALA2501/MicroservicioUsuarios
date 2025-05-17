package com.usuarios.demo.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CentroMedicoTest {

    @Test
    void testConstructorYGetters() {
        CentroMedico centro = new CentroMedico(
            1L,
            "Centro Esperanza",
            "3100000000",
            "Calle Salud #123",
            "http://logo.png",
            "centro@esperanza.com"
        );

        assertEquals(1L, centro.getPkId());
        assertEquals("Centro Esperanza", centro.getNombre());
        assertEquals("3100000000", centro.getTelefono());
        assertEquals("Calle Salud #123", centro.getDireccion());
        assertEquals("http://logo.png", centro.getUrlLogo());
        assertEquals("centro@esperanza.com", centro.getCorreo());
    }

    @Test
    void testSetters() {
        CentroMedico centro = new CentroMedico();
        centro.setPkId(2L);
        centro.setNombre("Centro Vida");
        centro.setTelefono("3200000000");
        centro.setDireccion("Av. Principal #456");
        centro.setUrlLogo("http://vida-logo.com");
        centro.setCorreo("vida@centro.com");

        assertEquals(2L, centro.getPkId());
        assertEquals("Centro Vida", centro.getNombre());
        assertEquals("3200000000", centro.getTelefono());
        assertEquals("Av. Principal #456", centro.getDireccion());
        assertEquals("http://vida-logo.com", centro.getUrlLogo());
        assertEquals("vida@centro.com", centro.getCorreo());
    }

    @Test
    void testNoArgsConstructor() {
        CentroMedico centro = new CentroMedico();
        assertNotNull(centro);
    }
}
