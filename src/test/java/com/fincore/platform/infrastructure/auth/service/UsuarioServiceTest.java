package com.fincore.platform.infrastructure.auth.service;

import com.fincore.platform.infrastructure.auth.domain.*;
import com.fincore.platform.infrastructure.auth.dto.*;
import com.fincore.platform.infrastructure.auth.repository.*;
import com.fincore.platform.infrastructure.auth.security.JwtService;
import com.fincore.platform.infrastructure.common.exception.*;
import com.fincore.platform.infrastructure.tenant.domain.*;
import com.fincore.platform.infrastructure.tenant.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private UsuarioEmpresaRepository usuarioEmpresaRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private JavaMailSender mailSender;

    @InjectMocks private UsuarioService usuarioService;

    private Usuario usuario;
    private final UUID usuarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(usuarioId).email("juan@test.com")
                .password("encoded").nombre("Juan").apellido("Perez")
                .emailVerificado(true).build();
    }

    @Test
    void registrarExitoso() {
        var req = new RegistroUsuarioRequest("juan@test.com", "pass123", "Juan", "Perez");
        when(usuarioRepository.existsByEmail("juan@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(usuarioRepository.save(any())).thenReturn(usuario);
        when(usuarioEmpresaRepository.findByUsuarioId(any())).thenReturn(List.of());
        doNothing().when(mailSender).send(any());

        var resp = usuarioService.registrar(req);
        assertEquals("juan@test.com", resp.getEmail());
    }

    @Test
    void loginExitoso() {
        var login = new LoginRequest("juan@test.com", "pass123");
        var empresa = Empresa.builder().id(UUID.randomUUID()).nombre("X").build();
        var membresia = UsuarioEmpresa.builder().usuario(usuario).empresa(empresa)
                .rol(RolEmpresa.OWNER).build();

        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("pass123", "encoded")).thenReturn(true);
        when(usuarioEmpresaRepository.findByUsuarioId(usuarioId)).thenReturn(List.of(membresia));
        when(jwtService.generarAccessToken(any(), any(), any(), any())).thenReturn("access");
        when(jwtService.generarRefreshToken(any())).thenReturn("refresh");
        when(jwtService.getAccessTokenMs()).thenReturn(900000L);
        when(refreshTokenRepository.save(any())).thenReturn(null);

        var resp = usuarioService.login(login);
        assertEquals("access", resp.getAccessToken());
        assertEquals("refresh", resp.getRefreshToken());
    }

    @Test
    void loginContrasenaIncorrecta() {
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);
        assertThrows(BadCredentialsException.class,
                () -> usuarioService.login(new LoginRequest("juan@test.com", "mal")));
    }

    @Test
    void loginEmailNoVerificado() {
        usuario.setEmailVerificado(false);
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        assertThrows(NegocioException.class,
                () -> usuarioService.login(new LoginRequest("juan@test.com", "pass")));
    }

    @Test
    void verificarEmail() {
        usuario.setEmailVerificado(false);
        usuario.setTokenVerificacion("tok123");
        when(usuarioRepository.findByTokenVerificacion("tok123")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any())).thenReturn(usuario);

        usuarioService.verificarEmail("tok123");
        assertTrue(usuario.isEmailVerificado());
    }

    @Test
    void cerrarSesion() {
        doNothing().when(refreshTokenRepository).deleteAllByUsuarioId(usuarioId);
        usuarioService.cerrarSesion(usuarioId);
        verify(refreshTokenRepository).deleteAllByUsuarioId(usuarioId);
    }
}
