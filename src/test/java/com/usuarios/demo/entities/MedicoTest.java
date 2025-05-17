package com.usuarios.demo.entities;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class MedicoTest {

    @Test
    void testConstructorConArgumentos() {
        CentroMedico centro = new CentroMedico(1L, "Centro Vida", "3210000000", "Calle 45", "http://logo.com", "centro@vida.com");
        TipoDocumento tipoDoc = new TipoDocumento("CC", "Cédula");
        Date fechaNacimiento = new Date();

        Medico medico = new Medico(
                "med001",
                centro,
                "Ana",
                "López",
                tipoDoc,
                "12345678",
                fechaNacimiento,
                "Pediatra",
                "Neonatología",
                "3109999999",
                "Cra 10 #20-30",
                "Femenino",
                "TP-001",
                "http://foto.com/ana.jpg",
                "ana@medico.com"
        );

        assertEquals("med001", medico.getPkId());
        assertEquals(centro, medico.getCentroMedico());
        assertEquals("Ana", medico.getNombre());
        assertEquals("López", medico.getApellido());
        assertEquals(tipoDoc, medico.getTipoDocumento());
        assertEquals("12345678", medico.getIdDocumento());
        assertEquals(fechaNacimiento, medico.getFechaNacimiento());
        assertEquals("Pediatra", medico.getProfesion());
        assertEquals("Neonatología", medico.getEspecialidad());
        assertEquals("3109999999", medico.getTelefono());
        assertEquals("Cra 10 #20-30", medico.getDireccion());
        assertEquals("Femenino", medico.getGenero());
        assertEquals("TP-001", medico.getTarjetaProfesional());
        assertEquals("http://foto.com/ana.jpg", medico.getUrlImagen());
        assertEquals("ana@medico.com", medico.getCorreo());
    }

    @Test
    void testSettersYGetters() {
        Medico medico = new Medico();
        CentroMedico centro = new CentroMedico();
        TipoDocumento tipoDoc = new TipoDocumento();
        Date fecha = new Date();

        medico.setPkId("m001");
        medico.setCentroMedico(centro);
        medico.setNombre("Carlos");
        medico.setApellido("Pérez");
        medico.setTipoDocumento(tipoDoc);
        medico.setIdDocumento("87654321");
        medico.setFechaNacimiento(fecha);
        medico.setProfesion("Cardiólogo");
        medico.setEspecialidad("Intervencionista");
        medico.setTelefono("3000000000");
        medico.setDireccion("Av 1 #2-3");
        medico.setGenero("Masculino");
        medico.setTarjetaProfesional("TP-999");
        medico.setUrlImagen("http://imagen.com");
        medico.setCorreo("carlos@hospital.com");

        assertEquals("m001", medico.getPkId());
        assertEquals(centro, medico.getCentroMedico());
        assertEquals("Carlos", medico.getNombre());
        assertEquals("Pérez", medico.getApellido());
        assertEquals(tipoDoc, medico.getTipoDocumento());
        assertEquals("87654321", medico.getIdDocumento());
        assertEquals(fecha, medico.getFechaNacimiento());
        assertEquals("Cardiólogo", medico.getProfesion());
        assertEquals("Intervencionista", medico.getEspecialidad());
        assertEquals("3000000000", medico.getTelefono());
        assertEquals("Av 1 #2-3", medico.getDireccion());
        assertEquals("Masculino", medico.getGenero());
        assertEquals("TP-999", medico.getTarjetaProfesional());
        assertEquals("http://imagen.com", medico.getUrlImagen());
        assertEquals("carlos@hospital.com", medico.getCorreo());
    }

    @Test
    void testConstructorVacio() {
        Medico medico = new Medico();
        assertNotNull(medico);
    }
}
