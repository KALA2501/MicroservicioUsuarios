package com.usuarios.demo.exceptions;

public class CentroMedicoException extends RuntimeException {
    public CentroMedicoException(String message, Throwable cause) {
        super(message, cause);
    }

    public CentroMedicoException(String message) {
        super(message);
    }
}
