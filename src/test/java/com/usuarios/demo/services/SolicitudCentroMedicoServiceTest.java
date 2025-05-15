package com.usuarios.demo.services;

import com.google.firebase.auth.*;
import com.usuarios.demo.entities.*;
import com.usuarios.demo.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SolicitudCentroMedicoServiceTest {

    @InjectMocks
    private SolicitudCentroMedicoService solicitudService;

    @Mock
    private SolicitudCentroMedicoRepository solicitudRepository;

    @Mock
    private CentroMedicoRepository centroMedicoRepository;

    @Mock
    private FirebaseAuth firebaseAuth;

    @Mock
    private UserRecord userRecord;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        solicitudService = new SolicitudCentroMedicoService(solicitudRepository, centroMedicoRepository, firebaseAuth);
    }

    @Test
    void testGuardarSolicitud_Nueva() {
        SolicitudCentroMedico solicitudLocal = new SolicitudCentroMedico();
        solicitudLocal.setCorreo("centro@kala.com");
        solicitudLocal.setTelefono("1234567890");

        when(solicitudRepository.existsByCorreo("centro@kala.com")).thenReturn(false);
        when(solicitudRepository.existsByTelefono("1234567890")).thenReturn(false);
        when(solicitudRepository.save(solicitudLocal)).thenReturn(solicitudLocal);

        SolicitudCentroMedico resultado = solicitudService.guardarSolicitud(solicitudLocal);

        assertEquals("centro@kala.com", resultado.getCorreo());
    }

    @Test
    void testMarcarComoProcesado() {
        SolicitudCentroMedico solicitud = new SolicitudCentroMedico();
        solicitud.setId(1L);
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));

        solicitudService.marcarComoProcesado(1L);

        assertTrue(solicitud.isProcesado());
        verify(solicitudRepository).save(solicitud);
    }

    @Test
    void testEliminarSolicitud_Existe() {
        when(solicitudRepository.existsById(1L)).thenReturn(true);

        solicitudService.eliminarSolicitud(1L);

        verify(solicitudRepository).deleteById(1L);
    }

    @Test
    void testEliminarSolicitud_NoExiste() {
        when(solicitudRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> solicitudService.eliminarSolicitud(99L));
        assertEquals("Solicitud no encontrada", ex.getMessage());
    }


    @Test
    void testRevertirProcesado_SoloBD() throws FirebaseAuthException {
        SolicitudCentroMedico solicitudLocal = new SolicitudCentroMedico();
        solicitudLocal.setId(2L);
        solicitudLocal.setCorreo("revertir@bd.com");

        when(solicitudRepository.findById(2L)).thenReturn(Optional.of(solicitudLocal));
        when(centroMedicoRepository.existsByCorreo("revertir@bd.com")).thenReturn(true);
        doNothing().when(centroMedicoRepository).deleteByCorreo("revertir@bd.com");

        // Simular que NO existe en Firebase
        FirebaseAuthException exception = mock(FirebaseAuthException.class);
        when(exception.getAuthErrorCode()).thenReturn(AuthErrorCode.USER_NOT_FOUND);
        doThrow(exception).when(firebaseAuth).getUserByEmail("revertir@bd.com");

        when(solicitudRepository.save(solicitudLocal)).thenReturn(solicitudLocal);

        solicitudService.revertirProcesado(2L);

        assertFalse(solicitudLocal.isProcesado());
        verify(solicitudRepository).save(solicitudLocal);
    }
} 
