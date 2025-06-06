package com.vehicle.repair.repository;

import com.vehicle.repair.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    
    List<Vehicle> findByUserUserId(Integer userId);
} 