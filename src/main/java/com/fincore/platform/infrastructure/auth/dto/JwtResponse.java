package com.fincore.platform.infrastructure.auth.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private String tipoToken;
    private long expiraEn;

    public static JwtResponse crear(String accessToken, String refreshToken, long expiraEn) {
        return JwtResponse.builder()
                .accessToken(accessToken).refreshToken(refreshToken)
                .tipoToken("Bearer").expiraEn(expiraEn).build();
    }
}
