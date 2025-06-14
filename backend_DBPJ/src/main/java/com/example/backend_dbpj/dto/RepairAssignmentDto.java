package com.example.backend_dbpj.dto;

import com.example.backend_dbpj.entity.RepairAssignment;
import com.example.backend_dbpj.entity.enums.AssignmentStatus;

import java.sql.Timestamp;

public class RepairAssignmentDto {
    private int assignmentId;
    private int orderId;
    private String issueDescription;
    private Timestamp assignmentDate;
    private AssignmentStatus assignmentStatus;

    public RepairAssignmentDto(RepairAssignment assignment) {
        this.assignmentId = assignment.getAssignmentId();
        this.orderId = assignment.getRepairOrder().getOrderId();
        this.issueDescription = assignment.getRepairOrder().getDescriptionOfIssue();
        this.assignmentDate = assignment.getAssignmentDate();
        this.assignmentStatus = assignment.getStatus();
    }

    // Getters and Setters
    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public void setIssueDescription(String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public Timestamp getAssignmentDate() {
        return assignmentDate;
    }

    public void setAssignmentDate(Timestamp assignmentDate) {
        this.assignmentDate = assignmentDate;
    }

    public AssignmentStatus getAssignmentStatus() {
        return assignmentStatus;
    }

    public void setAssignmentStatus(AssignmentStatus assignmentStatus) {
        this.assignmentStatus = assignmentStatus;
    }
} 