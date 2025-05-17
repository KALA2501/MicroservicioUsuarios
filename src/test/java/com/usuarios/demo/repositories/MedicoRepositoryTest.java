package com.usuarios.demo.repositories;

import com.usuarios.demo.entities.CentroMedico;
import com.usuarios.demo.entities.Medico;
import com.usuarios.demo.entities.TipoDocumento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class MedicoRepositoryTest {

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private CentroMedicoRepository centroMedicoRepository;

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    private CentroMedico centro;
    private TipoDocumento tipoDocumento;

    @BeforeEach
    void setUp() {
        centro = new CentroMedico();
        centro.setNombre("Clínica Central");
        centro.setCorreo("centro@correo.com");
        centro.setTelefono("3200000000");
        centro.setDireccion("Calle 123");
        centro.setUrlLogo("http://logo.com");
        centroMedicoRepository.save(centro);

        tipoDocumento = new TipoDocumento();
        tipoDocumento.setId("CC");
        tipoDocumento.setTipo("Cédula de Ciudadanía");
        tipoDocumentoRepository.save(tipoDocumento);

        Medico medico = new Medico();
        medico.setPkId("M001");
        medico.setNombre("Laura");
        medico.setApellido("Pérez");
        medico.setCentroMedico(centro);
        medico.setTipoDocumento(tipoDocumento);
        medico.setIdDocumento("1234567890");
        medico.setFechaNacimiento(new Date());
        medico.setProfesion("Ginecóloga");
        medico.setEspecialidad("Obstetricia");
        medico.setTelefono("3101234567");
        medico.setDireccion("Carrera 1");
        medico.setGenero("Femenino");
        medico.setTarjetaProfesional("TP-12345");
        medico.setUrlImagen("http://foto.com/laura.jpg");
        medico.setCorreo("laura@medica.com");

        medicoRepository.save(medico);
    }

    @Test
    void testFindByCentroMedicoPkId() {
        List<Medico> resultado = medicoRepository.findByCentroMedicoPkId(centro.getPkId());
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Laura");
    }

    @Test
    void testFindByCorreo() {
        Optional<Medico> resultado = medicoRepository.findByCorreo("laura@medica.com");
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getApellido()).isEqualTo("Pérez");
    }

    @Test
    void testFindByNombreContainingIgnoreCase() {
        List<Medico> resultado = medicoRepository.findByNombreContainingIgnoreCase("lau");
        assertThat(resultado).hasSize(1);
    }

    @Test
    void testFindByTarjetaProfesionalContainingIgnoreCase() {
        List<Medico> resultado = medicoRepository.findByTarjetaProfesionalContainingIgnoreCase("tp-");
        assertThat(resultado).hasSize(1);
    }

    @Test
    void testFindByProfesionContainingIgnoreCase() {
        List<Medico> resultado = medicoRepository.findByProfesionContainingIgnoreCase("gine");
        assertThat(resultado).hasSize(1);
    }
}
