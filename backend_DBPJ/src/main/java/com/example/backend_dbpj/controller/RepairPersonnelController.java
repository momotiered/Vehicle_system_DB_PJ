package com.example.backend_dbpj.controller;

import com.example.backend_dbpj.dto.RepairAssignmentDto;
import com.example.backend_dbpj.dto.RepairPersonnelLoginRequestDto;
import com.example.backend_dbpj.entity.enums.AssignmentStatus;
import com.example.backend_dbpj.dto.PayrollRecordDto;
import com.example.backend_dbpj.service.RepairPersonnelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/personnel")
public class RepairPersonnelController {

    @Autowired
    private RepairPersonnelService repairPersonnelService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody RepairPersonnelLoginRequestDto loginRequest) {
        try {
            return ResponseEntity.ok(repairPersonnelService.login(loginRequest));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRepairPersonnelById(@PathVariable("id") int personnelId) {
        try {
            return ResponseEntity.ok(repairPersonnelService.getRepairPersonnelById(personnelId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/assignments")
    public ResponseEntity<?> getAssignments(
            @PathVariable("id") int personnelId,
            @RequestParam(name = "status", defaultValue = "Assigned") AssignmentStatus status) {
        try {
            List<RepairAssignmentDto> assignments = repairPersonnelService.getAssignmentsByStatus(personnelId, status);
            return ResponseEntity.ok(assignments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "查询工单失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/payroll")
    public ResponseEntity<?> getPayrollRecords(@PathVariable("id") int personnelId) {
        try {
            List<PayrollRecordDto> records = repairPersonnelService.getPayrollRecordsByPersonnelId(personnelId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "查询薪资记录失败: " + e.getMessage()));
        }
    }
}
 