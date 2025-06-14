package com.example.backend_dbpj.entity;

import com.example.backend_dbpj.entity.enums.AssignmentStatus;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity(name = "RepairAssignments")
public class RepairAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private int assignmentId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private RepairOrder repairOrder;

    @ManyToOne
    @JoinColumn(name = "personnel_id", nullable = false)
    private RepairPersonnel repairPersonnel;

    @Column(name = "assignment_date")
    private Timestamp assignmentDate;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;

    @Column(name = "hours_worked")
    private BigDecimal hoursWorked;

    @Column(name = "labor_cost_for_personnel")
    private BigDecimal laborCostForPersonnel;

    private String notes;


    // Getters and Setters

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public RepairOrder getRepairOrder() {
        return repairOrder;
    }

    public void setRepairOrder(RepairOrder repairOrder) {
        this.repairOrder = repairOrder;
    }

    public RepairPersonnel getRepairPersonnel() {
        return repairPersonnel;
    }

    public void setRepairPersonnel(RepairPersonnel repairPersonnel) {
        this.repairPersonnel = repairPersonnel;
    }

    public Timestamp getAssignmentDate() {
        return assignmentDate;
    }

    public void setAssignmentDate(Timestamp assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }

    public BigDecimal getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(BigDecimal hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public BigDecimal getLaborCostForPersonnel() {
        return laborCostForPersonnel;
    }

    public void setLaborCostForPersonnel(BigDecimal laborCostForPersonnel) {
        this.laborCostForPersonnel = laborCostForPersonnel;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
} 