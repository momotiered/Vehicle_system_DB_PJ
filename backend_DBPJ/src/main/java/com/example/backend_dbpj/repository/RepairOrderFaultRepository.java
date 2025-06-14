package com.example.backend_dbpj.repository;

import com.example.backend_dbpj.entity.RepairOrderFault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepairOrderFaultRepository extends JpaRepository<RepairOrderFault, Integer> {
} 