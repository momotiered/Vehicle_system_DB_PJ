package com.example.backend_dbpj.repository;

import com.example.backend_dbpj.entity.RepairOrder;
import com.example.backend_dbpj.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepairOrderRepository extends JpaRepository<RepairOrder, Integer> {
    List<RepairOrder> findByUser_UserId(Integer userId);
    List<RepairOrder> findByStatus(OrderStatus status);
} 