package com.fincore.platform.infrastructure.report.controller;

import com.fincore.platform.infrastructure.report.dto.ReporteResponse;
import com.fincore.platform.infrastructure.report.service.ReporteService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/reportes")
@Tag(name = "Reportes", description = "Generacion de reportes financieros en PDF")
@SecurityRequirement(name = "Bearer")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) { this.reporteService = reporteService; }

    @PostMapping("/{empresaId}")
    @Operation(summary = "Solicitar reporte manual")
    public ResponseEntity<ReporteResponse> generar(
            @PathVariable UUID empresaId,
            @RequestParam int mes, @RequestParam int anio,
            @RequestAttribute(value = "usuarioId") UUID usuarioId) {
        return new ResponseEntity<>(reporteService.generarManual(empresaId, usuarioId, mes, anio), HttpStatus.CREATED);
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Listar reportes de la empresa")
    public ResponseEntity<List<ReporteResponse>> listar(@PathVariable UUID empresaId) {
        return ResponseEntity.ok(reporteService.listarPorEmpresa(empresaId));
    }

    @GetMapping("/{reporteId}/descargar")
    @Operation(summary = "Descargar el PDF del reporte")
    public ResponseEntity<byte[]> descargar(@PathVariable UUID reporteId) {
        byte[] pdf = reporteService.descargar(reporteId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "reporte.pdf");
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
