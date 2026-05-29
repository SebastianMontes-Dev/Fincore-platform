package com.fincore.platform.infrastructure.report.repository;

import com.fincore.platform.infrastructure.report.domain.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReporteRepository extends JpaRepository<Reporte, UUID> {
    List<Reporte> findByEmpresaIdOrderByFechaGeneracionDesc(UUID empresaId);
    Optional<Reporte> findByEmpresaIdAndMesAndAnio(UUID empresaId, int mes, int anio);
}
