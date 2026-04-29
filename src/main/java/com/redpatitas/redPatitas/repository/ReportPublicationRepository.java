package com.redpatitas.redPatitas.repository;

import com.redpatitas.redPatitas.entity.ReportPublication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReportPublicationRepository extends JpaRepository<ReportPublication, UUID> {

    @Query("select count(rp) from ReportPublication rp where rp.report.id = :reportId")
    long countByReportId(@Param("reportId") UUID reportId);

    @Query("select rp from ReportPublication rp where rp.report.id = :reportId and rp.userId = :userId")
    Optional<ReportPublication> findByReportIdAndUserId(@Param("reportId") UUID reportId, @Param("userId") UUID userId);
}
