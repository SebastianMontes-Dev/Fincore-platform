package com.fincore.platform.infrastructure.auth.repository;

import com.fincore.platform.infrastructure.auth.domain.LogApi;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface LogApiRepository extends JpaRepository<LogApi, UUID> {
}
