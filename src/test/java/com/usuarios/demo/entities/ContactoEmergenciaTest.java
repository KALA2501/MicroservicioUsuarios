package com.usuarios.demo.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContactoEmergenciaTest {

    @Test
    void testConstructorConArgumentos() {
        ContactoEmergencia contacto = new ContactoEmergencia(
            1L,
            "Lucía",
            "Pérez",
            "Hermana",
            "Calle 123",
            "3204567890",
            "lucia@correo.com"
        );

        assertEquals(1L, contacto.getPkId());
        assertEquals("Lucía", contacto.getNombre());
        assertEquals("Pérez", contacto.getApellido());
        assertEquals("Hermana", contacto.getRelacion());
        assertEquals("Calle 123", contacto.getDireccion());
        assertEquals("3204567890", contacto.getTelefono());
        assertEquals("lucia@correo.com", contacto.getEmail());
    }

    @Test
    void testSettersYGetters() {
        ContactoEmergencia contacto = new ContactoEmergencia();
        contacto.setPkId(2L);
        contacto.setNombre("Juan");
        contacto.setApellido("Gómez");
        contacto.setRelacion("Padre");
        contacto.setDireccion("Av. Siempre Viva 742");
        contacto.setTelefono("3000000000");
        contacto.setEmail("juan@correo.com");

        assertEquals(2L, contacto.getPkId());
        assertEquals("Juan", contacto.getNombre());
        assertEquals("Gómez", contacto.getApellido());
        assertEquals("Padre", contacto.getRelacion());
        assertEquals("Av. Siempre Viva 742", contacto.getDireccion());
        assertEquals("3000000000", contacto.getTelefono());
        assertEquals("juan@correo.com", contacto.getEmail());
    }

    @Test
    void testConstructorVacio() {
        ContactoEmergencia contacto = new ContactoEmergencia();
        assertNotNull(contacto);
    }
}
