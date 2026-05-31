package com.redpatitas.redPatitas.service.interfaces;

/**
 * Servicio para tareas programadas de limpieza del sistema
 */
public interface CleanupSchedulerService {

    /**
     * Elimina reportes antiguos (más de 14 días)
     * Se ejecuta automáticamente según la programación definida
     */
    void cleanupOldReports();

    /**
     * Ejecuta limpieza al iniciar la aplicación (opcional)
     */
    void cleanupOnStartup();
}