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
        repairOrder.setStatus(RepairOrderStatus.PENDING_ASSIGNMENT);
        repairOrder.setReportDate(LocalDateTime.now());
        return repairOrderRepository.save(repairOrder);
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
    public RepairOrder updateRepairOrderStatus(Integer orderId, RepairOrderStatus newStatus) {
        RepairOrder repairOrder = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("维修工单不存在"));

        repairOrder.setStatus(newStatus);

        // 如果状态为已完成，设置完成时间
        if (newStatus == RepairOrderStatus.COMPLETED) {
            repairOrder.setCompletionDate(LocalDateTime.now());
        }

        return repairOrderRepository.save(repairOrder);
    }

    @Transactional
    public RepairOrder updateRepairOrder(RepairOrder repairOrder) {
        RepairOrder existingOrder = repairOrderRepository.findById(repairOrder.getOrderId())
                .orElseThrow(() -> new RuntimeException("维修工单不存在"));

        // 更新工单信息
        if (repairOrder.getDescriptionOfIssue() != null) {
            existingOrder.setDescriptionOfIssue(repairOrder.getDescriptionOfIssue());
        }
        if (repairOrder.getEstimatedCompletionDate() != null) {
            existingOrder.setEstimatedCompletionDate(repairOrder.getEstimatedCompletionDate());
        }
        if (repairOrder.getNotes() != null) {
            existingOrder.setNotes(repairOrder.getNotes());
        }

        return repairOrderRepository.save(existingOrder);
    }

    @Transactional
    public void deleteRepairOrder(Integer orderId) {
        repairOrderRepository.deleteById(orderId);
    }
} 