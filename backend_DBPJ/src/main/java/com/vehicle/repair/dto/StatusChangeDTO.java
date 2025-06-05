package com.vehicle.repair.dto;

import com.vehicle.repair.entity.RepairOrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StatusChangeDTO {
    private Integer logId;
    private Integer orderId;
    private RepairOrderStatus previousStatus;
    private RepairOrderStatus newStatus;
    private String previousStatusText;
    private String newStatusText;
    private String changeReason;
    private Integer changedByUserId;
    private String changedByUserName;
    private LocalDateTime changeTime;
    private String notes;
} 