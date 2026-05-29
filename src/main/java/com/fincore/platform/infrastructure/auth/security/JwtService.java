package com.fincore.platform.infrastructure.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService {

    private final SecretKey secretKey;
    private final long accessTokenMs;
    private final long refreshTokenMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration}") long accessMinutes,
            @Value("${app.jwt.refresh-token-expiration-days}") long refreshDays) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                Base64.getEncoder().encodeToString(secret.getBytes())));
        this.accessTokenMs = accessMinutes * 60 * 1000;
        this.refreshTokenMs = refreshDays * 24 * 60 * 60 * 1000;
    }

    public String generarAccessToken(UUID usuarioId, String email, UUID empresaId, String rol) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuarioId.toString())
                .claim("email", email)
                .claim("tenant_id", empresaId != null ? empresaId.toString() : null)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + accessTokenMs))
                .signWith(secretKey)
                .compact();
    }

    public String generarRefreshToken(UUID usuarioId) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(usuarioId.toString())
                .claim("tipo", "refresh")
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + refreshTokenMs))
                .signWith(secretKey)
                .compact();
    }

    public boolean esTokenValido(String token) {
        try {
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Jws<Claims> obtenerClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
    }

    public UUID obtenerUsuarioId(String token) {
        return UUID.fromString(obtenerClaims(token).getPayload().getSubject());
    }

    public UUID obtenerTenantId(String token) {
        Claims claims = obtenerClaims(token).getPayload();
        String tid = claims.get("tenant_id", String.class);
        return tid != null ? UUID.fromString(tid) : null;
    }

    public String obtenerRol(String token) {
        return obtenerClaims(token).getPayload().get("rol", String.class);
    }

    public long getAccessTokenMs() { return accessTokenMs; }
}
