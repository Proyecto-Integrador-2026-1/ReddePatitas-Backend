package com.redpatitas.redPatitas.repository;

import com.redpatitas.redPatitas.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, UUID> {
    @Query("select r from Report r left join fetch r.pet order by r.fechaCreacion desc")
    List<Report> findAllWithPet();
}
