package com.example.backend_dbpj.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name = "RepairPersonnel")
public class RepairPersonnel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "personnel_id")
    private int personnelId;

    @Column(unique = true)
    private String username;

    @Column(name = "password_hash")
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "work_type")
    private String workType;

    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;

    @Column(name = "contact_phone", unique = true)
    private String contactPhone;

    @Column(name = "hire_date")
    private Date hireDate;

    @Column(name = "is_active")
    private boolean isActive;

    @JsonIgnore
    @OneToMany(mappedBy = "repairPersonnel")
    private List<RepairAssignment> repairAssignments;

    @JsonIgnore
    @OneToMany(mappedBy = "repairPersonnel")
    private List<PayrollRecord> payrollRecords;


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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    @JsonProperty("isActive")
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<RepairAssignment> getRepairAssignments() {
        return repairAssignments;
    }

    public void setRepairAssignments(List<RepairAssignment> repairAssignments) {
        this.repairAssignments = repairAssignments;
    }

    public List<PayrollRecord> getPayrollRecords() {
        return payrollRecords;
    }

    public void setPayrollRecords(List<PayrollRecord> payrollRecords) {
        this.payrollRecords = payrollRecords;
    }
} 