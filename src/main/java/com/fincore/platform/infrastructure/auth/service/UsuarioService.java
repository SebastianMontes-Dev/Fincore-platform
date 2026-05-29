package com.fincore.platform.infrastructure.auth.service;

import com.fincore.platform.infrastructure.auth.dto.*;
import com.fincore.platform.infrastructure.auth.security.FinCoreUserDetails;
import com.fincore.platform.infrastructure.auth.security.JwtService;
import com.fincore.platform.infrastructure.auth.domain.*;
import com.fincore.platform.infrastructure.auth.repository.*;
import com.fincore.platform.infrastructure.common.exception.*;
import com.fincore.platform.infrastructure.tenant.domain.*;
import com.fincore.platform.infrastructure.tenant.repository.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioEmpresaRepository usuarioEmpresaRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JavaMailSender mailSender;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          EmpresaRepository empresaRepository,
                          UsuarioEmpresaRepository usuarioEmpresaRepository,
                          RefreshTokenRepository refreshTokenRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          JavaMailSender mailSender) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioEmpresaRepository = usuarioEmpresaRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mailSender = mailSender;
    }

    @Transactional
    public UsuarioResponse registrar(RegistroUsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new NegocioException("Ya existe un usuario con ese email");
        }
        String tokenVerificacion = UUID.randomUUID().toString();
        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nombre(request.getNombre()).apellido(request.getApellido())
                .emailVerificado(false).tokenVerificacion(tokenVerificacion)
                .build();
        usuario = usuarioRepository.save(usuario);
        enviarEmailVerificacion(usuario);
        return mapearAResponse(usuario);
    }

    @Transactional
    public JwtResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("Credenciales invalidas");
        }
        if (!usuario.isEmailVerificado()) {
            throw new NegocioException("Debes verificar tu email antes de iniciar sesion");
        }

        UUID empresaId = null;
        String rol = "USER";
        List<UsuarioEmpresa> membresias = usuarioEmpresaRepository.findByUsuarioId(usuario.getId());
        if (!membresias.isEmpty()) {
            UsuarioEmpresa m = membresias.get(0);
            empresaId = m.getEmpresa().getId();
            rol = m.getRol().name();
        }

        String accessToken = jwtService.generarAccessToken(usuario.getId(), usuario.getEmail(), empresaId, rol);
        String refreshToken = jwtService.generarRefreshToken(usuario.getId());

        RefreshToken rt = RefreshToken.builder()
                .usuario(usuario).token(refreshToken)
                .fechaExpiracion(LocalDateTime.now().plusDays(7)).revocado(false).build();
        refreshTokenRepository.save(rt);

        return JwtResponse.crear(accessToken, refreshToken, jwtService.getAccessTokenMs());
    }

    @Transactional
    public JwtResponse refrescarToken(RefreshTokenRequest request) {
        RefreshToken rt = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new NegocioException("Refresh token invalido"));
        if (rt.isRevocado() || rt.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            throw new NegocioException("Refresh token expirado o revocado");
        }
        Usuario usuario = rt.getUsuario();
        rt.setRevocado(true);
        refreshTokenRepository.save(rt);

        UUID empresaId = null;
        String rol = "USER";
        List<UsuarioEmpresa> membresias = usuarioEmpresaRepository.findByUsuarioId(usuario.getId());
        if (!membresias.isEmpty()) {
            empresaId = membresias.get(0).getEmpresa().getId();
            rol = membresias.get(0).getRol().name();
        }

        String accessToken = jwtService.generarAccessToken(usuario.getId(), usuario.getEmail(), empresaId, rol);
        String newRefreshToken = jwtService.generarRefreshToken(usuario.getId());

        RefreshToken newRt = RefreshToken.builder()
                .usuario(usuario).token(newRefreshToken)
                .fechaExpiracion(LocalDateTime.now().plusDays(7)).revocado(false).build();
        refreshTokenRepository.save(newRt);

        return JwtResponse.crear(accessToken, newRefreshToken, jwtService.getAccessTokenMs());
    }

    @Transactional
    public void verificarEmail(String token) {
        Usuario usuario = usuarioRepository.findByTokenVerificacion(token)
                .orElseThrow(() -> new NegocioException("Token de verificacion invalido"));
        usuario.setEmailVerificado(true);
        usuario.setTokenVerificacion(null);
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return mapearAResponse(usuario);
    }

    @Transactional
    public com.fincore.platform.infrastructure.common.dto.MensajeResponse invitarUsuario(
            UUID empresaId, UUID ownerId, InvitarUsuarioRequest request) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada"));
        boolean esOwner = usuarioEmpresaRepository.existsByUsuarioIdAndEmpresaIdAndRol(
                ownerId, empresaId, RolEmpresa.OWNER);
        if (!esOwner) throw new AccesoNoAutorizadoException("Solo el OWNER puede invitar usuarios");

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));

        if (usuarioEmpresaRepository.findByUsuarioIdAndEmpresaId(usuario.getId(), empresaId).isPresent()) {
            throw new NegocioException("Este usuario ya pertenece a la empresa");
        }

        UsuarioEmpresa membresia = UsuarioEmpresa.builder()
                .usuario(usuario).empresa(empresa).rol(request.getRol()).build();
        usuarioEmpresaRepository.save(membresia);

        return com.fincore.platform.infrastructure.common.dto.MensajeResponse.builder()
                .mensaje("Usuario invitado como " + request.getRol()).build();
    }

    @Transactional
    public void cerrarSesion(UUID usuarioId) {
        refreshTokenRepository.deleteAllByUsuarioId(usuarioId);
    }

    @Transactional
    public JwtResponse cambiarEmpresa(UUID usuarioId, CambiarEmpresaRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        UsuarioEmpresa membresia = usuarioEmpresaRepository
                .findByUsuarioIdAndEmpresaId(usuarioId, request.getEmpresaId())
                .orElseThrow(() -> new NegocioException("No perteneces a esta empresa"));

        String accessToken = jwtService.generarAccessToken(
                usuario.getId(), usuario.getEmail(), request.getEmpresaId(), membresia.getRol().name());
        String refreshToken = jwtService.generarRefreshToken(usuario.getId());

        RefreshToken rt = RefreshToken.builder()
                .usuario(usuario).token(refreshToken)
                .fechaExpiracion(LocalDateTime.now().plusDays(7)).revocado(false).build();
        refreshTokenRepository.save(rt);

        return JwtResponse.crear(accessToken, refreshToken, jwtService.getAccessTokenMs());
    }

    private void enviarEmailVerificacion(Usuario usuario) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(usuario.getEmail());
            msg.setSubject("Verifica tu cuenta en FinCore");
            msg.setText("Hola " + usuario.getNombre() + ",\n\n" +
                    "Gracias por registrarte. Verifica tu cuenta aqui:\n" +
                    "http://localhost:8080/api/v1/auth/verificar?token=" + usuario.getTokenVerificacion() +
                    "\n\nEquipo FinCore");
            mailSender.send(msg);
        } catch (Exception e) { /* fallo silencioso */ }
    }

    private UsuarioResponse mapearAResponse(Usuario usuario) {
        var empresas = usuarioEmpresaRepository.findByUsuarioId(usuario.getId())
                .stream().map(ue -> com.fincore.platform.infrastructure.tenant.dto.UsuarioEmpresaResponse.builder()
                        .empresaId(ue.getEmpresa().getId().toString())
                        .nombreEmpresa(ue.getEmpresa().getNombre())
                        .rol(ue.getRol()).fechaUnion(ue.getFechaUnion().toString()).build())
                .collect(Collectors.toList());
        return UsuarioResponse.builder()
                .id(usuario.getId().toString()).email(usuario.getEmail())
                .nombre(usuario.getNombre()).apellido(usuario.getApellido())
                .emailVerificado(usuario.isEmailVerificado()).empresas(empresas).build();
    }
}
