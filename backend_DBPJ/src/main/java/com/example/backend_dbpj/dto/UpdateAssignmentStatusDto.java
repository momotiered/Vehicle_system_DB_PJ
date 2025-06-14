package com.example.backend_dbpj.dto;

import com.example.backend_dbpj.entity.enums.AssignmentStatus;

public class UpdateAssignmentStatusDto {
    private AssignmentStatus status;

    public AssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status = status;
    }
} 