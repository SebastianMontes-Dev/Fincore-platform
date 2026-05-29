package com.fincore.platform.infrastructure.notification.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificacionResponse {
    private String id;
    private String empresaNombre;
    private String tipo;
    private String mensaje;
    private String estado;
    private LocalDateTime fechaEnvio;
}
