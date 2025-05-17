package com.usuarios.demo.entities;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class VinculacionTest {

    @Test
    void testConstructorConArgumentos() {
        Paciente paciente = new Paciente();
        paciente.setPkId("PAC001");

        Medico medico = new Medico();
        medico.setPkId("MED001");

        TipoVinculacion tipo = new TipoVinculacion("TV01", "MEDICO", "Vinculación de prueba");
        Timestamp fecha = new Timestamp(System.currentTimeMillis());

        Vinculacion vinculacion = new Vinculacion(paciente, medico, fecha, tipo);

        assertEquals(paciente, vinculacion.getPaciente());
        assertEquals(medico, vinculacion.getMedico());
        assertEquals(fecha, vinculacion.getFechaVinculado());
        assertEquals(tipo, vinculacion.getTipoVinculacion());
    }

    @Test
    void testSettersYGetters() {
        Vinculacion vinculacion = new Vinculacion();

        Paciente paciente = new Paciente();
        paciente.setPkId("PAC002");

        Medico medico = new Medico();
        medico.setPkId("MED002");

        TipoVinculacion tipo = new TipoVinculacion("TV02", "PACIENTE", "Vinculación paciente");
        Timestamp fecha = new Timestamp(new Date().getTime());

        vinculacion.setPaciente(paciente);
        vinculacion.setMedico(medico);
        vinculacion.setFechaVinculado(fecha);
        vinculacion.setTipoVinculacion(tipo);

        assertEquals("PAC002", vinculacion.getPaciente().getPkId());
        assertEquals("MED002", vinculacion.getMedico().getPkId());
        assertEquals(fecha, vinculacion.getFechaVinculado());
        assertEquals("TV02", vinculacion.getTipoVinculacion().getId());
    }

    @Test
    void testConstructorVacio() {
        Vinculacion vinculacion = new Vinculacion();
        assertNotNull(vinculacion);
    }
}
