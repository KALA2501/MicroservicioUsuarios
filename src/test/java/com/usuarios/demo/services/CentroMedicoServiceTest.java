package com.usuarios.demo.services;

import com.google.firebase.auth.*;
import com.usuarios.demo.entities.CentroMedico;
import com.usuarios.demo.exceptions.CentroMedicoException;
import com.usuarios.demo.repositories.CentroMedicoRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
}
