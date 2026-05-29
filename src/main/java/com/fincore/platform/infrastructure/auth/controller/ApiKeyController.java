package com.fincore.platform.infrastructure.auth.controller;

import com.fincore.platform.infrastructure.auth.dto.*;
import com.fincore.platform.infrastructure.auth.service.ApiKeyService;
import com.fincore.platform.infrastructure.common.dto.MensajeResponse;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/empresas/{empresaId}/api-keys")
@Tag(name = "API Keys", description = "Generar y gestionar API Keys")
@SecurityRequirement(name = "Bearer")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) { this.apiKeyService = apiKeyService; }

    @PostMapping
    @Operation(summary = "Generar nueva API Key")
    public ResponseEntity<ApiKeyResponse> generar(
            @PathVariable UUID empresaId,
            @Valid @RequestBody GenerarApiKeyRequest request,
            @RequestAttribute(value = "usuarioId") UUID usuarioId) {
        return new ResponseEntity<>(apiKeyService.generar(empresaId, usuarioId, request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar API Keys activas")
    public ResponseEntity<List<ApiKeyResponse>> listar(@PathVariable UUID empresaId) {
        return ResponseEntity.ok(apiKeyService.listar(empresaId));
    }

    @DeleteMapping("/{apiKeyId}")
    @Operation(summary = "Revocar API Key")
    public ResponseEntity<MensajeResponse> revocar(
            @PathVariable UUID empresaId, @PathVariable UUID apiKeyId) {
        apiKeyService.revocar(empresaId, apiKeyId);
        return ResponseEntity.ok(MensajeResponse.builder().mensaje("API Key revocada").build());
    }
}
