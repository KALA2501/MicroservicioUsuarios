package com.usuarios.demo.exceptions;

public class JwtServiceException extends RuntimeException {
    public JwtServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public JwtServiceException(String message) {
        super(message);
    }
}
