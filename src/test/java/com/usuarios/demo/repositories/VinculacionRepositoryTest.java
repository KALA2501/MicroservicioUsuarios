package com.usuarios.demo.repositories;

import com.usuarios.demo.entities.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import jakarta.transaction.Transactional;
import org.springframework.test.annotation.Rollback;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class VinculacionRepositoryTest {

    @Autowired
    private VinculacionRepository vinculacionRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private CentroMedicoRepository centroMedicoRepository;

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Autowired
    private ContactoEmergenciaRepository contactoEmergenciaRepository;

    @Autowired
    private TipoVinculacionRepository tipoVinculacionRepository;

    private CentroMedico crearCentro() {
        CentroMedico centro = new CentroMedico();
        centro.setNombre("Centro Vital");
        centro.setCorreo("centro" + System.nanoTime() + "@correo.com");
        centro.setTelefono("300" + System.nanoTime());
        centro.setDireccion("Calle 123");
        centro.setUrlLogo("http://logo.png");
        return centroMedicoRepository.save(centro);
    }

    private TipoDocumento crearTipoDocumento() {
        TipoDocumento tipo = new TipoDocumento();
        tipo.setId("CC");
        tipo.setTipo("Cédula");
        return tipoDocumentoRepository.save(tipo);
    }

    private ContactoEmergencia crearContacto() {
        ContactoEmergencia contacto = new ContactoEmergencia();
        contacto.setNombre("Ana");
        contacto.setApellido("Pérez");
        contacto.setEmail("contacto" + System.nanoTime() + "@mail.com");
        contacto.setTelefono("301" + System.nanoTime());
        contacto.setRelacion("Hermana");
        contacto.setDireccion("Calle 1");
        return contactoEmergenciaRepository.save(contacto);
    }

    private Paciente crearPaciente(CentroMedico centro, TipoDocumento tipoDoc, ContactoEmergencia contacto) {
        Paciente paciente = new Paciente();
        paciente.setPkId("P" + System.nanoTime());
        paciente.setCentroMedico(centro);
        paciente.setTipoDocumento(tipoDoc);
        paciente.setContactoEmergencia(contacto);
        paciente.setNombre("Camila");
        paciente.setApellido("Gómez");
        paciente.setIdDocumento("1234567890" + System.nanoTime());
        paciente.setFechaNacimiento(new Timestamp(System.currentTimeMillis()));
        paciente.setCodigoCIE(111);
        paciente.setTelefono("311" + System.nanoTime());
        paciente.setEmail("paciente" + System.nanoTime() + "@mail.com");
        paciente.setDireccion("Av. Siempre Viva");
        paciente.setEtapa(3);
        paciente.setGenero("F");
        paciente.setUrlImagen("http://img.com/paciente.jpg");
        return pacienteRepository.save(paciente);
    }

    private Medico crearMedico(CentroMedico centro, TipoDocumento tipoDoc) {
        Medico medico = new Medico();
        medico.setPkId("M" + System.nanoTime());
        medico.setCentroMedico(centro);
        medico.setNombre("Laura");
        medico.setApellido("Díaz");
        medico.setTipoDocumento(tipoDoc);
        medico.setIdDocumento("654321"+ System.nanoTime());
        medico.setFechaNacimiento(new java.util.Date());
        medico.setProfesion("General");
        medico.setEspecialidad("Ninguna");
        medico.setTelefono("312" + System.nanoTime());
        medico.setDireccion("Carrera 7");
        medico.setGenero("Femenino");
        medico.setTarjetaProfesional("TP-" + System.nanoTime());
        medico.setUrlImagen("http://img.com/medico.jpg");
        medico.setCorreo("medico" + System.nanoTime() + "@mail.com");
        return medicoRepository.save(medico);
    }

    private TipoVinculacion crearTipoVinculacion() {
        TipoVinculacion tipo = new TipoVinculacion();
        tipo.setId("TV" + System.nanoTime());
        tipo.setTipo("ATENCION");
        tipo.setDescripcion("Vinculación por atención directa");
        return tipoVinculacionRepository.save(tipo);
    }

    @Test
    @DisplayName("Debe guardar y encontrar una vinculación entre paciente y médico")
    void testGuardarYBuscarVinculacion() {
        CentroMedico centro = crearCentro();
        TipoDocumento tipoDoc = crearTipoDocumento();
        ContactoEmergencia contacto = crearContacto();
        Paciente paciente = crearPaciente(centro, tipoDoc, contacto);
        Medico medico = crearMedico(centro, tipoDoc);
        TipoVinculacion tipoVinculacion = crearTipoVinculacion();

        Vinculacion vinculacion = new Vinculacion();
        vinculacion.setPaciente(paciente);
        vinculacion.setMedico(medico);
        vinculacion.setFechaVinculado(new Timestamp(System.currentTimeMillis()));
        vinculacion.setTipoVinculacion(tipoVinculacion);

        vinculacionRepository.save(vinculacion);

        Optional<Vinculacion> encontrada = vinculacionRepository.findByPaciente_PkIdAndMedico_PkId(paciente.getPkId(), medico.getPkId());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getTipoVinculacion().getTipo()).isEqualTo("ATENCION");
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Debe eliminar todas las vinculaciones de un paciente")
    void testDeleteAllByPacientePkId() {
        CentroMedico centro = crearCentro();
        TipoDocumento tipoDoc = crearTipoDocumento();
        ContactoEmergencia contacto = crearContacto();
        Paciente paciente = crearPaciente(centro, tipoDoc, contacto);
        Medico medico1 = crearMedico(centro, tipoDoc);
        Medico medico2 = crearMedico(centro, tipoDoc);
        TipoVinculacion tipo = crearTipoVinculacion();

        // Vinculaciones con dos médicos distintos
        vinculacionRepository.save(new Vinculacion(paciente, medico1, new Timestamp(System.currentTimeMillis()), tipo));
        vinculacionRepository.save(new Vinculacion(paciente, medico2, new Timestamp(System.currentTimeMillis()), tipo));

        assertThat(vinculacionRepository.findByPaciente_PkId(paciente.getPkId())).hasSize(2);

        vinculacionRepository.deleteAllByPaciente_PkId(paciente.getPkId());

        assertThat(vinculacionRepository.findByPaciente_PkId(paciente.getPkId())).isEmpty();
    }

    @Test
    @Transactional
    @Rollback
    @DisplayName("Debe eliminar todas las vinculaciones de un médico")
    void testDeleteAllByMedicoPkId() {
        CentroMedico centro = crearCentro();
        TipoDocumento tipoDoc = crearTipoDocumento();
        TipoVinculacion tipo = crearTipoVinculacion();
        Medico medico = crearMedico(centro, tipoDoc);

        // Creamos 2 pacientes distintos vinculados al mismo médico
        for (int i = 0; i < 2; i++) {
            ContactoEmergencia contacto = crearContacto();
            Paciente paciente = crearPaciente(centro, tipoDoc, contacto);
            vinculacionRepository.save(new Vinculacion(paciente, medico, new Timestamp(System.currentTimeMillis()), tipo));
        }

        assertThat(vinculacionRepository.findByMedico_PkId(medico.getPkId())).hasSize(2);

        vinculacionRepository.deleteAllByMedico_PkId(medico.getPkId());

        assertThat(vinculacionRepository.findByMedico_PkId(medico.getPkId())).isEmpty();
    }

    @Test
    @DisplayName("Debe encontrar vinculaciones por médico y tipo de vinculación")
    void testFindByMedicoAndTipoVinculacion() {
        CentroMedico centro = crearCentro();
        TipoDocumento tipoDoc = crearTipoDocumento();
        ContactoEmergencia contacto = crearContacto();
        Paciente paciente = crearPaciente(centro, tipoDoc, contacto);
        Medico medico = crearMedico(centro, tipoDoc);
        TipoVinculacion tipo = crearTipoVinculacion();

        vinculacionRepository.save(new Vinculacion(paciente, medico, new Timestamp(System.currentTimeMillis()), tipo));

        List<Vinculacion> resultado = vinculacionRepository.findByMedicoAndTipoVinculacion(medico, tipo);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getPaciente().getPkId()).isEqualTo(paciente.getPkId());
    }

    @Test
    @DisplayName("Debe encontrar vinculaciones por centro médico del paciente")
    void testFindByPacienteCentroMedicoPkId() {
        CentroMedico centro = crearCentro();
        TipoDocumento tipoDoc = crearTipoDocumento();
        ContactoEmergencia contacto = crearContacto();
        Paciente paciente = crearPaciente(centro, tipoDoc, contacto);
        Medico medico = crearMedico(centro, tipoDoc);
        TipoVinculacion tipo = crearTipoVinculacion();

        vinculacionRepository.save(new Vinculacion(paciente, medico, new Timestamp(System.currentTimeMillis()), tipo));

        List<Vinculacion> resultado = vinculacionRepository.findByPaciente_CentroMedico_PkId(centro.getPkId());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getPaciente().getCentroMedico().getPkId()).isEqualTo(centro.getPkId());
    }

    @Test
    @DisplayName("Debe encontrar todas las vinculaciones de un médico")
    void testFindByMedico() {
        CentroMedico centro = crearCentro();
        TipoDocumento tipoDoc = crearTipoDocumento();
        Medico medico = crearMedico(centro, tipoDoc);
        TipoVinculacion tipo = crearTipoVinculacion();

        for (int i = 0; i < 2; i++) {
            ContactoEmergencia contacto = crearContacto();
            Paciente paciente = crearPaciente(centro, tipoDoc, contacto);
            vinculacionRepository.save(new Vinculacion(paciente, medico, new Timestamp(System.currentTimeMillis()), tipo));
        }

        List<Vinculacion> resultado = vinculacionRepository.findByMedico(medico);
        assertThat(resultado).hasSize(2);
    }

    @Test
    @DisplayName("Debe encontrar todas las vinculaciones de un paciente")
    void testFindByPaciente() {
        CentroMedico centro = crearCentro();
        TipoDocumento tipoDoc = crearTipoDocumento();
        ContactoEmergencia contacto = crearContacto();
        Paciente paciente = crearPaciente(centro, tipoDoc, contacto);
        TipoVinculacion tipo = crearTipoVinculacion();

        for (int i = 0; i < 2; i++) {
            Medico medico = crearMedico(centro, tipoDoc);
            vinculacionRepository.save(new Vinculacion(paciente, medico, new Timestamp(System.currentTimeMillis()), tipo));
        }

        List<Vinculacion> resultado = vinculacionRepository.findByPaciente(paciente);
        assertThat(resultado).hasSize(2);
    }

}
