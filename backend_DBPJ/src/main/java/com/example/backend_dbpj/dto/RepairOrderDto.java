package com.example.backend_dbpj.dto;

import com.example.backend_dbpj.entity.RepairOrder;
import com.example.backend_dbpj.entity.enums.OrderStatus;

import java.sql.Date;
import java.sql.Timestamp;

public class RepairOrderDto {
    private int orderId;
    private int vehicleId;
    private int userId;
    private String descriptionOfIssue;
    private Timestamp reportDate;
    private OrderStatus status;

    public RepairOrderDto(RepairOrder order) {
        this.orderId = order.getOrderId();
        this.vehicleId = order.getVehicle().getVehicleId();
        this.userId = order.getUser().getUserId();
        this.descriptionOfIssue = order.getDescriptionOfIssue();
        this.reportDate = order.getReportDate();
        this.status = order.getStatus();
    }

    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDescriptionOfIssue() {
        return descriptionOfIssue;
    }

    public void setDescriptionOfIssue(String descriptionOfIssue) {
        this.descriptionOfIssue = descriptionOfIssue;
    }

    public Timestamp getReportDate() {
        return reportDate;
    }

    public void setReportDate(Timestamp reportDate) {
        this.reportDate = reportDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
} 