package com.usuarios.demo.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TipoDocumentoTest {

    @Test
    void testConstructorConArgumentos() {
        TipoDocumento tipoDoc = new TipoDocumento("CC", "Cédula de Ciudadanía");

        assertEquals("CC", tipoDoc.getId());
        assertEquals("Cédula de Ciudadanía", tipoDoc.getTipo());
    }

    @Test
    void testSettersYGetters() {
        TipoDocumento tipoDoc = new TipoDocumento();
        tipoDoc.setId("TI");
        tipoDoc.setTipo("Tarjeta de Identidad");

        assertEquals("TI", tipoDoc.getId());
        assertEquals("Tarjeta de Identidad", tipoDoc.getTipo());
    }

    @Test
    void testConstructorVacio() {
        TipoDocumento tipoDoc = new TipoDocumento();
        assertNotNull(tipoDoc);
    }
}
