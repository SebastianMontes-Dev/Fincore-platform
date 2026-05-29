package com.fincore.platform.infrastructure.auth.controller;

import com.fincore.platform.infrastructure.auth.domain.ApiKey;
import com.fincore.platform.infrastructure.auth.domain.LogApi;
import com.fincore.platform.infrastructure.auth.dto.TransaccionExternaRequest;
import com.fincore.platform.infrastructure.auth.repository.ApiKeyRepository;
import com.fincore.platform.infrastructure.auth.repository.LogApiRepository;
import com.fincore.platform.infrastructure.auth.security.ApiKeyAuthFilter;
import com.fincore.platform.infrastructure.common.dto.MensajeResponse;
import com.fincore.platform.infrastructure.common.exception.RateLimitExcedidoException;
import io.github.bucket4j.*;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/publico")
@Tag(name = "API Publica", description = "Endpoints para sistemas externos")
@SecurityRequirement(name = "API-Key")
public class PublicoController {

    private final ApiKeyRepository apiKeyRepository;
    private final LogApiRepository logApiRepository;
    private final int limiteRequests;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public PublicoController(ApiKeyRepository apiKeyRepository,
                             LogApiRepository logApiRepository,
                             @Value("${app.api.rate-limit-requests-per-minute}") int limiteRequests) {
        this.apiKeyRepository = apiKeyRepository;
        this.logApiRepository = logApiRepository;
        this.limiteRequests = limiteRequests;
    }

    @PostMapping("/transacciones")
    @Operation(summary = "Registrar transaccion desde sistema externo")
    public ResponseEntity<MensajeResponse> registrar(
            @RequestBody TransaccionExternaRequest request,
            HttpServletRequest httpRequest) {

        String apiKeyRaw = httpRequest.getHeader("X-API-Key");

        if (apiKeyRaw == null || apiKeyRaw.isEmpty()) {
            return new ResponseEntity<>(
                    MensajeResponse.builder().exitoso(false).mensaje("API Key no proporcionada").build(),
                    HttpStatus.UNAUTHORIZED);
        }

        Bucket bucket = buckets.computeIfAbsent(apiKeyRaw, k -> Bucket.builder()
                .addLimit(Bandwidth.classic(limiteRequests, Refill.greedy(limiteRequests, Duration.ofMinutes(1))))
                .build());
        if (!bucket.tryConsume(1)) {
            throw new RateLimitExcedidoException("Limite de " + limiteRequests + " peticiones por minuto excedido");
        }

        String hash = ApiKeyAuthFilter.hashApiKey(apiKeyRaw);
        ApiKey apiKey = apiKeyRepository.findByClaveHash(hash).orElse(null);
        int codigoRespuesta = HttpStatus.OK.value();

        try {
            if (apiKey == null || !apiKey.isActiva()) {
                codigoRespuesta = HttpStatus.UNAUTHORIZED.value();
                return new ResponseEntity<>(
                        MensajeResponse.builder().exitoso(false).mensaje("API Key invalida").build(),
                        HttpStatus.UNAUTHORIZED);
            }

            return ResponseEntity.ok(MensajeResponse.builder()
                    .mensaje("Transaccion registrada correctamente")
                    .datos(Map.of("empresa", apiKey.getEmpresa().getNombre())).build());

        } finally {
            LogApi log = LogApi.builder()
                    .apiKey(apiKey).endpoint("/api/v1/publico/transacciones")
                    .metodo("POST").cuerpoPeticion(request.toString())
                    .codigoRespuesta(codigoRespuesta).direccionIp(httpRequest.getRemoteAddr()).build();
            logApiRepository.save(log);
        }
    }

    @GetMapping("/ping")
    @Operation(summary = "Verificar que la API funciona")
    public ResponseEntity<MensajeResponse> ping() {
        return ResponseEntity.ok(MensajeResponse.builder().mensaje("API FinCore funcionando").build());
    }
}
