package com.redpatitas.redPatitas.repository;

import com.redpatitas.redPatitas.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {
}
