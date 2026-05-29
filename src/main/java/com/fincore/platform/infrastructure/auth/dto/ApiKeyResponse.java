package com.fincore.platform.infrastructure.auth.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ApiKeyResponse {
    private String id;
    private String nombre;
    private String apiKey;
    private boolean activa;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaRevocacion;
}
