package com.usuarios.demo.services;

import com.google.firebase.auth.*;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.usuarios.demo.entities.*;
import com.usuarios.demo.exceptions.CentroMedicoException;
import com.usuarios.demo.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void testGuardarSolicitud_CorreoDuplicado() {
        SolicitudCentroMedico solicitud = new SolicitudCentroMedico();
        solicitud.setCorreo("duplicado@kala.com");
        solicitud.setTelefono("123456");

        when(solicitudRepository.existsByCorreo("duplicado@kala.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> solicitudService.guardarSolicitud(solicitud));
        assertEquals("Ya existe una solicitud con ese correo", ex.getMessage());
    }

    @Test
    void testGuardarSolicitud_TelefonoDuplicado() {
        SolicitudCentroMedico solicitud = new SolicitudCentroMedico();
        solicitud.setCorreo("unico@kala.com");
        solicitud.setTelefono("rep");

        when(solicitudRepository.existsByCorreo("unico@kala.com")).thenReturn(false);
        when(solicitudRepository.existsByTelefono("rep")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> solicitudService.guardarSolicitud(solicitud));
        assertEquals("Ya existe una solicitud con ese teléfono", ex.getMessage());
    }

    @Test
    void testProcesarYCrearUsuario_UsuarioYaExisteEnFirebase() throws FirebaseAuthException {
        SolicitudCentroMedico solicitud = new SolicitudCentroMedico();
        solicitud.setId(10L);
        solicitud.setCorreo("exists@kala.com");

        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        when(firebaseAuth.getUserByEmail("exists@kala.com")).thenReturn(userRecord);
        when(userRecord.getUid()).thenReturn("firebase-uid");

        solicitudService.procesarYCrearUsuario(10L, "CENTRO_MEDICO");

        verify(firebaseAuth, times(1)).setCustomUserClaims(eq("firebase-uid"), anyMap());
        verify(centroMedicoRepository, times(1)).save(any(CentroMedico.class));
    }

    @Test
    void testProcesarYCrearUsuario_EmailAlreadyExistsRecovery() throws FirebaseAuthException {
        SolicitudCentroMedico solicitud = new SolicitudCentroMedico();
        solicitud.setId(11L);
        solicitud.setCorreo("already@kala.com");

        when(solicitudRepository.findById(11L)).thenReturn(Optional.of(solicitud));

        FirebaseAuthException emailExists = mock(FirebaseAuthException.class);
        when(emailExists.getAuthErrorCode()).thenReturn(AuthErrorCode.EMAIL_ALREADY_EXISTS);

        FirebaseAuthException userNotFound = mock(FirebaseAuthException.class);
        when(userNotFound.getAuthErrorCode()).thenReturn(AuthErrorCode.USER_NOT_FOUND);

        // First call throws USER_NOT_FOUND, triggers createUser
        FirebaseAuthException userCreationFails = mock(FirebaseAuthException.class);
        when(userCreationFails.getAuthErrorCode()).thenReturn(AuthErrorCode.EMAIL_ALREADY_EXISTS);

        doThrow(userCreationFails).when(firebaseAuth).createUser(any(CreateRequest.class));
        when(firebaseAuth.getUserByEmail("already@kala.com")).thenReturn(userRecord);
        when(userRecord.getUid()).thenReturn("uid-exists");

        solicitudService.procesarYCrearUsuario(11L, "CENTRO_MEDICO");

        verify(firebaseAuth).setCustomUserClaims("uid-exists", Map.of("rol", "CENTRO_MEDICO"));
        verify(centroMedicoRepository).save(any(CentroMedico.class));
    }

    @Test
    void testProcesarYCrearUsuario_CreationFailsWithOtherError() throws FirebaseAuthException {
        SolicitudCentroMedico solicitud = new SolicitudCentroMedico();
        solicitud.setId(12L);
        solicitud.setCorreo("fail@kala.com");

        when(solicitudRepository.findById(12L)).thenReturn(Optional.of(solicitud));

        FirebaseAuthException userNotFound = mock(FirebaseAuthException.class);
        when(userNotFound.getAuthErrorCode()).thenReturn(AuthErrorCode.USER_NOT_FOUND);

        FirebaseAuthException otherError = mock(FirebaseAuthException.class);
        when(otherError.getAuthErrorCode()).thenReturn(AuthErrorCode.INVALID_ID_TOKEN);
        when(otherError.getMessage()).thenReturn("Something unexpected");


        doThrow(userNotFound).when(firebaseAuth).getUserByEmail("fail@kala.com");
        doThrow(otherError).when(firebaseAuth).createUser(any(CreateRequest.class));

        CentroMedicoException ex = assertThrows(CentroMedicoException.class, () ->
                solicitudService.procesarYCrearUsuario(12L, "CENTRO_MEDICO")
        );
        assertTrue(ex.getMessage().contains("No se pudo crear el usuario"));
    }

    @Test
    void testRevertirProcesado_ConFirebase() throws FirebaseAuthException {
        SolicitudCentroMedico solicitud = new SolicitudCentroMedico();
        solicitud.setId(3L);
        solicitud.setCorreo("exists@firebase.com");

        when(solicitudRepository.findById(3L)).thenReturn(Optional.of(solicitud));
        when(centroMedicoRepository.existsByCorreo("exists@firebase.com")).thenReturn(false);
        when(firebaseAuth.getUserByEmail("exists@firebase.com")).thenReturn(userRecord);
        when(userRecord.getUid()).thenReturn("to-delete-uid");

        solicitudService.revertirProcesado(3L);

        verify(firebaseAuth).deleteUser("to-delete-uid");
        verify(solicitudRepository).save(solicitud);
        assertFalse(solicitud.isProcesado());
    }

    @Test
    void testGuardarSolicitud_NullCorreo() {
        SolicitudCentroMedico solicitud = new SolicitudCentroMedico();
        solicitud.setCorreo(null);
        solicitud.setTelefono("1234567890");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> solicitudService.guardarSolicitud(solicitud));
        assertEquals("Correo y teléfono no pueden ser nulos", ex.getMessage());
    }

    @Test
    void testGuardarSolicitud_NullTelefono() {
        SolicitudCentroMedico solicitud = new SolicitudCentroMedico();
        solicitud.setCorreo("centro@kala.com");
        solicitud.setTelefono(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> solicitudService.guardarSolicitud(solicitud));
        assertEquals("Correo y teléfono no pueden ser nulos", ex.getMessage());
    }

    @Test
    void testMarcarComoProcesado_InvalidId() {
        when(solicitudRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> solicitudService.marcarComoProcesado(99L));
        assertEquals("Solicitud no encontrada", ex.getMessage());
    }

    @Test
    void testProcesarYCrearUsuario_CreationFailsInFirebase() throws FirebaseAuthException {
        SolicitudCentroMedico solicitud = new SolicitudCentroMedico();
        solicitud.setId(10L);
        solicitud.setCorreo("fail@create.com");

        when(solicitudRepository.findById(10L)).thenReturn(Optional.of(solicitud));
        FirebaseAuthException userNotFoundException = mock(FirebaseAuthException.class);
        when(userNotFoundException.getAuthErrorCode()).thenReturn(AuthErrorCode.USER_NOT_FOUND);
        when(firebaseAuth.getUserByEmail("fail@create.com")).thenThrow(userNotFoundException);

        FirebaseAuthException firebaseException = mock(FirebaseAuthException.class);
        when(firebaseException.getAuthErrorCode()).thenReturn(AuthErrorCode.EMAIL_ALREADY_EXISTS);
        doThrow(firebaseException).when(firebaseAuth).createUser(any(CreateRequest.class));

        CentroMedicoException ex = assertThrows(CentroMedicoException.class, () -> solicitudService.procesarYCrearUsuario(10L, "CENTRO_MEDICO"));
        assertTrue(ex.getMessage().contains("No se pudo crear el usuario"));
    }

    @Test
    void testRevertirProcesado_InvalidSolicitud() {
        when(solicitudRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> solicitudService.revertirProcesado(99L));
        assertEquals("Solicitud no encontrada", ex.getMessage());
    }

    @Test
    void testEliminarSolicitud_InvalidId() {
        when(solicitudRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> solicitudService.eliminarSolicitud(99L));
        assertEquals("Solicitud no encontrada", ex.getMessage());
    }

    @Test
    void testEliminarPorId() {
        when(solicitudRepository.existsById(1L)).thenReturn(true);

        solicitudService.eliminarPorId(1L);

        verify(solicitudRepository).deleteById(1L);
    }

    @Test
    void testObtenerSolicitudes() {
        List<SolicitudCentroMedico> solicitudes = List.of(new SolicitudCentroMedico(), new SolicitudCentroMedico());
        when(solicitudRepository.findAll()).thenReturn(solicitudes);

        List<SolicitudCentroMedico> result = solicitudService.obtenerSolicitudes();

        assertEquals(2, result.size());
        verify(solicitudRepository).findAll();
    }

    @Test
    void testEliminarPorId_InvalidId() {
        when(solicitudRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> solicitudService.eliminarPorId(99L));
        assertEquals("Solicitud no encontrada", ex.getMessage());
    }



} 
