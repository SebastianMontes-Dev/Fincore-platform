package com.fincore.platform.infrastructure.report.scheduler;

import com.fincore.platform.infrastructure.report.service.ReporteService;
import org.quartz.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReporteJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(ReporteJob.class);

    @Autowired
    private ReporteService reporteService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("Ejecutando reportes programados...");
        try {
            reporteService.generarProgramados();
            logger.info("Reportes generados correctamente");
        } catch (Exception e) {
            logger.error("Error en reportes programados: {}", e.getMessage());
            throw new JobExecutionException(e);
        }
    }
}
