package com.fincore.platform.infrastructure.common.exception;

public class NegocioException extends RuntimeException {
    public NegocioException(String mensaje) { super(mensaje); }
}
