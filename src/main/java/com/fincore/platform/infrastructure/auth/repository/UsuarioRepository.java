package com.fincore.platform.infrastructure.auth.repository;

import com.fincore.platform.infrastructure.auth.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByTokenVerificacion(String token);
    boolean existsByEmail(String email);
}
