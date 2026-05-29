package com.fincore.platform.infrastructure.auth.security;

import lombok.*;
import java.util.UUID;

@Getter @Builder
public class FinCoreUserDetails {
    private UUID usuarioId;
    private UUID tenantId;
    private String rol;
    private String token;
}
