package com.fincore.platform.infrastructure.auth.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "api_keys")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ApiKey {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private com.fincore.platform.infrastructure.tenant.domain.Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "clave_hash", nullable = false, unique = true, length = 255)
    private String claveHash;

    @Column(nullable = false)
    private boolean activa;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_revocacion")
    private LocalDateTime fechaRevocacion;
}
