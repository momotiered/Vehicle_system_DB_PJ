package com.example.backend_dbpj.repository;

import com.example.backend_dbpj.entity.RepairPersonnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairPersonnelRepository extends JpaRepository<RepairPersonnel, Integer> {
    Optional<RepairPersonnel> findByUsername(String username);
    List<RepairPersonnel> findByIsActive(boolean isActive);
    
    // 监控查询方法
    Long countByIsActive(boolean isActive);
} 