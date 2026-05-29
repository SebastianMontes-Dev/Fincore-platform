package com.fincore.platform.infrastructure.common.exception;

import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.*;

import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarRecursoNoEncontrado(
            RecursoNoEncontradoException ex, WebRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), request);
    }

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErrorResponse> manejarErrorNegocio(NegocioException ex, WebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Error de negocio", ex.getMessage(), request);
    }

    @ExceptionHandler(AccesoNoAutorizadoException.class)
    public ResponseEntity<ErrorResponse> manejarAccesoNoAutorizado(
            AccesoNoAutorizadoException ex, WebRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> manejarCredencialesInvalidas(
            BadCredentialsException ex, WebRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Credenciales invalidas",
                "El email o la contrasena son incorrectos", request);
    }

    @ExceptionHandler(RateLimitExcedidoException.class)
    public ResponseEntity<ErrorResponse> manejarRateLimit(
            RateLimitExcedidoException ex, WebRequest request) {
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, "Limite excedido", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarErrorGeneral(Exception ex, WebRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrio un error inesperado", request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            errores.put(campo, error.getDefaultMessage());
        });
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        ErrorResponse error = ErrorResponse.builder()
                .status(400).error("Datos invalidos")
                .message(errores.toString()).timestamp(LocalDateTime.now()).path(path).build();
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String error, String message, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        return new ResponseEntity<>(ErrorResponse.builder()
                .status(status.value()).error(error).message(message)
                .timestamp(LocalDateTime.now()).path(path).build(), status);
    }
}
