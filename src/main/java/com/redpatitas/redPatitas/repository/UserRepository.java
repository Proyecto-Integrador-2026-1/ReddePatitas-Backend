package com.redpatitas.redPatitas.repository;

import com.redpatitas.redPatitas.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
