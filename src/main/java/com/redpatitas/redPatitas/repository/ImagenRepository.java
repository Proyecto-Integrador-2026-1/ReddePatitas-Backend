package com.redpatitas.redPatitas.repository;

import com.redpatitas.redPatitas.entity.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ImagenRepository extends JpaRepository<Imagen, Long> {
	Optional<Imagen> findTopByReportIdOrderByIdImagenDesc(Long reportId);
	Optional<Imagen> findTopByReportOrderByIdImagenDesc(com.redpatitas.redPatitas.entity.Report report);
	@org.springframework.data.jpa.repository.Query("select i from Imagen i where i.report.id = :rid order by i.idImagen desc")
	Optional<Imagen> findLatestByReportId(@org.springframework.data.repository.query.Param("rid") Long reportId);
}
