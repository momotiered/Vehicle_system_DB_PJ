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
    
    @Autowired
    private StatusChangeLogService statusChangeLogService;
    
    @Autowired
    private NotificationService notificationService;

    @Transactional
    public RepairOrder createRepairOrder(RepairOrder repairOrder) {
        // 设置默认状态和报修时间
        repairOrder.setStatus(RepairOrderStatus.PENDING_ASSIGNMENT);
        repairOrder.setReportDate(LocalDateTime.now());
        
        // 保存工单
        RepairOrder savedOrder = repairOrderRepository.save(repairOrder);
        
        // 记录状态变更日志（新建工单，无previous状态）
        statusChangeLogService.logStatusChange(
                savedOrder.getOrderId(),
                null,
                RepairOrderStatus.PENDING_ASSIGNMENT,
                repairOrder.getUser().getUserId(),
                "用户提交报修申请",
                null
        );
        
        // 创建新工单通知
        notificationService.createNewOrderNotification(
                repairOrder.getUser().getUserId(),
                savedOrder.getOrderId()
        );
        
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

        // 记录原状态
        RepairOrderStatus previousStatus = repairOrder.getStatus();
        
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
        RepairOrder updatedOrder = repairOrderRepository.save(repairOrder);
        
        // 记录状态变更日志
        statusChangeLogService.logStatusChange(
                orderId,
                previousStatus,
                newStatus,
                operatorId,
                reason,
                null
        );
        
        // 创建状态变更通知
        Integer userId = repairOrder.getUser().getUserId();
        notificationService.createStatusChangeNotification(
                userId,
                orderId,
                previousStatus,
                newStatus
        );
        
        // 如果状态变为已完成，创建完成通知
        if (newStatus == RepairOrderStatus.COMPLETED) {
            notificationService.createCompletionNotification(
                    userId,
                    orderId
            );
        }

        return updatedOrder;
    }
    
    // 兼容旧版API调用
    @Transactional
    public RepairOrder updateRepairOrderStatus(Integer orderId, RepairOrderStatus newStatus) {
        return updateRepairOrderStatus(orderId, newStatus, null, "系统自动更新状态");
    }

    @Transactional
    public RepairOrder updateRepairOrder(RepairOrder repairOrder, Integer operatorId) {
        RepairOrder existingOrder = repairOrderRepository.findById(repairOrder.getOrderId())
                .orElseThrow(() -> new RuntimeException("维修工单不存在"));

        // 检查状态是否变更
        boolean statusChanged = repairOrder.getStatus() != null && 
                !existingOrder.getStatus().equals(repairOrder.getStatus());
        
        RepairOrderStatus previousStatus = existingOrder.getStatus();

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
        if (repairOrder.getUrgencyLevel() != null) {
            existingOrder.setUrgencyLevel(repairOrder.getUrgencyLevel());
        }
        if (repairOrder.getExpectedServiceDate() != null) {
            existingOrder.setExpectedServiceDate(repairOrder.getExpectedServiceDate());
        }
        if (repairOrder.getStatus() != null) {
            existingOrder.setStatus(repairOrder.getStatus());
            
            // 如果状态变为已完成，设置完成时间
            if (repairOrder.getStatus() == RepairOrderStatus.COMPLETED) {
                existingOrder.setCompletionDate(LocalDateTime.now());
            }
            
            // 如果状态变为已分配或维修中，设置开始时间（如果尚未设置）
            if ((repairOrder.getStatus() == RepairOrderStatus.ASSIGNED || 
                 repairOrder.getStatus() == RepairOrderStatus.IN_PROGRESS) && 
                existingOrder.getStartDate() == null) {
                existingOrder.setStartDate(LocalDateTime.now());
            }
        }
        
        // 保存更新后的工单
        RepairOrder updatedOrder = repairOrderRepository.save(existingOrder);
        
        // 如果状态发生变化，记录日志和发送通知
        if (statusChanged) {
            // 记录状态变更日志
            statusChangeLogService.logStatusChange(
                    updatedOrder.getOrderId(),
                    previousStatus,
                    updatedOrder.getStatus(),
                    operatorId,
                    "工单信息更新",
                    repairOrder.getNotes()
            );
            
            // 创建状态变更通知
            Integer userId = existingOrder.getUser().getUserId();
            notificationService.createStatusChangeNotification(
                    userId,
                    updatedOrder.getOrderId(),
                    previousStatus,
                    updatedOrder.getStatus()
            );
            
            // 如果状态变为已完成，创建完成通知
            if (updatedOrder.getStatus() == RepairOrderStatus.COMPLETED) {
                notificationService.createCompletionNotification(
                        userId,
                        updatedOrder.getOrderId()
                );
            }
        }

        return updatedOrder;
    }
    
    // 兼容旧版API调用
    @Transactional
    public RepairOrder updateRepairOrder(RepairOrder repairOrder) {
        return updateRepairOrder(repairOrder, null);
    }

    @Transactional
    public void deleteRepairOrder(Integer orderId) {
        repairOrderRepository.deleteById(orderId);
    }
    
    /**
     * 分配维修工单给维修人员
     */
    @Transactional
    public RepairOrder assignRepairOrder(Integer orderId, Integer technicianId, Integer operatorId) {
        RepairOrder repairOrder = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("维修工单不存在"));
        
        // TODO: 设置工单与维修人员的关联
        
        // 更新工单状态
        RepairOrderStatus previousStatus = repairOrder.getStatus();
        repairOrder.setStatus(RepairOrderStatus.ASSIGNED);
        
        // 如果没有设置开始时间，则设置
        if (repairOrder.getStartDate() == null) {
            repairOrder.setStartDate(LocalDateTime.now());
        }
        
        RepairOrder updatedOrder = repairOrderRepository.save(repairOrder);
        
        // 记录状态变更
        statusChangeLogService.logStatusChange(
                orderId,
                previousStatus,
                RepairOrderStatus.ASSIGNED,
                operatorId,
                "分配维修工单给技术人员",
                "技术人员ID: " + technicianId
        );
        
        // 创建状态变更通知
        notificationService.createStatusChangeNotification(
                repairOrder.getUser().getUserId(),
                orderId,
                previousStatus,
                RepairOrderStatus.ASSIGNED
        );
        
        return updatedOrder;
    }
    
    /**
     * 更新工单进度
     */
    @Transactional
    public RepairOrder updateProgress(Integer orderId, String progressNotes, Integer operatorId) {
        RepairOrder repairOrder = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("维修工单不存在"));
        
        // 记录进度信息
        String currentNotes = repairOrder.getNotes();
        String updatedNotes = currentNotes == null ? 
                progressNotes : currentNotes + "\n" + LocalDateTime.now() + ": " + progressNotes;
        
        repairOrder.setNotes(updatedNotes);
        
        return repairOrderRepository.save(repairOrder);
    }
    
    /**
     * 完成维修工单
     */
    @Transactional
    public RepairOrder completeRepairOrder(Integer orderId, String completionNotes, Integer operatorId) {
        RepairOrder repairOrder = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("维修工单不存在"));
        
        // 记录原状态
        RepairOrderStatus previousStatus = repairOrder.getStatus();
        
        // 更新状态和完成时间
        repairOrder.setStatus(RepairOrderStatus.COMPLETED);
        repairOrder.setCompletionDate(LocalDateTime.now());
        
        // 更新备注
        String currentNotes = repairOrder.getNotes();
        String updatedNotes = currentNotes == null ? 
                completionNotes : currentNotes + "\n" + LocalDateTime.now() + " [完成]: " + completionNotes;
        
        repairOrder.setNotes(updatedNotes);
        
        RepairOrder updatedOrder = repairOrderRepository.save(repairOrder);
        
        // 记录状态变更
        statusChangeLogService.logStatusChange(
                orderId,
                previousStatus,
                RepairOrderStatus.COMPLETED,
                operatorId,
                "完成维修工单",
                completionNotes
        );
        
        // 创建状态变更通知
        Integer userId = repairOrder.getUser().getUserId();
        notificationService.createStatusChangeNotification(
                userId,
                orderId,
                previousStatus,
                RepairOrderStatus.COMPLETED
        );
        
        // 创建完成通知
        notificationService.createCompletionNotification(
                userId,
                orderId
        );
        
        return updatedOrder;
    }
} 