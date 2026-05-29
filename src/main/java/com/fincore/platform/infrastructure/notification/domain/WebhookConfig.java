package com.fincore.platform.infrastructure.notification.domain;

import com.fincore.platform.infrastructure.tenant.domain.Empresa;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "configuracion_webhook")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WebhookConfig {

    @Id
    @UuidGenerator
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false, unique = true)
    private Empresa empresa;

    @Column(name = "url_webhook", nullable = false, length = 500)
    private String urlWebhook;

    @Column(nullable = false)
    private boolean activo;

    @Column(name = "fecha_actualizacion")
    @Builder.Default
    private LocalDateTime fechaActualizacion = LocalDateTime.now();

    @PreUpdate
    void onUpdate() { fechaActualizacion = LocalDateTime.now(); }
}
