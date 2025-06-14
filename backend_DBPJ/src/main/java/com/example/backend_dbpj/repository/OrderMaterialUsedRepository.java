package com.example.backend_dbpj.repository;

import com.example.backend_dbpj.entity.OrderMaterialUsed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderMaterialUsedRepository extends JpaRepository<OrderMaterialUsed, Integer> {
} 