package com.vehicle.repair.dto;

import com.vehicle.repair.entity.RepairOrderStatus;
import com.vehicle.repair.entity.UrgencyLevel;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RepairOrderRequestDTO {
    private Integer vehicleId;
    private Integer userId;
    private String descriptionOfIssue;
    private UrgencyLevel urgencyLevel = UrgencyLevel.MEDIUM;
    private LocalDate expectedServiceDate;
    private RepairOrderStatus status = RepairOrderStatus.PENDING_ASSIGNMENT;
    private String notes;
} 