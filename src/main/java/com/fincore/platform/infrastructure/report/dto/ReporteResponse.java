package com.fincore.platform.infrastructure.report.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReporteResponse {
    private String id;
    private String empresaId;
    private String nombreEmpresa;
    private int mes;
    private int anio;
    private String tipo;
    private BigDecimal totalIngresos;
    private BigDecimal totalGastos;
    private BigDecimal balance;
    private List<Map<String, Object>> topCategorias;
    private LocalDateTime fechaGeneracion;
    private String rutaDescarga;
}
