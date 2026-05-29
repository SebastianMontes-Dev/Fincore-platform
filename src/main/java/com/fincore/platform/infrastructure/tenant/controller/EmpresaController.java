package com.fincore.platform.infrastructure.tenant.controller;

import com.fincore.platform.infrastructure.common.dto.MensajeResponse;
import com.fincore.platform.infrastructure.tenant.dto.*;
import com.fincore.platform.infrastructure.tenant.service.EmpresaService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/empresas")
@Tag(name = "Empresas", description = "Gestion de empresas (tenants)")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) { this.empresaService = empresaService; }

    @PostMapping("/registro")
    @Operation(summary = "Registrar una empresa nueva")
    public ResponseEntity<EmpresaResponse> registrar(@Valid @RequestBody RegistroEmpresaRequest request) {
        return new ResponseEntity<>(empresaService.registrar(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener empresa por ID")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<EmpresaResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(empresaService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desactivar empresa")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<MensajeResponse> desactivar(@PathVariable UUID id) {
        return ResponseEntity.ok(empresaService.desactivar(id));
    }
}
