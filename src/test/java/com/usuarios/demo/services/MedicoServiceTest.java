package com.usuarios.demo.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.usuarios.demo.entities.*;
import com.usuarios.demo.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MedicoServiceTest {

    @InjectMocks
    private MedicoService service;

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private CentroMedicoRepository centroMedicoRepository;

    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;

    @Mock
    private FirebaseAuth firebaseAuth;

    @Mock
    private UserRecord userRecord;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new MedicoService(medicoRepository, centroMedicoRepository, tipoDocumentoRepository, firebaseAuth);
    }

    @Test
    void testObtenerTodos() {
        List<Medico> medicos = List.of(new Medico(), new Medico());
        when(medicoRepository.findAll()).thenReturn(medicos);

        List<Medico> resultado = service.obtenerTodos();

        assertEquals(2, resultado.size());
    }

    @Test
    void testGuardarNuevoMedicoConCentroYTipoDoc() {
        Medico medico = new Medico();
        medico.setCentroMedico(new CentroMedico(1L, "Centro de Salud", "123456789", "direccion", "url", "centro@xxx"));
        medico.setTipoDocumento(new TipoDocumento("CC", "Cédula"));

        CentroMedico centroMock = new CentroMedico(1L, "Centro de Salud", "123456789", "direccion", "url", "centro@xxx");
        TipoDocumento tipoMock = new TipoDocumento("CC", "Cédula");

        when(centroMedicoRepository.findById(1L)).thenReturn(Optional.of(centroMock));
        when(tipoDocumentoRepository.findById("CC")).thenReturn(Optional.of(tipoMock));
        when(medicoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Medico resultado = service.guardar(medico);

        assertNotNull(resultado.getPkId());
        assertEquals("Centro de Salud", resultado.getCentroMedico().getNombre());
    }

    @Test
    void testEliminarMedicoExistente() throws Exception {
        Medico medico = new Medico();
        medico.setPkId("123");
        medico.setCorreo("correo@kala.com");

        when(medicoRepository.findById("123")).thenReturn(Optional.of(medico));
        when(firebaseAuth.getUserByEmail("correo@kala.com")).thenReturn(userRecord);
        when(userRecord.getUid()).thenReturn("firebase-uid");

        service.eliminar("123");

        verify(firebaseAuth, times(1)).deleteUser("firebase-uid");
        verify(medicoRepository, times(1)).deleteById("123");
    }

    @Test
    void testFiltrarPorNombre() {
        when(medicoRepository.findByNombreContainingIgnoreCase("ana"))
                .thenReturn(List.of(new Medico()));

        List<Medico> resultado = service.filtrarMedicos("ana", null, null);

        assertEquals(1, resultado.size());
    }

    @Test
    void testObtenerOCrearPorCorreo_Nuevo() {
        when(medicoRepository.findByCorreo("nuevo@kala.com")).thenReturn(Optional.empty());
        when(medicoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Medico medico = service.obtenerOCrearPorCorreo("nuevo@kala.com");

        assertEquals("nuevo@kala.com", medico.getCorreo());
        assertNotNull(medico.getPkId());
    }

    @Test
    void testEliminarPorCorreo_NoExiste() throws Exception {
        when(medicoRepository.findByCorreo("no@existe.com")).thenReturn(Optional.empty());

        service.eliminarPorCorreo("no@existe.com");

        verify(firebaseAuth, never()).deleteUser(any());
        verify(medicoRepository, never()).delete(any());
    }
}
