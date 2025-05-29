package com.vehicle.repair.repository;

import com.vehicle.repair.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByContactPhone(String contactPhone);
    Optional<User> findByContactEmail(String contactEmail);
    boolean existsByUsername(String username);
    boolean existsByContactPhone(String contactPhone);
    boolean existsByContactEmail(String contactEmail);
} 