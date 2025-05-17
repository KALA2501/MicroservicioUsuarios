package com.usuarios.demo.services;

import com.google.firebase.auth.*;
import com.usuarios.demo.entities.CentroMedico;
import com.usuarios.demo.exceptions.CentroMedicoException;
import com.usuarios.demo.repositories.CentroMedicoRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CentroMedicoServiceTest {

    private static MockedStatic<FirebaseAuth> firebaseAuthStatic;

    @Mock
    private FirebaseAuth firebaseAuth;

    @InjectMocks
    private CentroMedicoService centroMedicoService;

    @Mock
    private CentroMedicoRepository centroMedicoRepository;

    @Mock
    private UserRecord userRecord;

    @BeforeAll
    static void initStaticMock() {
        firebaseAuthStatic = mockStatic(FirebaseAuth.class);
    }

    @AfterAll
    static void closeStaticMock() {
        firebaseAuthStatic.close();
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        firebaseAuthStatic.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);
    }

    private CentroMedico crearCentro(String nombre, String correo, String telefono) {
        CentroMedico centro = new CentroMedico();
        centro.setNombre(nombre);
        centro.setCorreo(correo);
        centro.setTelefono(telefono);
        return centro;
    }

    @Test
    void testRegistrarCentroMedico_Exitoso() throws Exception {
        CentroMedico centro = crearCentro("Clínica Kala", "kala@salud.com", "123456789");

        when(centroMedicoRepository.existsByCorreo(centro.getCorreo())).thenReturn(false);
        when(centroMedicoRepository.save(any())).thenAnswer(invocation -> {
            CentroMedico saved = invocation.getArgument(0);
            saved.setPkId(1L); // simula ID autogenerado
            return saved;
        });

        when(firebaseAuth.createUser(any(UserRecord.CreateRequest.class))).thenReturn(userRecord);
        doNothing().when(firebaseAuth).setCustomUserClaims(any(), any());

        CentroMedico resultado = centroMedicoService.registrarCentroMedico(centro);

        assertNotNull(resultado);
        assertEquals("Clínica Kala", resultado.getNombre());
        verify(centroMedicoRepository, times(1)).save(any());
        verify(firebaseAuth, times(1)).createUser(any());
        verify(firebaseAuth, times(1)).setCustomUserClaims(any(), any());
    }

    @Test
    void testRegistrarCentroMedico_FirebaseFalla() throws Exception {
        CentroMedico centro = crearCentro("Clínica Falla", "fail@salud.com", "987654321");

        when(centroMedicoRepository.existsByCorreo(centro.getCorreo())).thenReturn(false);
        when(centroMedicoRepository.save(any())).thenAnswer(invocation -> {
            CentroMedico saved = invocation.getArgument(0);
            saved.setPkId(99L);
            return saved;
        });

        // ✅ Simulamos un fallo en Firebase sin necesidad de FirebaseAuthException
        when(firebaseAuth.createUser(any()))
            .thenThrow(new RuntimeException("Simulated Firebase failure"));

        CentroMedicoException ex = assertThrows(
            CentroMedicoException.class,
            () -> centroMedicoService.registrarCentroMedico(centro)
        );

        assertTrue(ex.getMessage().contains("Error al registrar centro médico"));
        verify(centroMedicoRepository, times(1)).deleteById(99L);
    }



    @Test
    void testRegistrarCentroMedico_CorreoYaExiste() {
        CentroMedico centro = crearCentro("Clínica Existente", "existe@salud.com", "999");

        when(centroMedicoRepository.existsByCorreo(centro.getCorreo())).thenReturn(true);

        CentroMedicoException ex = assertThrows(
                CentroMedicoException.class,
                () -> centroMedicoService.registrarCentroMedico(centro)
        );

        assertEquals("Centro ya existe con ese correo", ex.getMessage());
    }

    @Test
    void testRegistrarCentroMedico_FaltanDatos() {
        CentroMedico centro = crearCentro(null, null, null);

        CentroMedicoException ex = assertThrows(
                CentroMedicoException.class,
                () -> centroMedicoService.registrarCentroMedico(centro)
        );

        assertEquals("Faltan datos obligatorios", ex.getMessage());
    }

    @Test
    void testRegistrarCentroMedico_ValidData() throws Exception {
        CentroMedico centro = crearCentro("Centro Saludable", "salud@salud.com", "987654321");

        when(centroMedicoRepository.existsByCorreo(centro.getCorreo())).thenReturn(false);
        when(centroMedicoRepository.save(any())).thenAnswer(invocation -> {
            CentroMedico saved = invocation.getArgument(0);
            saved.setPkId(101L); // Simulate ID generation
            return saved;
        });

        when(firebaseAuth.createUser(any(UserRecord.CreateRequest.class))).thenReturn(userRecord);
        doNothing().when(firebaseAuth).setCustomUserClaims(any(), any());

        CentroMedico result = centroMedicoService.registrarCentroMedico(centro);

        assertNotNull(result);
        assertEquals("Centro Saludable", result.getNombre());
        verify(centroMedicoRepository, times(1)).save(any());
        verify(firebaseAuth, times(1)).createUser(any());
        verify(firebaseAuth, times(1)).setCustomUserClaims(any(), any());
    }

    @Test
    void testActualizarCentroMedico_Exitoso() throws Exception {
        CentroMedico centroExistente = crearCentro("Clínica Existen", "existen@salud.com", "456789123");
        centroExistente.setPkId(1L);

        CentroMedico nuevosDatos = crearCentro("Clínica Actualizada", "existen@salud.com", "123456789");

        when(centroMedicoRepository.findById(centroExistente.getPkId())).thenReturn(Optional.of(centroExistente));
        when(centroMedicoRepository.save(any())).thenReturn(centroExistente);

        CentroMedico resultado = centroMedicoService.actualizar(centroExistente.getPkId(), nuevosDatos);

        assertNotNull(resultado);
        assertEquals("Clínica Actualizada", resultado.getNombre());
        verify(centroMedicoRepository, times(1)).save(any());
    }

    @Test
    void testActualizarCentroMedico_NoEncontrado() {
        CentroMedico nuevosDatos = crearCentro("Clínica Nueva", "nuevo@salud.com", "123456789");

        when(centroMedicoRepository.findById(anyLong())).thenReturn(Optional.empty());

        CentroMedicoException ex = assertThrows(CentroMedicoException.class,
            () -> centroMedicoService.actualizar(1L, nuevosDatos)
        );

        assertEquals("Centro médico no encontrado con ID: 1", ex.getMessage());
        verify(centroMedicoRepository, times(1)).findById(anyLong());
    }

    @Test
    void testRegistrarCentroMedico_FirebaseFailsAfterCreation() throws Exception {
        CentroMedico centro = crearCentro("Clínica Fallida", "fallido@salud.com", "321654987");

        when(centroMedicoRepository.existsByCorreo(centro.getCorreo())).thenReturn(false);
        when(centroMedicoRepository.save(any())).thenAnswer(invocation -> {
            CentroMedico saved = invocation.getArgument(0);
            saved.setPkId(102L);
            return saved;
        });

        // Simulate a Firebase failure after the user is created
        when(firebaseAuth.createUser(any())).thenReturn(userRecord);
        doThrow(new RuntimeException("Simulated Firebase failure during claim assignment"))
            .when(firebaseAuth).setCustomUserClaims(any(), any());

        CentroMedicoException ex = assertThrows(
            CentroMedicoException.class,
            () -> centroMedicoService.registrarCentroMedico(centro)
        );

        assertTrue(ex.getMessage().contains("Error al registrar centro médico"));
        verify(centroMedicoRepository, times(1)).deleteById(102L);
    }

    @Test
    void testEliminarCentroMedico_Exitoso() throws Exception {
        CentroMedico centro = crearCentro("Centro a Eliminar", "eliminar@salud.com", "654321789");
        centro.setPkId(1L);

        when(centroMedicoRepository.findByCorreo(centro.getCorreo())).thenReturn(Optional.of(centro));

        centroMedicoService.eliminarPorCorreo(centro.getCorreo());

        verify(centroMedicoRepository, times(1)).delete(centro);
    }

    @Test
    void testEliminarCentroMedico_NoEncontrado() {
        when(centroMedicoRepository.findByCorreo(anyString())).thenReturn(Optional.empty());

        centroMedicoService.eliminarPorCorreo("noexistente@salud.com");

        verify(centroMedicoRepository, times(0)).delete(any());
    }

    @Test
    void testRegistrarCentroMedico_FaltanCampos() {
        CentroMedico centro = new CentroMedico();
        centro.setCorreo(null); // nombre o teléfono también puede ser null

        CentroMedicoException ex = assertThrows(CentroMedicoException.class, () -> {
            centroMedicoService.registrarCentroMedico(centro);
        });

        assertThat(ex.getMessage()).contains("Faltan datos obligatorios");
    }


    @Test
    void testRegistrarCentroMedico_CorreoExistente() {
        CentroMedico existente = new CentroMedico(1L, "Centro Existente", "correo@falso", "3001234567", "Calle Falsa", "http://logo.com");

        when(centroMedicoRepository.existsByCorreo(anyString())).thenReturn(true);


        CentroMedicoException ex = assertThrows(CentroMedicoException.class, () -> {
            centroMedicoService.registrarCentroMedico(existente);
        });

        assertThat(ex.getMessage()).contains("Centro ya existe con ese correo");
    }


    @Test
    void testEliminarPorCorreo_NoExiste() {
        when(centroMedicoRepository.findByCorreo("noexiste@mail.com")).thenReturn(Optional.empty());

        centroMedicoService.eliminarPorCorreo("noexiste@mail.com");

        // No lanza excepción, solo logger.warn, por eso no hace falta assertThrows
        verify(centroMedicoRepository, never()).delete(any());
    }

    @Test
    void testEliminarPorCorreo_ErrorEnDelete() {
        CentroMedico centro = new CentroMedico();
        centro.setCorreo("error@mail.com");

        when(centroMedicoRepository.findByCorreo("error@mail.com")).thenReturn(Optional.of(centro));
        doThrow(new RuntimeException("Error en delete")).when(centroMedicoRepository).delete(any());

        CentroMedicoException ex = assertThrows(CentroMedicoException.class, () -> {
            centroMedicoService.eliminarPorCorreo("error@mail.com");
        });

        assertThat(ex.getMessage()).contains("Error al eliminar centro médico con correo");
    }

    
}
