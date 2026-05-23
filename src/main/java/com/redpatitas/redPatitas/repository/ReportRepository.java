package com.redpatitas.redPatitas.repository;

import com.redpatitas.redPatitas.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, UUID> {
    @Query("select r from Report r left join fetch r.pet order by r.fechaCreacion desc")
    List<Report> findAllWithPet();

    @Query("select r from Report r left join fetch r.pet where r.estado <> 'RESUELTO' and r.oculto = false and r.eliminado = false order by r.fechaCreacion desc")
    List<Report> findAllActiveWithPet();

    @Query("select r from Report r left join fetch r.pet where r.estado = 'RESUELTO' order by r.fechaCreacion desc")
    List<Report> findAllResolvedWithPet();

    long countByEstadoIn(java.util.Collection<String> estados);

    long countByEstado(String estado);

    @Query("select r from Report r left join fetch r.pet where r.id = :id")
    java.util.Optional<Report> findByIdWithPet(java.util.UUID id);

    long countByEstadoNot(String estado);
}
