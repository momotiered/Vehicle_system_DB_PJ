package com.example.backend_dbpj.dto;

public class CreateRepairOrderRequest {
    private Integer userId;
    private Integer vehicleId;
    private String descriptionOfIssue;

    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Integer vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getDescriptionOfIssue() {
        return descriptionOfIssue;
    }

    public void setDescriptionOfIssue(String descriptionOfIssue) {
        this.descriptionOfIssue = descriptionOfIssue;
    }
} 