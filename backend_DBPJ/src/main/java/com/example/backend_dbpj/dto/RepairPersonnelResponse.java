package com.example.backend_dbpj.dto;

import com.example.backend_dbpj.entity.RepairPersonnel;
import java.math.BigDecimal;
import java.sql.Date;

public class RepairPersonnelResponse {

    private int personnelId;
    private String username;
    private String fullName;
    private String workType;
    private BigDecimal hourlyRate;
    private String contactPhone;
    private Date hireDate;
    private boolean isActive;
    private final String role = "TECHNICIAN"; // 添加固定的角色字段

    public RepairPersonnelResponse(RepairPersonnel personnel) {
        this.personnelId = personnel.getPersonnelId();
        this.username = personnel.getUsername();
        this.fullName = personnel.getFullName();
        this.workType = personnel.getWorkType();
        this.hourlyRate = personnel.getHourlyRate();
        this.contactPhone = personnel.getContactPhone();
        this.hireDate = personnel.getHireDate();
        this.isActive = personnel.isActive();
    }

    // Getters and Setters
    public int getPersonnelId() {
        return personnelId;
    }

    public void setPersonnelId(int personnelId) {
        this.personnelId = personnelId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getRole() {
        return role;
    }
} 