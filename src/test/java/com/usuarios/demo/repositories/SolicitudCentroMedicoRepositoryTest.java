package com.usuarios.demo.repositories;

import com.usuarios.demo.entities.SolicitudCentroMedico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class SolicitudCentroMedicoRepositoryTest {

    @Autowired
    private SolicitudCentroMedicoRepository repository;

    private SolicitudCentroMedico crearSolicitud() {
        SolicitudCentroMedico solicitud = new SolicitudCentroMedico();
        solicitud.setNombre("Clínica Demo");
        solicitud.setCorreo("clinica" + System.nanoTime() + "@test.com");
        solicitud.setTelefono("311" + System.nanoTime()); // único
        solicitud.setDireccion("Calle 123");
        solicitud.setUrlLogo("http://logo.com/logo.png");
        solicitud.setEstadoSolicitud(SolicitudCentroMedico.EstadoSolicitud.PENDIENTE);
        solicitud.setProcesado(false);
        return repository.save(solicitud);
    }

    @Test
    @DisplayName("Debe detectar si existe una solicitud por correo")
    void testExistsByCorreo() {
        SolicitudCentroMedico solicitud = crearSolicitud();
        boolean existe = repository.existsByCorreo(solicitud.getCorreo());
        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("Debe detectar si existe una solicitud por teléfono")
    void testExistsByTelefono() {
        SolicitudCentroMedico solicitud = crearSolicitud();
        boolean existe = repository.existsByTelefono(solicitud.getTelefono());
        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("Debe encontrar una solicitud por correo")
    void testFindByCorreo() {
        SolicitudCentroMedico solicitud = crearSolicitud();
        Optional<SolicitudCentroMedico> resultado = repository.findByCorreo(solicitud.getCorreo());
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Clínica Demo");
    }
}
