package com.fincore.platform.infrastructure.notification.service;

import com.fincore.platform.infrastructure.auth.domain.*;
import com.fincore.platform.infrastructure.notification.domain.Notificacion;
import com.fincore.platform.infrastructure.notification.dto.ConfigurarWebhookRequest;
import com.fincore.platform.infrastructure.notification.repository.*;
import com.fincore.platform.infrastructure.tenant.domain.*;
import com.fincore.platform.infrastructure.tenant.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock private NotificacionRepository notificacionRepository;
    @Mock private UsuarioEmpresaRepository usuarioEmpresaRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private WebhookConfigRepository webhookConfigRepository;
    @Mock private JavaMailSender mailSender;

    @InjectMocks private NotificacionService notificacionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificacionService, "maxReintentos", 1);
        ReflectionTestUtils.setField(notificacionService, "backoffSegundos", 1);
    }

    @Test
    void configurarWebhookNuevo() {
        UUID empresaId = UUID.randomUUID();
        var empresa = Empresa.builder().id(empresaId).nombre("X").build();
        when(webhookConfigRepository.findByEmpresaId(empresaId)).thenReturn(Optional.empty());
        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
        when(webhookConfigRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var resp = notificacionService.configurarWebhook(empresaId,
                new ConfigurarWebhookRequest("https://hook.test"));
        assertTrue(resp.isExitoso());
    }

    @Test
    void procesarAlertaExitoso() {
        UUID empresaId = UUID.randomUUID();
        var empresa = Empresa.builder().id(empresaId).nombre("X").build();
        var usuario = Usuario.builder().id(UUID.randomUUID()).email("u@test.com").nombre("U").build();
        var membresia = UsuarioEmpresa.builder().usuario(usuario).empresa(empresa)
                .rol(RolEmpresa.OWNER).build();

        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
        when(usuarioEmpresaRepository.findByEmpresaId(empresaId)).thenReturn(List.of(membresia));
        when(notificacionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(mailSender).send(any());

        notificacionService.procesarAlerta(empresaId, "Test");
        verify(notificacionRepository).save(any(Notificacion.class));
    }

    @Test
    void listarVacio() {
        when(notificacionRepository.findByUsuarioIdOrderByFechaEnvioDesc(any()))
                .thenReturn(List.of());
        assertTrue(notificacionService.listarPorUsuario(UUID.randomUUID()).isEmpty());
    }
}
