package com.redpatitas.redPatitas.repository;

import com.redpatitas.redPatitas.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PetRepository extends JpaRepository<Pet, UUID> {
    Optional<Pet> findByUserIdAndNombreAndTipo(UUID userId, String nombre, String tipo);
}