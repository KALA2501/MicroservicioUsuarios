package com.usuarios.demo.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class FunctionTest {
    @Test
    void testSyncData() {
        Function service = new Function();
        assertDoesNotThrow(service::syncData);
    }
}
