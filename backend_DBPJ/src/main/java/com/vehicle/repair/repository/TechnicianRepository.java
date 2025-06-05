package com.vehicle.repair.repository;

import com.vehicle.repair.entity.SkillType;
import com.vehicle.repair.entity.Technician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicianRepository extends JpaRepository<Technician, Integer> {
    
    Optional<Technician> findByUser_UserId(Integer userId);
    
    List<Technician> findBySkillType(SkillType skillType);
    
    List<Technician> findByIsAvailableTrue();
    
    @Query("SELECT t FROM Technician t WHERE t.isAvailable = true AND t.currentWorkOrders < t.maxWorkOrdersPerDay")
    List<Technician> findAvailableTechnicians();
    
    @Query("SELECT t FROM Technician t WHERE t.isAvailable = true AND t.currentWorkOrders < t.maxWorkOrdersPerDay AND t.skillType = ?1")
    List<Technician> findAvailableTechniciansBySkill(SkillType skillType);
} 