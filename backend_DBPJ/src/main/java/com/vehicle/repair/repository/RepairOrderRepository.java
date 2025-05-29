package com.vehicle.repair.repository;

import com.vehicle.repair.entity.RepairOrder;
import com.vehicle.repair.entity.RepairOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RepairOrderRepository extends JpaRepository<RepairOrder, Integer> {
    List<RepairOrder> findByUser_UserId(Integer userId);
    List<RepairOrder> findByVehicle_VehicleId(Integer vehicleId);
    List<RepairOrder> findByStatus(RepairOrderStatus status);
    List<RepairOrder> findByReportDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<RepairOrder> findByUser_UserIdAndStatus(Integer userId, RepairOrderStatus status);
    List<RepairOrder> findByVehicle_VehicleIdAndStatus(Integer vehicleId, RepairOrderStatus status);
} 