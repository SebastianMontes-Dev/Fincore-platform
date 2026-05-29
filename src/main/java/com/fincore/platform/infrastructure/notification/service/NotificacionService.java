package com.fincore.platform.infrastructure.notification.service;

import com.fincore.platform.infrastructure.auth.domain.Usuario;
import com.fincore.platform.infrastructure.common.dto.MensajeResponse;
import com.fincore.platform.infrastructure.common.exception.RecursoNoEncontradoException;
import com.fincore.platform.infrastructure.notification.domain.*;
import com.fincore.platform.infrastructure.notification.dto.*;
import com.fincore.platform.infrastructure.notification.repository.*;
import com.fincore.platform.infrastructure.tenant.domain.Empresa;
import com.fincore.platform.infrastructure.tenant.domain.UsuarioEmpresa;
import com.fincore.platform.infrastructure.tenant.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioEmpresaRepository usuarioEmpresaRepository;
    private final EmpresaRepository empresaRepository;
    private final WebhookConfigRepository webhookConfigRepository;
    private final JavaMailSender mailSender;
    private final int maxReintentos;
    private final int backoffSegundos;

    public NotificacionService(NotificacionRepository notificacionRepository,
                               UsuarioEmpresaRepository usuarioEmpresaRepository,
                               EmpresaRepository empresaRepository,
                               WebhookConfigRepository webhookConfigRepository,
                               JavaMailSender mailSender,
                               @Value("${app.webhook.max-retries}") int maxReintentos,
                               @Value("${app.webhook.retry-backoff-seconds}") int backoffSegundos) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioEmpresaRepository = usuarioEmpresaRepository;
        this.empresaRepository = empresaRepository;
        this.webhookConfigRepository = webhookConfigRepository;
        this.mailSender = mailSender;
        this.maxReintentos = maxReintentos;
        this.backoffSegundos = backoffSegundos;
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponse> listarPorUsuario(UUID usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaEnvioDesc(usuarioId)
                .stream().map(this::mapear).collect(Collectors.toList());
    }

    @Transactional
    public void procesarAlerta(UUID empresaId, String mensaje) {
        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
        if (empresa == null) return;

        for (UsuarioEmpresa miembro : usuarioEmpresaRepository.findByEmpresaId(empresaId)) {
            Notificacion n = Notificacion.builder()
                    .usuario(miembro.getUsuario()).empresa(empresa)
                    .tipo("ALERTA").mensaje(mensaje).estado("ENVIADO").build();
            notificacionRepository.save(n);
            enviarEmail(miembro.getUsuario(), empresa, mensaje, "ALERTA");
        }
    }

    @Transactional
    public void procesarAnomalia(UUID empresaId, String mensaje) {
        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
        if (empresa == null) return;

        for (UsuarioEmpresa miembro : usuarioEmpresaRepository.findByEmpresaId(empresaId)) {
            Notificacion n = Notificacion.builder()
                    .usuario(miembro.getUsuario()).empresa(empresa)
                    .tipo("ANOMALIA").mensaje(mensaje).estado("ENVIADO").build();
            notificacionRepository.save(n);
            enviarEmail(miembro.getUsuario(), empresa, mensaje, "ANOMALIA");
        }

        webhookConfigRepository.findByEmpresaId(empresaId).ifPresent(config -> {
            if (config.isActivo()) enviarWebhookConReintentos(config, empresa, mensaje);
        });
    }

    @Transactional
    public MensajeResponse configurarWebhook(UUID empresaId, ConfigurarWebhookRequest request) {
        WebhookConfig config = webhookConfigRepository.findByEmpresaId(empresaId)
                .orElse(WebhookConfig.builder()
                        .empresa(empresaRepository.findById(empresaId)
                                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada")))
                        .build());
        config.setUrlWebhook(request.getUrlWebhook());
        config.setActivo(true);
        webhookConfigRepository.save(config);
        return MensajeResponse.builder().mensaje("Webhook configurado correctamente").build();
    }

    @Transactional(readOnly = true)
    public WebhookConfig obtenerWebhook(UUID empresaId) {
        return webhookConfigRepository.findByEmpresaId(empresaId).orElse(null);
    }

    private void enviarEmail(Usuario usuario, Empresa empresa, String mensaje, String tipo) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(usuario.getEmail());
            mail.setSubject("FinCore - " + tipo + " - " + empresa.getNombre());
            mail.setText("Hola " + usuario.getNombre() + ",\n\n" +
                    "Se detecto una " + tipo.toLowerCase() + " en " + empresa.getNombre() + ":\n" +
                    mensaje + "\n\nEquipo FinCore");
            mailSender.send(mail);
        } catch (Exception e) { /* fallo silencioso */ }
    }

    private void enviarWebhookConReintentos(WebhookConfig webhook, Empresa empresa, String mensaje) {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        String payload = String.format("{\"empresa\":\"%s\",\"tipo\":\"ANOMALIA\",\"mensaje\":\"%s\"}",
                empresa.getNombre(), mensaje);

        for (int i = 1; i <= maxReintentos; i++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(webhook.getUrlWebhook()))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .timeout(Duration.ofSeconds(10)).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) return;
                if (i < maxReintentos) Thread.sleep(backoffSegundos * 1000L);
            } catch (Exception e) {
                if (i < maxReintentos) {
                    try { Thread.sleep(backoffSegundos * 1000L); }
                    catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }
                }
            }
        }
    }

    private NotificacionResponse mapear(Notificacion n) {
        return NotificacionResponse.builder()
                .id(n.getId().toString())
                .empresaNombre(n.getEmpresa() != null ? n.getEmpresa().getNombre() : "Sin empresa")
                .tipo(n.getTipo()).mensaje(n.getMensaje()).estado(n.getEstado())
                .fechaEnvio(n.getFechaEnvio()).build();
    }
}
