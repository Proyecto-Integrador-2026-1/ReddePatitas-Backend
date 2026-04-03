package com.redpatitas.redPatitas.repository;

import com.redpatitas.redPatitas.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
	@org.springframework.data.jpa.repository.Query("select r from Report r left join fetch r.pet order by r.fechaCreacion desc")
	List<Report> findAllWithPet();
}
