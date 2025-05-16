package com.usuarios.demo.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdminTest {

    @Test
    void testAdminEquality_DifferentValues() {
        Admin admin1 = new Admin("admin123", "John Doe");
        Admin admin2 = new Admin("admin124", "Jane Doe");

        // Test inequality for objects with different pkId or nombreCompleto
        assertNotEquals(admin1, admin2, "Admins with different pkId or nombreCompleto should not be equal");
    }

    @Test
    void testAdminHashCode_DifferentValues() {
        Admin admin1 = new Admin("admin123", "John Doe");
        Admin admin2 = new Admin("admin124", "Jane Doe");

        // Test hash code for unequal objects should be different
        assertNotEquals(admin1.hashCode(), admin2.hashCode(), "Admins with different pkId or nombreCompleto should have different hashCodes");
    }
}
