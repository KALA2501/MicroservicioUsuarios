package com.usuarios.demo.services;

import com.usuarios.demo.entities.*;
import com.usuarios.demo.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PacienteServiceTest {

    @InjectMocks
    private PacienteService service;

    @Mock
    private PacienteRepository pacienteRepository;

    @Mock
    private CentroMedicoRepository centroMedicoRepository;

    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Mock
    private VinculacionRepository vinculacionRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testObtenerTodos() {
        List<Paciente> pacientes = List.of(new Paciente(), new Paciente());
        when(pacienteRepository.findAll()).thenReturn(pacientes);

        List<Paciente> resultado = service.obtenerTodos();

        assertEquals(2, resultado.size());
    }

    @Test
    void testObtenerPorId_Existe() {
        Paciente paciente = new Paciente();
        paciente.setPkId("123");
        when(pacienteRepository.findById("123")).thenReturn(Optional.of(paciente));

        Optional<Paciente> resultado = service.obtenerPorId("123");

        assertTrue(resultado.isPresent());
        assertEquals("123", resultado.get().getPkId());
    }

    @Test
    void testGuardar() {
        Paciente paciente = new Paciente();
        paciente.setNombre("Ana");
        when(pacienteRepository.save(paciente)).thenReturn(paciente);

        Paciente resultado = service.guardar(paciente);

        assertEquals("Ana", resultado.getNombre());
    }

    @Test
    void testBuscarPorNombre() {
        Paciente paciente = new Paciente();
        paciente.setNombre("Juanito");
        when(pacienteRepository.findByNombreContainingIgnoreCase("Juan"))
                .thenReturn(List.of(paciente));

        List<Paciente> resultado = service.buscarPorNombre("Juan");

        assertEquals(1, resultado.size());
        assertEquals("Juanito", resultado.get(0).getNombre());
    }

    @Test
    void testBuscarPorTipoYNumero() {
        Paciente paciente = new Paciente();
        paciente.setIdDocumento("999");
        when(pacienteRepository.findByTipoDocumento_IdAndIdDocumento("CC", "999"))
                .thenReturn(Optional.of(paciente));

        Optional<Paciente> resultado = service.buscarPorTipoYNumero("CC", "999");

        assertTrue(resultado.isPresent());
        assertEquals("999", resultado.get().getIdDocumento());
    }

    @Test
    void testObtenerPorCentroMedico() {
        Paciente paciente = new Paciente();
        when(pacienteRepository.findByCentroMedico_PkId(1L)).thenReturn(List.of(paciente));

        List<Paciente> resultado = service.obtenerPorCentroMedico(1L);

        assertEquals(1, resultado.size());
    }

        @Test
    void testGuardarConValidacion_OK() {
        Paciente paciente = new Paciente();
        paciente.setTelefono("3000000000");
        paciente.setIdDocumento("123");
        paciente.setTipoDocumento(new TipoDocumento("CC", "Cédula"));

        when(pacienteRepository.findByTipoDocumento_IdAndIdDocumento("CC", "123")).thenReturn(Optional.empty());
        when(pacienteRepository.existsByTelefono("3000000000")).thenReturn(false);
        when(pacienteRepository.save(paciente)).thenReturn(paciente);

        Paciente resultado = service.guardarConValidacion(paciente);
        assertEquals("123", resultado.getIdDocumento());
    }

    @Test
    void testEliminarPacienteConVinculaciones() {
        Paciente paciente = new Paciente();
        paciente.setPkId("abc123");

        Medico medico = new Medico();
        medico.setNombre("Dr. House");

        Vinculacion vinculacion = new Vinculacion();
        vinculacion.setPaciente(paciente);
        vinculacion.setMedico(medico);

        when(pacienteRepository.findById("abc123")).thenReturn(Optional.of(paciente));
        when(vinculacionRepository.findByPaciente_PkId("abc123")).thenReturn(List.of(vinculacion));

        service.eliminar("abc123");

        verify(vinculacionRepository, times(1)).delete(vinculacion);
        verify(pacienteRepository, times(1)).delete(paciente);
    }

    @Test
    void testObtenerPacientesDelMedico() {
        String token = "mock.jwt.token";
        String correo = "medico@kala.com";

        Medico medico = new Medico();
        medico.setPkId("med123");
        medico.setCorreo(correo);

        Paciente paciente = new Paciente();
        paciente.setPkId("pac999");

        Vinculacion vinculacion = new Vinculacion();
        vinculacion.setMedico(medico);
        vinculacion.setPaciente(paciente);

        when(jwtService.extractUsername(token)).thenReturn(correo);
        when(medicoRepository.findByCorreo(correo)).thenReturn(Optional.of(medico));
        when(vinculacionRepository.findByMedico_PkId("med123")).thenReturn(List.of(vinculacion));

        List<Paciente> resultado = service.obtenerPacientesDelMedico(token);
        assertEquals(1, resultado.size());
        assertEquals("pac999", resultado.get(0).getPkId());
    }

    @Test
    void testEliminarPorCorreo() {
        Paciente paciente = new Paciente();
        paciente.setPkId("pac001");
        paciente.setEmail("paciente@kala.com");

        Vinculacion vinculacion = new Vinculacion();
        vinculacion.setPaciente(paciente);

        when(pacienteRepository.findByEmail("paciente@kala.com")).thenReturn(Optional.of(paciente));
        when(vinculacionRepository.findByPaciente_PkId("pac001")).thenReturn(List.of(vinculacion));

        service.eliminarPorCorreo("paciente@kala.com");

        verify(vinculacionRepository).delete(vinculacion);
        verify(pacienteRepository).delete(paciente);
    }
} 
