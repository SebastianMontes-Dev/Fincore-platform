package com.fincore.platform.infrastructure.common.exception;

public class AccesoNoAutorizadoException extends RuntimeException {
    public AccesoNoAutorizadoException(String mensaje) { super(mensaje); }
}
