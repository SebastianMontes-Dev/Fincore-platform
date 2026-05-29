package com.fincore.platform.infrastructure.auth.controller;

import com.fincore.platform.infrastructure.auth.dto.*;
import com.fincore.platform.infrastructure.auth.security.FinCoreUserDetails;
import com.fincore.platform.infrastructure.auth.security.UsuarioActual;
import com.fincore.platform.infrastructure.auth.service.UsuarioService;
import com.fincore.platform.infrastructure.common.dto.MensajeResponse;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Autenticacion", description = "Registro, login, cambio de empresa")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) { this.usuarioService = usuarioService; }

    @PostMapping("/usuarios/registro")
    @Operation(summary = "Registrar un usuario nuevo")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroUsuarioRequest request) {
        return new ResponseEntity<>(usuarioService.registrar(request), HttpStatus.CREATED);
    }

    @PostMapping("/auth/login")
    @Operation(summary = "Iniciar sesion")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }

    @PostMapping("/auth/refrescar")
    @Operation(summary = "Renovar access token")
    public ResponseEntity<JwtResponse> refrescar(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(usuarioService.refrescarToken(request));
    }

    @GetMapping("/auth/verificar")
    @Operation(summary = "Verificar email")
    public ResponseEntity<MensajeResponse> verificarEmail(@RequestParam String token) {
        usuarioService.verificarEmail(token);
        return ResponseEntity.ok(MensajeResponse.builder()
                .mensaje("Email verificado. Ya puedes iniciar sesion.").build());
    }

    @PostMapping("/auth/cerrar-sesion")
    @Operation(summary = "Cerrar sesion")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<MensajeResponse> cerrarSesion(@UsuarioActual FinCoreUserDetails userDetails) {
        usuarioService.cerrarSesion(userDetails.getUsuarioId());
        return ResponseEntity.ok(MensajeResponse.builder().mensaje("Sesion cerrada").build());
    }

    @GetMapping("/usuarios/{id}")
    @Operation(summary = "Obtener usuario por ID")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<UsuarioResponse> obtenerUsuario(@PathVariable UUID id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PostMapping("/empresas/{id}/invitar")
    @Operation(summary = "Invitar usuario a la empresa")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<MensajeResponse> invitar(
            @PathVariable UUID id, @RequestBody InvitarUsuarioRequest request,
            @UsuarioActual FinCoreUserDetails userDetails) {
        return ResponseEntity.ok(usuarioService.invitarUsuario(id, userDetails.getUsuarioId(), request));
    }

    @PostMapping("/auth/cambiar-empresa")
    @Operation(summary = "Cambiar de empresa activa")
    @SecurityRequirement(name = "Bearer")
    public ResponseEntity<JwtResponse> cambiarEmpresa(
            @Valid @RequestBody CambiarEmpresaRequest request,
            @UsuarioActual FinCoreUserDetails userDetails) {
        return ResponseEntity.ok(usuarioService.cambiarEmpresa(userDetails.getUsuarioId(), request));
    }
}
