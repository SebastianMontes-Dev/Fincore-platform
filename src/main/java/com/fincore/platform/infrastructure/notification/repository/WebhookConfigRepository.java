package com.fincore.platform.infrastructure.notification.repository;

import com.fincore.platform.infrastructure.notification.domain.WebhookConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface WebhookConfigRepository extends JpaRepository<WebhookConfig, UUID> {
    Optional<WebhookConfig> findByEmpresaId(UUID empresaId);
}
