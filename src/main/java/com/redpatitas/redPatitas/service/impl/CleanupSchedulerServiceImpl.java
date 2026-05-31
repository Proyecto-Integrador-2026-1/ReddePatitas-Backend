package com.redpatitas.redPatitas.service.impl;

import com.redpatitas.redPatitas.service.interfaces.CleanupSchedulerService;
import com.redpatitas.redPatitas.service.interfaces.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanupSchedulerServiceImpl implements CleanupSchedulerService {

    private final ReportService reportService;

    /**
     * Elimina reportes antiguos todos los días a las 3:00 AM
     * 
     * Cron expression: segundos minutos horas día mes año día_semana
     * "0 0 3 * * ?" = todos los días a las 3:00 AM
     */
    @Override
    @Scheduled(cron = "0 0 3 * * ?", zone = "America/Bogota")
    public void cleanupOldReports() {
        log.info("🔄 Iniciando limpieza programada de reportes antiguos...");
        try {
            int deleted = reportService.deleteReportsOlderThan14Days();
            if (deleted > 0) {
                log.info("✅ Limpieza completada. {} reportes eliminados.", deleted);
            } else {
                log.info("✅ Limpieza completada. No se encontraron reportes para eliminar.");
            }
        } catch (Exception e) {
            log.error("❌ Error durante la limpieza de reportes antiguos: {}", e.getMessage(), e);
        }
    }

    /**
     * Ejecuta limpieza al iniciar la aplicación (descomentar para activar)
     * Se ejecuta 60 segundos después de iniciar y luego nunca más
     */
    @Override
    // @Scheduled(initialDelay = 60000, fixedDelay = Long.MAX_VALUE)
    public void cleanupOnStartup() {
        log.info("🔄 Ejecutando limpieza inicial de reportes antiguos...");
        cleanupOldReports();
    }
}