package com.usuarios.demo.services;

import com.usuarios.demo.entities.*;
import com.usuarios.demo.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.sql.Timestamp;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VinculacionServiceTest {

    @InjectMocks
    private VinculacionService vinculacionService;

    @Mock
    private VinculacionRepository vinculacionRepository;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private TipoVinculacionRepository tipoVinculacionRepository;

    private final Paciente paciente = new Paciente();
    private final Medico medico = new Medico();
    private final TipoVinculacion tipo = new TipoVinculacion();
    private final Vinculacion vinculacion = new Vinculacion();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        paciente.setPkId("p1");
        paciente.setNombre("Ana");
        paciente.setApellido("Gómez");

        medico.setPkId("m1");
        medico.setNombre("Dr.");
        medico.setApellido("Salud");

        tipo.setId("t1");
        tipo.setTipo("Tratante");

        vinculacion.setPaciente(paciente);
        vinculacion.setMedico(medico);
        vinculacion.setTipoVinculacion(tipo);
        vinculacion.setFechaVinculado(new Timestamp(System.currentTimeMillis()));
    }

    @Test
    void testCrearVinculacion_exito() {
        when(pacienteRepository.findById("p1")).thenReturn(Optional.of(paciente));
        when(medicoRepository.findById("m1")).thenReturn(Optional.of(medico));
        when(tipoVinculacionRepository.findById("t1")).thenReturn(Optional.of(tipo));
        when(vinculacionRepository.findByPacienteAndMedicoAndTipoVinculacion(paciente, medico, tipo))
                .thenReturn(Optional.empty());
        when(vinculacionRepository.save(any(Vinculacion.class))).thenReturn(vinculacion);

        Vinculacion resultado = vinculacionService.crearVinculacion("p1", "m1", "t1");

        assertEquals("Ana", resultado.getPaciente().getNombre());
        verify(vinculacionRepository).save(any(Vinculacion.class));
    }

    @Test
    void testCrearVinculacion_duplicada() {
        when(pacienteRepository.findById("p1")).thenReturn(Optional.of(paciente));
        when(medicoRepository.findById("m1")).thenReturn(Optional.of(medico));
        when(tipoVinculacionRepository.findById("t1")).thenReturn(Optional.of(tipo));
        when(vinculacionRepository.findByPacienteAndMedicoAndTipoVinculacion(paciente, medico, tipo))
                .thenReturn(Optional.of(vinculacion));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                vinculacionService.crearVinculacion("p1", "m1", "t1"));

        assertTrue(ex.getMessage().contains("ya existe"));
    }

    @Test
    void testObtenerVinculacionesPorMedico() {
        when(vinculacionRepository.findByMedico_TarjetaProfesional("12345"))
                .thenReturn(List.of(vinculacion));

        List<Map<String, String>> resultado = vinculacionService.obtenerVinculacionesPorMedico("12345");

        assertEquals(1, resultado.size());
        assertEquals("Ana Gómez", resultado.get(0).get("nombrePaciente"));
    }

    @Test
    void testEliminarVinculacion_exito() {
        VinculacionId id = new VinculacionId("p1", "m1");
        when(vinculacionRepository.findById(id)).thenReturn(Optional.of(vinculacion));

        vinculacionService.eliminarVinculacion("p1", "m1");

        verify(vinculacionRepository).delete(vinculacion);
    }

    @Test
    void testActualizarVinculacion_exito() {
        VinculacionId id = new VinculacionId("p1", "m1");
        TipoVinculacion nuevoTipo = new TipoVinculacion("t2", "Apoyo"," Apoyo a la salud");

        when(vinculacionRepository.findById(id)).thenReturn(Optional.of(vinculacion));
        when(tipoVinculacionRepository.findById("t2")).thenReturn(Optional.of(nuevoTipo));
        when(vinculacionRepository.save(any())).thenReturn(vinculacion);

        Vinculacion resultado = vinculacionService.actualizarVinculacion(id, "t2");

        assertEquals("Apoyo", resultado.getTipoVinculacion().getTipo());
    }
}
