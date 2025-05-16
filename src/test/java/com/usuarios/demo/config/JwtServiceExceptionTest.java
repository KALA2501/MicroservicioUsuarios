package com.usuarios.demo.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import com.usuarios.demo.exceptions.JwtServiceException;


class JwtServiceExceptionTest {

    @Test
    void testJwtServiceExceptionWithMessage() {
        String expectedMessage = "JWT service error occurred";
        
        // Create exception with only the message
        JwtServiceException exception = new JwtServiceException(expectedMessage);
        
        // Assert that the exception message matches
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testJwtServiceExceptionWithMessageAndCause() {
        String expectedMessage = "JWT service error occurred";
        Throwable expectedCause = new Throwable("Cause of the error");
        
        // Create exception with message and cause
        JwtServiceException exception = new JwtServiceException(expectedMessage, expectedCause);
        
        // Assert that the message matches
        assertEquals(expectedMessage, exception.getMessage());
        
        // Assert that the cause matches
        assertEquals(expectedCause, exception.getCause());
    }

    @Test
    void testJwtServiceExceptionWithNullMessage() {
        Throwable expectedCause = new Throwable("Cause of the error");
        
        // Create exception with null message and a cause
        JwtServiceException exception = new JwtServiceException(null, expectedCause);
        
        // Assert that the cause matches
        assertEquals(expectedCause, exception.getCause());
        assertNull(exception.getMessage());  // Message should be null
    }

    @Test
    void testJwtServiceExceptionWithNullCause() {
        String expectedMessage = "JWT service error occurred";
        
        // Create exception with a message and null cause
        JwtServiceException exception = new JwtServiceException(expectedMessage, null);
        
        // Assert that the message matches
        assertEquals(expectedMessage, exception.getMessage());
        
        // Assert that the cause is null
        assertNull(exception.getCause());
    }
}