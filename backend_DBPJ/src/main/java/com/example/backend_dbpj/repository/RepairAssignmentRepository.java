package com.example.backend_dbpj.repository;

import com.example.backend_dbpj.entity.RepairAssignment;
import com.example.backend_dbpj.entity.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepairAssignmentRepository extends JpaRepository<RepairAssignment, Integer> {
    List<RepairAssignment> findByRepairPersonnel_PersonnelIdAndStatus(int personnelId, AssignmentStatus status);
}