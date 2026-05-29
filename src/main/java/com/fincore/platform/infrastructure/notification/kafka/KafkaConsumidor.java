package com.fincore.platform.infrastructure.notification.kafka;

import com.fincore.platform.infrastructure.notification.service.NotificacionService;
import org.slf4j.*;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class KafkaConsumidor {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumidor.class);
    private final NotificacionService notificacionService;

    public KafkaConsumidor(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @KafkaListener(topics = "fincore.eventos.alerta", groupId = "fincore-platform-group")
    public void consumirAlerta(Map<String, Object> evento) {
        try {
            UUID empresaId = UUID.fromString((String) evento.get("empresa_id"));
            String mensaje = (String) evento.getOrDefault("mensaje", "Alerta del sistema");
            logger.info("Alerta recibida para empresa: {}", empresaId);
            notificacionService.procesarAlerta(empresaId, mensaje);
        } catch (Exception e) {
            logger.error("Error procesando alerta: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "fincore.eventos.anomalia", groupId = "fincore-platform-group")
    public void consumirAnomalia(Map<String, Object> evento) {
        try {
            UUID empresaId = UUID.fromString((String) evento.get("empresa_id"));
            String mensaje = (String) evento.getOrDefault("mensaje", "Anomalia detectada");
            logger.info("Anomalia recibida para empresa: {}", empresaId);
            notificacionService.procesarAnomalia(empresaId, mensaje);
        } catch (Exception e) {
            logger.error("Error procesando anomalia: {}", e.getMessage());
        }
    }
}
