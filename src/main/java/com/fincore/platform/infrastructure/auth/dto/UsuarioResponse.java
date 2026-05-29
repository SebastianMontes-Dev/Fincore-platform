package com.fincore.platform.infrastructure.auth.dto;

import com.fincore.platform.infrastructure.tenant.dto.EmpresaResponse;
import com.fincore.platform.infrastructure.tenant.dto.UsuarioEmpresaResponse;
import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UsuarioResponse {
    private String id;
    private String email;
    private String nombre;
    private String apellido;
    private boolean emailVerificado;
    private List<UsuarioEmpresaResponse> empresas;
}
