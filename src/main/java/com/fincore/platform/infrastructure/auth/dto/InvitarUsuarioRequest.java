package com.fincore.platform.infrastructure.auth.dto;

import com.fincore.platform.infrastructure.auth.domain.RolEmpresa;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InvitarUsuarioRequest {
    private String email;
    private RolEmpresa rol;
}
