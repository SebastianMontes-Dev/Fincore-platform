package com.fincore.platform.infrastructure.common.exception;

public class RateLimitExcedidoException extends RuntimeException {
    public RateLimitExcedidoException(String mensaje) { super(mensaje); }
}
