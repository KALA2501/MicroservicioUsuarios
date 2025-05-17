package com.usuarios.demo.repositories;

import com.usuarios.demo.entities.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PacienteRepositoryTest {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private CentroMedicoRepository centroMedicoRepository;

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Autowired
    private ContactoEmergenciaRepository contactoEmergenciaRepository;

    private String generarTelefonoUnico() {
        return "3" + System.nanoTime(); // Siempre único
    }

    private CentroMedico crearCentro() {
        CentroMedico centro = new CentroMedico();
        centro.setNombre("Centro Vital");
        centro.setCorreo("correo" + System.nanoTime() + "@test.com");
        centro.setTelefono(generarTelefonoUnico());
        centro.setDireccion("Calle 8 Sur");
        centro.setUrlLogo("http://logo.vital.com");
        return centroMedicoRepository.save(centro);
    }

    private TipoDocumento crearTipoDocumento() {
        TipoDocumento tipo = new TipoDocumento();
        tipo.setId("CC");
        tipo.setTipo("Cédula de Ciudadanía");
        return tipoDocumentoRepository.save(tipo);
    }

    private ContactoEmergencia crearContacto() {
        ContactoEmergencia contacto = new ContactoEmergencia();
        contacto.setNombre("Ana Pérez");
        contacto.setApellido("Gómez");
        contacto.setEmail("email" + System.nanoTime() + "@test.com");
        contacto.setTelefono(generarTelefonoUnico());
        contacto.setRelacion("Hermana");
        contacto.setDireccion("Carrera 10");
        return contactoEmergenciaRepository.save(contacto);
    }

    private Paciente crearPaciente() {
        CentroMedico centro = crearCentro();
        TipoDocumento tipoDoc = crearTipoDocumento();
        ContactoEmergencia contacto = crearContacto();

        Paciente paciente = new Paciente();
        paciente.setPkId("P" + System.nanoTime());
        paciente.setCentroMedico(centro);
        paciente.setTipoDocumento(tipoDoc);
        paciente.setContactoEmergencia(contacto);
        paciente.setNombre("Camila");
        paciente.setApellido("Gómez");
        paciente.setIdDocumento("112233" + System.nanoTime());
        paciente.setFechaNacimiento(Timestamp.valueOf("1995-06-15 00:00:00"));
        paciente.setCodigoCIE(123);
        paciente.setTelefono(generarTelefonoUnico());
        paciente.setEmail("paciente" + System.nanoTime() + "@correo.com");
        paciente.setDireccion("Av. Siempre Viva");
        paciente.setEtapa(3);
        paciente.setGenero("Femenino");
        paciente.setUrlImagen("http://img.com/camila.jpg");

        return pacienteRepository.save(paciente);
    }

    @Test
    @DisplayName("Debe encontrar por centro médico")
    void testFindByCentroMedicoPkId() {
        Paciente paciente = crearPaciente();
        List<Paciente> resultado = pacienteRepository.findByCentroMedicoPkId(paciente.getCentroMedico().getPkId());
        assertThat(resultado).isNotEmpty();
    }

    @Test
    @DisplayName("Debe encontrar por nombre o apellido (ignore case)")
    void testFindByNombreOrApellidoIgnoreCase() {
        Paciente paciente = crearPaciente();
        List<Paciente> resultado = pacienteRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase("cam", "gómez");
        assertThat(resultado).isNotEmpty();
    }

    @Test
    @DisplayName("Debe encontrar por tipo documento e ID")
    void testFindByTipoDocumentoAndIdDocumento() {
        Paciente paciente = crearPaciente();
        Optional<Paciente> resultado = pacienteRepository.findByTipoDocumento_IdAndIdDocumento(
                paciente.getTipoDocumento().getId(),
                paciente.getIdDocumento()
        );
        assertThat(resultado).isPresent();
    }

    @Test
    @DisplayName("Debe retornar true si existe paciente con ese teléfono")
    void testExistsByTelefono() {
        Paciente paciente = crearPaciente();
        boolean existe = pacienteRepository.existsByTelefono(paciente.getTelefono());
        assertThat(existe).isTrue();
    }

    @Test
    @DisplayName("Debe encontrar por email sin importar mayúsculas/minúsculas")
    void testFindByEmailIgnoreCase() {
        Paciente paciente = crearPaciente();
        Optional<Paciente> resultado = pacienteRepository.findByEmailIgnoreCase(paciente.getEmail().toUpperCase());
        assertThat(resultado).isPresent();
    }

    @Test
    @DisplayName("Debe encontrar pacientes por etapa")
    void testFindByEtapa() {
        Paciente paciente = crearPaciente();
        List<Paciente> resultado = pacienteRepository.findByEtapa(paciente.getEtapa());
        assertThat(resultado).isNotEmpty();
    }
}
