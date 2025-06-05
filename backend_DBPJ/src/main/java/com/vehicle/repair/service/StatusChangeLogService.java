package com.vehicle.repair.service;

import com.vehicle.repair.dto.StatusChangeDTO;
import com.vehicle.repair.entity.RepairOrder;
import com.vehicle.repair.entity.RepairOrderStatus;
import com.vehicle.repair.entity.StatusChangeLog;
import com.vehicle.repair.entity.User;
import com.vehicle.repair.repository.RepairOrderRepository;
import com.vehicle.repair.repository.StatusChangeLogRepository;
import com.vehicle.repair.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatusChangeLogService {

    @Autowired
    private StatusChangeLogRepository statusChangeLogRepository;
    
    @Autowired
    private RepairOrderRepository repairOrderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 记录工单状态变更
     */
    @Transactional
    public StatusChangeLog logStatusChange(Integer orderId, RepairOrderStatus previousStatus, 
                                          RepairOrderStatus newStatus, Integer changedByUserId, 
                                          String changeReason, String notes) {
        // 获取维修工单
        RepairOrder repairOrder = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("维修工单不存在"));
        
        // 获取操作用户
        User changedByUser = null;
        if (changedByUserId != null) {
            changedByUser = userRepository.findById(changedByUserId)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
        }
        
        // 创建日志记录
        StatusChangeLog log = new StatusChangeLog();
        log.setRepairOrder(repairOrder);
        log.setPreviousStatus(previousStatus);
        log.setNewStatus(newStatus);
        log.setChangeReason(changeReason);
        log.setChangedByUser(changedByUser);
        log.setNotes(notes);
        
        return statusChangeLogRepository.save(log);
    }
    
    /**
     * 获取工单的状态变更历史
     */
    public List<StatusChangeDTO> getStatusChangeHistory(Integer orderId) {
        List<StatusChangeLog> logs = statusChangeLogRepository.findByRepairOrder_OrderIdOrderByChangeTimeDesc(orderId);
        return logs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    /**
     * 获取用户操作的状态变更历史
     */
    public List<StatusChangeDTO> getUserChangeHistory(Integer userId) {
        List<StatusChangeLog> logs = statusChangeLogRepository.findByChangedByUser_UserIdOrderByChangeTimeDesc(userId);
        return logs.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    /**
     * 将实体转换为DTO
     */
    private StatusChangeDTO convertToDTO(StatusChangeLog log) {
        StatusChangeDTO dto = new StatusChangeDTO();
        dto.setLogId(log.getLogId());
        dto.setOrderId(log.getRepairOrder().getOrderId());
        dto.setPreviousStatus(log.getPreviousStatus());
        dto.setNewStatus(log.getNewStatus());
        dto.setPreviousStatusText(getStatusText(log.getPreviousStatus()));
        dto.setNewStatusText(getStatusText(log.getNewStatus()));
        dto.setChangeReason(log.getChangeReason());
        
        if (log.getChangedByUser() != null) {
            dto.setChangedByUserId(log.getChangedByUser().getUserId());
            dto.setChangedByUserName(log.getChangedByUser().getFullName());
        }
        
        dto.setChangeTime(log.getChangeTime());
        dto.setNotes(log.getNotes());
        
        return dto;
    }
    
    /**
     * 获取状态的中文描述
     */
    private String getStatusText(RepairOrderStatus status) {
        switch (status) {
            case PENDING_ASSIGNMENT: return "待分配";
            case ASSIGNED: return "已分配";
            case IN_PROGRESS: return "维修中";
            case AWAITING_PARTS: return "等待配件";
            case COMPLETED: return "已完成";
            case CANCELLED_BY_USER: return "用户取消";
            case CANNOT_REPAIR: return "无法维修";
            case PENDING_USER_CONFIRMATION: return "待用户确认";
            default: return status.name();
        }
    }
} 