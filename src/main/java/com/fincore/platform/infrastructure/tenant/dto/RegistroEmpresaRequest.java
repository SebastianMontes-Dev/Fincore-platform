package com.fincore.platform.infrastructure.tenant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RegistroEmpresaRequest {
    @NotBlank @Size(max = 150)
    private String nombre;
    @NotBlank @Email @Size(max = 150)
    private String email;
    @Size(max = 20)
    private String telefono;
    private String direccion;
}
