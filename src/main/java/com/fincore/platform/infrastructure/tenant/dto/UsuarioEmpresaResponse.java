package com.fincore.platform.infrastructure.tenant.dto;

import com.fincore.platform.infrastructure.auth.domain.RolEmpresa;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UsuarioEmpresaResponse {
    private String empresaId;
    private String nombreEmpresa;
    private RolEmpresa rol;
    private String fechaUnion;
}
