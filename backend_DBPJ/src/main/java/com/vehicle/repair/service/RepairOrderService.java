package com.vehicle.repair.service;

import com.vehicle.repair.entity.RepairOrder;
import com.vehicle.repair.entity.RepairOrderStatus;
import com.vehicle.repair.repository.RepairOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RepairOrderService {
    @Autowired
    private RepairOrderRepository repairOrderRepository;

    @Transactional
    public RepairOrder createRepairOrder(RepairOrder repairOrder) {
        // 设置默认状态和报修时间
        if (repairOrder.getStatus() == null) {
            repairOrder.setStatus(RepairOrderStatus.PENDING_ASSIGNMENT);
        }
        if (repairOrder.getReportDate() == null) {
            repairOrder.setReportDate(LocalDateTime.now());
        }
        
        // 保存工单
        RepairOrder savedOrder = repairOrderRepository.save(repairOrder);
        return savedOrder;
    }

    public Optional<RepairOrder> getRepairOrderById(Integer orderId) {
        return repairOrderRepository.findById(orderId);
    }

    public List<RepairOrder> getRepairOrdersByUserId(Integer userId) {
        return repairOrderRepository.findByUser_UserId(userId);
    }

    public List<RepairOrder> getRepairOrdersByVehicleId(Integer vehicleId) {
        return repairOrderRepository.findByVehicle_VehicleId(vehicleId);
    }
    
    public List<RepairOrder> getRepairOrdersByStatus(RepairOrderStatus status) {
        return repairOrderRepository.findByStatus(status);
    }

    public List<RepairOrder> getRepairOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return repairOrderRepository.findByReportDateBetween(startDate, endDate);
    }

    @Transactional
    public RepairOrder updateRepairOrderStatus(Integer orderId, RepairOrderStatus newStatus, Integer operatorId, String reason) {
        RepairOrder repairOrder = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("维修工单不存在"));

        // 更新状态
        repairOrder.setStatus(newStatus);

        // 如果状态为已完成，设置完成时间
        if (newStatus == RepairOrderStatus.COMPLETED) {
            repairOrder.setCompletionDate(LocalDateTime.now());
        }
        
        // 如果状态为已分配或维修中，设置开始时间（如果尚未设置）
        if ((newStatus == RepairOrderStatus.ASSIGNED || newStatus == RepairOrderStatus.IN_PROGRESS) 
                && repairOrder.getStartDate() == null) {
            repairOrder.setStartDate(LocalDateTime.now());
        }

        // 保存工单
        return repairOrderRepository.save(repairOrder);
    }
    
    // 兼容旧版API调用
    @Transactional
    public RepairOrder updateRepairOrderStatus(Integer orderId, RepairOrderStatus newStatus) {
        return updateRepairOrderStatus(orderId, newStatus, null, "系统自动更新状态");
    }

    @Transactional
    public void deleteRepairOrder(Integer orderId) {
        RepairOrder repairOrder = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("维修工单不存在"));
        repairOrderRepository.delete(repairOrder);
    }
} 