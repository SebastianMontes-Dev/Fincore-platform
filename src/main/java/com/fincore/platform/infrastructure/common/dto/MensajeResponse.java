package com.fincore.platform.infrastructure.common.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MensajeResponse {
    @Builder.Default
    private boolean exitoso = true;
    private String mensaje;
    private Object datos;
}
