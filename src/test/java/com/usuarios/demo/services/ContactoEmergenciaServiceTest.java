package com.usuarios.demo.services;

import com.usuarios.demo.entities.ContactoEmergencia;
import com.usuarios.demo.repositories.ContactoEmergenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContactoEmergenciaServiceTest {

    @InjectMocks
    private ContactoEmergenciaService service;

    @Mock
    private ContactoEmergenciaRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private ContactoEmergencia crearContacto(Long id, String nombre, String telefono) {
        ContactoEmergencia c = new ContactoEmergencia();
        c.setPkId(id);
        c.setNombre(nombre);
        c.setTelefono(telefono);
        return c;
    }

    @Test
    void testObtenerTodos() {
        List<ContactoEmergencia> contactos = List.of(
                crearContacto(1L, "María Pérez", "3001234567"),
                crearContacto(2L, "Carlos Díaz", "3017654321")
        );
        when(repository.findAll()).thenReturn(contactos);

        List<ContactoEmergencia> resultado = service.obtenerTodos();

        assertEquals(2, resultado.size());
        assertEquals("María Pérez", resultado.get(0).getNombre());
    }

    @Test
    void testObtenerPorId() {
        ContactoEmergencia contacto = crearContacto(1L, "Ana Torres", "3119876543");
        when(repository.findById(1L)).thenReturn(Optional.of(contacto));

        Optional<ContactoEmergencia> resultado = service.obtenerPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Ana Torres", resultado.get().getNombre());
    }

    @Test
    void testGuardar() {
        ContactoEmergencia contacto = crearContacto(null, "Luis Soto", "3104443322");
        ContactoEmergencia guardado = crearContacto(10L, "Luis Soto", "3104443322");
        when(repository.save(contacto)).thenReturn(guardado);

        ContactoEmergencia resultado = service.guardar(contacto);

        assertNotNull(resultado.getPkId());
        assertEquals("Luis Soto", resultado.getNombre());
    }

    @Test
    void testEliminar() {
        Long id = 5L;

        service.eliminar(id);

        verify(repository, times(1)).deleteById(id);
    }

    @Test
    void testBuscarPorTelefono() {
        ContactoEmergencia contacto = crearContacto(7L, "Sandra Velásquez", "3120001111");
        when(repository.findByTelefono("3120001111")).thenReturn(Optional.of(contacto));

        Optional<ContactoEmergencia> resultado = service.buscarPorTelefono("3120001111");

        assertTrue(resultado.isPresent());
        assertEquals("Sandra Velásquez", resultado.get().getNombre());
    }
}
