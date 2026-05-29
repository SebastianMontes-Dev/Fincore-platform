package com.fincore.platform.infrastructure.notification.repository;

import com.fincore.platform.infrastructure.notification.domain.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {
    List<Notificacion> findByUsuarioIdOrderByFechaEnvioDesc(UUID usuarioId);
}

