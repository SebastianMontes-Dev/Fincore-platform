package com.fincore.platform.infrastructure.tenant.repository;

import com.fincore.platform.infrastructure.tenant.domain.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {
    Optional<Empresa> findByEmail(String email);
    boolean existsByEmail(String email);
}
