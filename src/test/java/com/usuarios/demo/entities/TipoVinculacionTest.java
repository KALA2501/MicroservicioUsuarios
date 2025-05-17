package com.usuarios.demo.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TipoVinculacionTest {

    @Test
    void testConstructorConArgumentos() {
        TipoVinculacion tipo = new TipoVinculacion("TV01", "MEDICO", "Médico vinculado al centro");

        assertEquals("TV01", tipo.getId());
        assertEquals("MEDICO", tipo.getTipo());
        assertEquals("Médico vinculado al centro", tipo.getDescripcion());
    }

    @Test
    void testSettersYGetters() {
        TipoVinculacion tipo = new TipoVinculacion();
        tipo.setId("TV02");
        tipo.setTipo("PACIENTE");
        tipo.setDescripcion("Paciente registrado en el centro");

        assertEquals("TV02", tipo.getId());
        assertEquals("PACIENTE", tipo.getTipo());
        assertEquals("Paciente registrado en el centro", tipo.getDescripcion());
    }

    @Test
    void testConstructorVacio() {
        TipoVinculacion tipo = new TipoVinculacion();
        assertNotNull(tipo);
    }
}
