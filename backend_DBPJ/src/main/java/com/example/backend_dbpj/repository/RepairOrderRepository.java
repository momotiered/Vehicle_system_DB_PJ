package com.example.backend_dbpj.repository;

import com.example.backend_dbpj.entity.RepairOrder;
import com.example.backend_dbpj.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.sql.Date;

@Repository
public interface RepairOrderRepository extends JpaRepository<RepairOrder, Integer> {
    List<RepairOrder> findByUser_UserId(Integer userId);
    List<RepairOrder> findByStatus(OrderStatus status);
    
    // 监控查询方法
    Long countByStatus(OrderStatus status);
    
    @Query("SELECT COUNT(r) FROM RepairOrder r WHERE DATE(r.reportDate) >= :startDate AND DATE(r.reportDate) < :endDate")
    Long countByReportDateBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Query("SELECT r FROM RepairOrder r WHERE r.status = :status AND r.completionDate >= :startDate AND r.completionDate < :endDate")
    List<RepairOrder> findCompletedOrdersInDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("status") OrderStatus status);
} 