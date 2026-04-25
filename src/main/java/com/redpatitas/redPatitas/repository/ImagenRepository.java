package com.redpatitas.redPatitas.repository;

import com.redpatitas.redPatitas.entity.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;
import java.util.Optional;

public interface ImagenRepository extends JpaRepository<Imagen, UUID> {
  Optional<Imagen> findTopByReportIdOrderByIdImagenDesc(UUID reportId);
// o con query nativa
@Query("select i from Imagen i where i.report.id = :rid order by i.idImagen desc")
Optional<Imagen> findLatestByReportId(@Param("rid") UUID reportId);
}
