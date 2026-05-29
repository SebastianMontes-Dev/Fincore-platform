package com.fincore.platform.infrastructure.report.domain;

import com.fincore.platform.infrastructure.tenant.domain.Empresa;
import com.fincore.platform.infrastructure.auth.domain.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reportes")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Reporte {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitado_por")
    private Usuario solicitadoPor;

    @Column(nullable = false)
    private int mes;

    @Column(name = "anio", nullable = false)
    private int anio;

    @Column(name = "ruta_archivo", length = 500)
    private String rutaArchivo;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(name = "fecha_generacion", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaGeneracion = LocalDateTime.now();
}
