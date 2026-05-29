package com.fincore.platform.infrastructure.tenant.domain;

import com.fincore.platform.infrastructure.auth.domain.RolEmpresa;
import com.fincore.platform.infrastructure.auth.domain.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios_empresas",
       uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "empresa_id"}))
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UsuarioEmpresa {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RolEmpresa rol;

    @Column(name = "fecha_union", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaUnion = LocalDateTime.now();
}
