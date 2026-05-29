package com.fincore.platform.infrastructure.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class GenerarApiKeyRequest {
    @NotBlank @Size(max = 100)
    private String nombre;
}
