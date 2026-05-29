package com.fincore.platform.infrastructure.auth.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "logs_api")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LogApi {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_key_id")
    private ApiKey apiKey;

    @Column(nullable = false, length = 255)
    private String endpoint;

    @Column(nullable = false, length = 10)
    private String metodo;

    @Column(name = "cuerpo_peticion", columnDefinition = "TEXT")
    private String cuerpoPeticion;

    @Column(name = "codigo_respuesta", nullable = false)
    private int codigoRespuesta;

    @Column(name = "direccion_ip", length = 45)
    private String direccionIp;

    @Column(name = "fecha_peticion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaPeticion = LocalDateTime.now();
}
