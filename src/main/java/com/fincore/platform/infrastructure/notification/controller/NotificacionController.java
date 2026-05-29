package com.fincore.platform.infrastructure.notification.controller;

import com.fincore.platform.infrastructure.common.dto.MensajeResponse;
import com.fincore.platform.infrastructure.notification.domain.WebhookConfig;
import com.fincore.platform.infrastructure.notification.dto.*;
import com.fincore.platform.infrastructure.notification.service.NotificacionService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Notificaciones", description = "Notificaciones y configuracion de webhooks")
@SecurityRequirement(name = "Bearer")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping("/notificaciones/usuario/{usuarioId}")
    @Operation(summary = "Listar notificaciones del usuario")
    public ResponseEntity<List<NotificacionResponse>> listar(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(notificacionService.listarPorUsuario(usuarioId));
    }

    @PostMapping("/empresas/{empresaId}/webhook")
    @Operation(summary = "Configurar webhook para anomalias")
    public ResponseEntity<MensajeResponse> configurarWebhook(
            @PathVariable UUID empresaId, @Valid @RequestBody ConfigurarWebhookRequest request) {
        return ResponseEntity.ok(notificacionService.configurarWebhook(empresaId, request));
    }

    @GetMapping("/empresas/{empresaId}/webhook")
    @Operation(summary = "Ver configuracion del webhook")
    public ResponseEntity<WebhookConfig> obtenerWebhook(@PathVariable UUID empresaId) {
        WebhookConfig config = notificacionService.obtenerWebhook(empresaId);
        return config != null ? ResponseEntity.ok(config) : ResponseEntity.notFound().build();
    }
}
