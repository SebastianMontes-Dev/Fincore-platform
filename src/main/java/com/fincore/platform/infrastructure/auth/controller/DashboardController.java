package com.fincore.platform.infrastructure.auth.controller;

import com.fincore.platform.infrastructure.auth.security.FinCoreUserDetails;
import com.fincore.platform.infrastructure.auth.security.UsuarioActual;
import com.fincore.platform.infrastructure.auth.service.UsuarioService;
import com.fincore.platform.infrastructure.common.dto.MensajeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Dashboard", description = "Resumen del usuario logueado")
@SecurityRequirement(name = "Bearer")
public class DashboardController {

    private final UsuarioService usuarioService;

    public DashboardController(UsuarioService usuarioService) { this.usuarioService = usuarioService; }

    @GetMapping("/dashboard")
    @Operation(summary = "Resumen del usuario con su empresa")
    public ResponseEntity<Map<String, Object>> dashboard(@UsuarioActual FinCoreUserDetails userDetails) {
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("usuarioId", userDetails.getUsuarioId());
        resumen.put("rol", userDetails.getRol());
        resumen.put("tenantId", userDetails.getTenantId());
        resumen.put("mensaje", "Bienvenido a FinCore");
        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check publico")
    public ResponseEntity<MensajeResponse> health() {
        return ResponseEntity.ok(MensajeResponse.builder()
                .mensaje("FinCore corriendo sin problemas")
                .datos(Map.of("status", "UP")).build());
    }
}
