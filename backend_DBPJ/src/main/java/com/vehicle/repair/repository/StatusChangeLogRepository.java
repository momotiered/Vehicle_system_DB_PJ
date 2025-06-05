package com.vehicle.repair.repository;

import com.vehicle.repair.entity.StatusChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusChangeLogRepository extends JpaRepository<StatusChangeLog, Integer> {
    List<StatusChangeLog> findByRepairOrder_OrderId(Integer orderId);
    
    List<StatusChangeLog> findByRepairOrder_OrderIdOrderByChangeTimeDesc(Integer orderId);
    
    List<StatusChangeLog> findByChangedByUser_UserIdOrderByChangeTimeDesc(Integer userId);
} 