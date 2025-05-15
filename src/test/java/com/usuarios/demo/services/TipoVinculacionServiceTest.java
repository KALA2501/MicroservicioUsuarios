package com.usuarios.demo.services;

import com.usuarios.demo.entities.TipoVinculacion;
import com.usuarios.demo.repositories.TipoVinculacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TipoVinculacionServiceTest {

    @InjectMocks
    private TipoVinculacionService tipoVinculacionService;

    @Mock
    private TipoVinculacionRepository tipoVinculacionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testObtenerTodos() {
        List<TipoVinculacion> lista = Arrays.asList(
                new TipoVinculacion("01","FAMILIAR","Familiar cercano"),
                new TipoVinculacion("02","RESPONSABLE", "Responsable legal")
        );
        when(tipoVinculacionRepository.findAll()).thenReturn(lista);

        List<TipoVinculacion> resultado = tipoVinculacionService.obtenerTodos();

        assertEquals(2, resultado.size());
        verify(tipoVinculacionRepository).findAll();
    }

    @Test
    void testObtenerPorId() {
        TipoVinculacion tipo = new TipoVinculacion("02", "RESPONSABLE", "Responsable legal");
        when(tipoVinculacionRepository.findById("RESPONSABLE")).thenReturn(Optional.of(tipo));

        Optional<TipoVinculacion> resultado = tipoVinculacionService.obtenerPorId("RESPONSABLE");

        assertTrue(resultado.isPresent());
        assertEquals("Responsable legal", resultado.get().getDescripcion());
    }

    @Test
    void testGuardar() {
        TipoVinculacion tipo = new TipoVinculacion("01", "FAMILIAR", "Familiar cercano");
        when(tipoVinculacionRepository.save(tipo)).thenReturn(tipo);

        TipoVinculacion resultado = tipoVinculacionService.guardar(tipo);

        assertEquals("01", resultado.getId());
        assertEquals("Familiar cercano", resultado.getDescripcion());
        verify(tipoVinculacionRepository).save(tipo);
    }


    @Test
    void testEliminar() {
        tipoVinculacionService.eliminar("ACOMPAÑANTE");
        verify(tipoVinculacionRepository).deleteById("ACOMPAÑANTE");
    }
}
