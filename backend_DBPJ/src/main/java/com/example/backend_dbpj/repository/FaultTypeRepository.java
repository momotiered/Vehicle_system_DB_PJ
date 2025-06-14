package com.example.backend_dbpj.repository;

import com.example.backend_dbpj.entity.FaultType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface FaultTypeRepository extends JpaRepository<FaultType, Integer> {
} 