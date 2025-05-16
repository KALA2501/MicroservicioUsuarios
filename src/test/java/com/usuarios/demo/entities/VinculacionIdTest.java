package com.usuarios.demo.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VinculacionIdTest {

    private VinculacionId vinculacionId1;
    private VinculacionId vinculacionId2;
    private VinculacionId vinculacionId3;

    @BeforeEach
    void setUp() {
        // Initialize the VinculacionId objects with sample data
        vinculacionId1 = new VinculacionId("paciente1", "medico1");
        vinculacionId2 = new VinculacionId("paciente1", "medico1");
        vinculacionId3 = new VinculacionId("paciente2", "medico2");
    }

    @Test
    void testEquals_SameObject() {
        // Same object comparison should return true
        assertTrue(vinculacionId1.equals(vinculacionId1));
    }

    @Test
    void testEquals_DifferentObjectSameValues() {
        // Different object but same values should return true
        assertTrue(vinculacionId1.equals(vinculacionId2));
    }

    @Test
    void testEquals_DifferentObjectDifferentValues() {
        // Different object and different values should return false
        assertFalse(vinculacionId1.equals(vinculacionId3));
    }

    @Test
    void testEquals_NullObject() {
        // Comparing with null should return false
        assertFalse(vinculacionId1.equals(null));
    }

    @Test
    void testEquals_DifferentClass() {
        // Comparing with an object of a different class should return false
        assertFalse(vinculacionId1.equals("String"));
    }

    @Test
    void testHashCode_EqualObjects() {
        // Two objects with the same values should have the same hash code
        assertEquals(vinculacionId1.hashCode(), vinculacionId2.hashCode());
    }

    @Test
    void testHashCode_DifferentObjects() {
        // Two objects with different values should have different hash codes
        assertNotEquals(vinculacionId1.hashCode(), vinculacionId3.hashCode());
    }

    @Test
    void testConstructor() {
        // Check that the constructor properly sets the fields
        assertEquals("paciente1", vinculacionId1.getPaciente());
        assertEquals("medico1", vinculacionId1.getMedico());
    }
}
