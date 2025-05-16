package com.usuarios.demo.entities;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;

class PacienteTest {

    @Test
    void testAllArgsConstructor() {
        // Arrange - create objects for the fields (e.g., CentroMedico, TipoDocumento, ContactoEmergencia)
        CentroMedico centroMedico = new CentroMedico(); // Mock or real object
        TipoDocumento tipoDocumento = new TipoDocumento(); // Mock or real object
        ContactoEmergencia contactoEmergencia = new ContactoEmergencia(); // Mock or real object

        String pkId = "1234";
        String nombre = "Juan";
        String apellido = "Perez";
        String idDocumento = "D123456";
        Timestamp fechaNacimiento = new Timestamp(System.currentTimeMillis());
        int codigoCIE = 123;
        String telefono = "987654321";
        String email = "juan.perez@email.com";
        String direccion = "Calle Ficticia 123";
        int etapa = 1;
        String genero = "Masculino";
        String urlImagen = "http://example.com/imagen.jpg";

        // Act - create Paciente using all-args constructor
        Paciente paciente = new Paciente(
            pkId,
            centroMedico,
            tipoDocumento,
            contactoEmergencia,
            nombre,
            apellido,
            idDocumento,
            fechaNacimiento,
            codigoCIE,
            telefono,
            email,
            direccion,
            etapa,
            genero,
            urlImagen
        );

        // Assert - validate that the object is created with the correct values
        assertNotNull(paciente); // Ensure the object is not null
        assertEquals(pkId, paciente.getPkId());
        assertEquals(nombre, paciente.getNombre());
        assertEquals(apellido, paciente.getApellido());
        assertEquals(idDocumento, paciente.getIdDocumento());
        assertEquals(fechaNacimiento, paciente.getFechaNacimiento());
        assertEquals(codigoCIE, paciente.getCodigoCIE());
        assertEquals(telefono, paciente.getTelefono());
        assertEquals(email, paciente.getEmail());
        assertEquals(direccion, paciente.getDireccion());
        assertEquals(etapa, paciente.getEtapa());
        assertEquals(genero, paciente.getGenero());
        assertEquals(urlImagen, paciente.getUrlImagen());
        assertEquals(centroMedico, paciente.getCentroMedico());
        assertEquals(tipoDocumento, paciente.getTipoDocumento());
        assertEquals(contactoEmergencia, paciente.getContactoEmergencia());
    }
}

