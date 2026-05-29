package com.fincore.platform.infrastructure.auth.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransaccionExternaRequest {
    private String tipo;
    private BigDecimal monto;
    private String descripcion;
    private String categoria;
    private LocalDateTime fecha;
    private Map<String, String> metadatos;
}
