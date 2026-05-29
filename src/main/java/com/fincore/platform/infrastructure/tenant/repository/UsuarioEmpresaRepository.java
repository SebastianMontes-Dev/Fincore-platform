package com.fincore.platform.infrastructure.tenant.repository;

import com.fincore.platform.infrastructure.auth.domain.RolEmpresa;
import com.fincore.platform.infrastructure.tenant.domain.UsuarioEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioEmpresaRepository extends JpaRepository<UsuarioEmpresa, UUID> {
    List<UsuarioEmpresa> findByUsuarioId(UUID usuarioId);
    Optional<UsuarioEmpresa> findByUsuarioIdAndEmpresaId(UUID usuarioId, UUID empresaId);
    boolean existsByUsuarioIdAndEmpresaIdAndRol(UUID usuarioId, UUID empresaId, RolEmpresa rol);
    List<UsuarioEmpresa> findByEmpresaId(UUID empresaId);
}
