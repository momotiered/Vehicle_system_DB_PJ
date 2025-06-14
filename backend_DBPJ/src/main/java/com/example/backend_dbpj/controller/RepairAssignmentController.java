package com.example.backend_dbpj.controller;

import com.example.backend_dbpj.dto.RepairAssignmentDto;
import com.example.backend_dbpj.dto.UpdateAssignmentStatusDto;
import com.example.backend_dbpj.dto.LogHoursRequestDto;
import com.example.backend_dbpj.service.RepairAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/assignments")
public class RepairAssignmentController {

    @Autowired
    private RepairAssignmentService repairAssignmentService;

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateAssignmentStatus(
            @PathVariable("id") int assignmentId,
            @RequestBody UpdateAssignmentStatusDto statusDto) {
        try {
            RepairAssignmentDto updatedAssignment = repairAssignmentService.updateAssignmentStatus(assignmentId, statusDto.getStatus());
            return ResponseEntity.ok(updatedAssignment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/hours")
    public ResponseEntity<?> logHours(
            @PathVariable("id") int assignmentId,
            @RequestBody LogHoursRequestDto hoursDto) {
        try {
            RepairAssignmentDto updatedAssignment = repairAssignmentService.logHoursToAssignment(assignmentId, hoursDto.getHours());
            return ResponseEntity.ok(updatedAssignment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
} 