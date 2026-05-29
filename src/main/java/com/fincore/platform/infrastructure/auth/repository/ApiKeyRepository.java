package com.fincore.platform.infrastructure.auth.repository;

import com.fincore.platform.infrastructure.auth.domain.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    Optional<ApiKey> findByClaveHash(String claveHash);
    List<ApiKey> findByEmpresaIdAndActivaTrue(UUID empresaId);
}
