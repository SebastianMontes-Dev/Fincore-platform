package com.fincore.platform.infrastructure.auth.security;

import com.fincore.platform.infrastructure.auth.domain.ApiKey;
import com.fincore.platform.infrastructure.auth.repository.ApiKeyRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HexFormat;
import java.util.Optional;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyAuthFilter(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String apiKeyRaw = request.getHeader("X-API-Key");
        if (apiKeyRaw != null && !apiKeyRaw.isEmpty()) {
            String hash = hashApiKey(apiKeyRaw);
            Optional<ApiKey> apiKeyOpt = apiKeyRepository.findByClaveHash(hash);
            if (apiKeyOpt.isPresent() && apiKeyOpt.get().isActiva()) {
                ApiKey apiKey = apiKeyOpt.get();
                FinCoreUserDetails userDetails = FinCoreUserDetails.builder()
                        .tenantId(apiKey.getEmpresa().getId()).rol("API_CLIENT").build();
                var auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_API_CLIENT")));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/publico/");
    }

    public static String hashApiKey(String apiKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error al hashear API Key", e);
        }
    }
}
