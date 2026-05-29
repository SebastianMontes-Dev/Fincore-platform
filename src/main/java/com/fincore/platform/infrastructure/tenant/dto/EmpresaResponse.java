package com.fincore.platform.infrastructure.tenant.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EmpresaResponse {
    private String id;
    private String nombre;
    private String email;
    private String telefono;
    private String direccion;
    private boolean activa;
    private LocalDateTime fechaCreacion;
}
