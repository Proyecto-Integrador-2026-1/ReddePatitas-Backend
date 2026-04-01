package com.redpatitas.redPatitas.repository;

import com.redpatitas.redPatitas.entity.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {
	Optional<Ubicacion> findTopByReportIdOrderByIdUbicacionDesc(Long reportId);
	Optional<Ubicacion> findTopByReportOrderByIdUbicacionDesc(com.redpatitas.redPatitas.entity.Report report);
	@org.springframework.data.jpa.repository.Query("select u from Ubicacion u where u.report.id = :rid order by u.idUbicacion desc")
	Optional<Ubicacion> findLatestByReportId(@org.springframework.data.repository.query.Param("rid") Long reportId);
}
