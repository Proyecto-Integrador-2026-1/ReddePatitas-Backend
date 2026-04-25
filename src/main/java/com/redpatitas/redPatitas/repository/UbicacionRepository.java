package com.redpatitas.redPatitas.repository;

import com.redpatitas.redPatitas.entity.Report;
import com.redpatitas.redPatitas.entity.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UbicacionRepository extends JpaRepository<Ubicacion, UUID> {
    Optional<Ubicacion> findTopByReportIdOrderByIdUbicacionDesc(UUID reportId);
    Optional<Ubicacion> findTopByReportOrderByIdUbicacionDesc(Report report);
    Optional<Ubicacion> findByReportId(UUID reportId);
}
