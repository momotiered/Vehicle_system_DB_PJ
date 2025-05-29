package com.vehicle.repair.entity;

public enum RepairOrderStatus {
    PENDING_ASSIGNMENT,
    ASSIGNED,
    IN_PROGRESS,
    AWAITING_PARTS,
    COMPLETED,
    CANCELLED_BY_USER,
    CANNOT_REPAIR,
    PENDING_USER_CONFIRMATION
} 