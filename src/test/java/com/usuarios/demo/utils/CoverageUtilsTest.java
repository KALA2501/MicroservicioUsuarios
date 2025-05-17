package com.usuarios.demo.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CoverageUtilsTest {

    @Test
    public void testEdadInvalida() {
        assertEquals("Edad inválida", CoverageUtils.evaluarEdad(-1));
    }

    @Test
    public void testMenorEdad() {
        assertEquals("Menor de edad", CoverageUtils.evaluarEdad(12));
    }

    @Test
    public void testAdulto() {
        assertEquals("Adulto", CoverageUtils.evaluarEdad(35));
    }

    @Test
    public void testAdultoMayor() {
        assertEquals("Adulto mayor", CoverageUtils.evaluarEdad(75));
    }

     @Test
    void testEvaluarEdadTodosLosCasos() {
        assertEquals("Edad inválida", CoverageUtils.evaluarEdad(-5));
        assertEquals("Menor de edad", CoverageUtils.evaluarEdad(10));
        assertEquals("Adulto", CoverageUtils.evaluarEdad(30));
        assertEquals("Adulto mayor", CoverageUtils.evaluarEdad(70));
    }

        @Test
    void testClasificarIMC() {
        assertEquals("Bajo peso", CoverageUtils.clasificarIMC(17.0));
        assertEquals("Normal", CoverageUtils.clasificarIMC(22.0));
        assertEquals("Sobrepeso", CoverageUtils.clasificarIMC(27.0));
        assertEquals("Obesidad", CoverageUtils.clasificarIMC(32.0));
    }

    @Test
    void testContieneSoloLetras() {
        assertTrue(CoverageUtils.contieneSoloLetras("Hola"));
        assertFalse(CoverageUtils.contieneSoloLetras("Hola123"));
        assertFalse(CoverageUtils.contieneSoloLetras(""));
        assertFalse(CoverageUtils.contieneSoloLetras(null));
    }

    @Test
    void testFormatearNombre() {
        assertEquals("Ana", CoverageUtils.formatearNombre("ana"));
        assertEquals("Carlos", CoverageUtils.formatearNombre(" CARLOS "));
        assertEquals("Nombre inválido", CoverageUtils.formatearNombre(" "));
        assertEquals("Nombre inválido", CoverageUtils.formatearNombre(null));
    }

    @Test
    void testEsPar() {
        assertTrue(CoverageUtils.esNumeroPar(4));
        assertFalse(CoverageUtils.esNumeroPar(3));
    }

    @Test
void testEsPalindromo() {
    assertTrue(CoverageUtils.esPalindromo("Anita lava la tina"));
    assertFalse(CoverageUtils.esPalindromo("Hola mundo"));
    assertFalse(CoverageUtils.esPalindromo(null));
}

@Test
void testMaximo() {
    assertEquals(5, CoverageUtils.maximo(5, 3));
    assertEquals(8, CoverageUtils.maximo(2, 8));
}

@Test
void testClasificarTemperatura() {
    assertEquals("Congelante", CoverageUtils.clasificarTemperatura(-10));
    assertEquals("Frío", CoverageUtils.clasificarTemperatura(10));
    assertEquals("Templado", CoverageUtils.clasificarTemperatura(22));
    assertEquals("Caliente", CoverageUtils.clasificarTemperatura(35));
}

@Test
void testContieneNumero() {
    assertTrue(CoverageUtils.contieneNumero("abc123"));
    assertFalse(CoverageUtils.contieneNumero("soloTexto"));
    assertFalse(CoverageUtils.contieneNumero(null));
}

@Test
void testCalificarNota() {
    assertEquals("Inválida", CoverageUtils.calificarNota(-1));
    assertEquals("Inválida", CoverageUtils.calificarNota(6));
    assertEquals("Excelente", CoverageUtils.calificarNota(4.6));
    assertEquals("Buena", CoverageUtils.calificarNota(4.0));
    assertEquals("Regular", CoverageUtils.calificarNota(3.0));
    assertEquals("Insuficiente", CoverageUtils.calificarNota(1.5));
}

}
