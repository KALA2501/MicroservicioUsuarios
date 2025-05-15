package com.usuarios.demo.services;

import com.usuarios.demo.entities.TipoDocumento;
import com.usuarios.demo.repositories.TipoDocumentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TipoDocumentoServiceTest {

    @InjectMocks
    private TipoDocumentoService tipoDocumentoService;

    @Mock
    private TipoDocumentoRepository tipoDocumentoRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testObtenerTodos() {
        List<TipoDocumento> mockList = Arrays.asList(new TipoDocumento("CC", "Cédula"), new TipoDocumento("TI", "Tarjeta"));
        when(tipoDocumentoRepository.findAll()).thenReturn(mockList);

        List<TipoDocumento> resultado = tipoDocumentoService.obtenerTodos();

        assertEquals(2, resultado.size());
        verify(tipoDocumentoRepository).findAll();
    }

    @Test
    void testObtenerPorId() {
        TipoDocumento doc = new TipoDocumento("CC", "Cédula de ciudadanía");
        when(tipoDocumentoRepository.findById("CC")).thenReturn(Optional.of(doc));

        Optional<TipoDocumento> resultado = tipoDocumentoService.obtenerPorId("CC");

        assertTrue(resultado.isPresent());
        assertEquals("Cédula de ciudadanía", resultado.get().getTipo());
    }

    @Test
    void testGuardar() {
        TipoDocumento doc = new TipoDocumento("CE", "Cédula de extranjería");
        when(tipoDocumentoRepository.save(doc)).thenReturn(doc);

        TipoDocumento resultado = tipoDocumentoService.guardar(doc);

        assertEquals("CE", resultado.getId());
        verify(tipoDocumentoRepository).save(doc);
    }

    @Test
    void testEliminar() {
        tipoDocumentoService.eliminar("TI");
        verify(tipoDocumentoRepository).deleteById("TI");
    }
}
