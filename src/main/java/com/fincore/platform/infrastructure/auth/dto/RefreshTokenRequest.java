package com.fincore.platform.infrastructure.auth.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RefreshTokenRequest {
    private String refreshToken;
}
