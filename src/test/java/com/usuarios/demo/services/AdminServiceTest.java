package com.usuarios.demo.services;

import com.usuarios.demo.entities.Admin;
import com.usuarios.demo.entities.SolicitudCentroMedico;
import com.usuarios.demo.repositories.AdminRepository;
import com.usuarios.demo.repositories.CentroMedicoRepository;
import com.usuarios.demo.repositories.SolicitudCentroMedicoRepository;
import com.usuarios.demo.controllers.SolicitudCentroMedicoController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private SolicitudCentroMedicoService solicitudCentroMedicoService;

    @Mock
    private SolicitudCentroMedico solicitudCentroMedico;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private CentroMedicoRepository centroMedicoRepository;

    @Mock
    private SolicitudCentroMedicoRepository solicitudRepository;

    @Mock
    private SolicitudCentroMedicoController solicitudCentroMedicoController;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 🔥 Método auxiliar para crear Admin con pkId y nombreCompleto
    private Admin crearAdmin(String pkId, String nombreCompleto) {
        return new Admin(pkId, nombreCompleto);
    }

    @Test
    void testObtenerTodos() {
        List<Admin> admins = Arrays.asList(
                crearAdmin("ADM001", "Laura Gómez"),
                crearAdmin("ADM002", "Mateo Rodríguez")
        );
        when(adminRepository.findAll()).thenReturn(admins);

        List<Admin> resultado = adminService.obtenerTodos();

        assertEquals(2, resultado.size());
        assertEquals("Laura Gómez", resultado.get(0).getNombreCompleto());
        assertEquals("Mateo Rodríguez", resultado.get(1).getNombreCompleto());
        assertEquals("ADM001", resultado.get(0).getPkId());
        assertEquals("ADM002", resultado.get(1).getPkId());

        verify(adminRepository, times(1)).findAll();
    }

    @Test
    void testEliminarUsuarioDeBaseDeDatos_CentroMedicoExiste() {
        String correo = "admin@kala.com";
        when(centroMedicoRepository.existsByCorreo(correo)).thenReturn(true);

        adminService.eliminarUsuarioDeBaseDeDatos(correo);

        verify(centroMedicoRepository, times(1)).deleteByCorreo(correo);
    }

    @Test
    void testEliminarUsuarioDeBaseDeDatos_CentroMedicoNoExiste() {
        String correo = "admin@kala.com";
        when(centroMedicoRepository.existsByCorreo(correo)).thenReturn(false);

        adminService.eliminarUsuarioDeBaseDeDatos(correo);

        verify(centroMedicoRepository, never()).deleteByCorreo(correo);
    }

    @Test
    void testActualizarSolicitudAlEliminarUsuario_Existe() {
        String correo = "admin@kala.com";
        SolicitudCentroMedico solicitud = new SolicitudCentroMedico();
        solicitud.setProcesado(true);
        when(solicitudRepository.findByCorreo(correo)).thenReturn(Optional.of(solicitud));

        adminService.actualizarSolicitudAlEliminarUsuario(correo);

        assertFalse(solicitud.isProcesado());
        verify(solicitudRepository, times(1)).save(solicitud);
    }

    @Test
    void testActualizarSolicitudAlEliminarUsuario_NoExiste() {
        String correo = "admin@kala.com";
        when(solicitudRepository.findByCorreo(correo)).thenReturn(Optional.empty());

        adminService.actualizarSolicitudAlEliminarUsuario(correo);

        verify(solicitudRepository, never()).save(any());
    }
}
