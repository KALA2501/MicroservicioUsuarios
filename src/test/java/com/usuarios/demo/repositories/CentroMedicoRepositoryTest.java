package com.usuarios.demo.repositories;

import com.usuarios.demo.entities.CentroMedico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CentroMedicoRepositoryTest {

    @Autowired
    private CentroMedicoRepository repository;

    private CentroMedico crearCentroEjemplo() {
        CentroMedico centro = new CentroMedico();
        centro.setPkId(1L);
        centro.setNombre("Centro de Prueba");
        centro.setCorreo("centro@test.com");
        centro.setTelefono("3001234567");
        centro.setDireccion("Calle Falsa 123");
        centro.setUrlLogo("http://logo.test.com");
        return centro;
    }

    @Test
    @DisplayName("Debe retornar true si el correo ya existe")
    void testExistsByCorreo() {
        CentroMedico centro = crearCentroEjemplo();
        repository.save(centro);

        boolean exists = repository.existsByCorreo("centro@test.com");
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Debe retornar el centro médico cuando existe por correo")
    void testFindByCorreo() {
        CentroMedico centro = crearCentroEjemplo();
        repository.save(centro);

        Optional<CentroMedico> resultado = repository.findByCorreo("centro@test.com");
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Centro de Prueba");
    }

    @Test
    @DisplayName("Debe eliminar el centro médico por correo usando query nativa")
    void testDeleteByCorreo() {
        CentroMedico centro = crearCentroEjemplo();
        repository.save(centro);

        repository.deleteByCorreo("centro@test.com");

        boolean exists = repository.existsByCorreo("centro@test.com");
        assertThat(exists).isFalse();
    }
}
