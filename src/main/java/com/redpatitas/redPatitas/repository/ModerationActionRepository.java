package com.redpatitas.redPatitas.repository;

import com.redpatitas.redPatitas.entity.ModerationAction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ModerationActionRepository extends JpaRepository<ModerationAction, UUID> {

}
