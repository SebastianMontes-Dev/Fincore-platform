package com.fincore.platform.infrastructure.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor
public class CambiarEmpresaRequest {
    @NotNull
    private UUID empresaId;
}
