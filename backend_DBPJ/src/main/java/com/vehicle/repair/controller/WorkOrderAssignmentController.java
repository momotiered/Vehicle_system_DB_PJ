package com.vehicle.repair.controller;

import com.vehicle.repair.entity.AssignmentStatus;
import com.vehicle.repair.entity.WorkOrderAssignment;
import com.vehicle.repair.service.WorkOrderAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assignments")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class WorkOrderAssignmentController {

    @Autowired
    private WorkOrderAssignmentService assignmentService;
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<WorkOrderAssignment>> getAssignmentsByOrderId(@PathVariable Integer orderId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByOrderId(orderId));
    }
    
    @GetMapping("/order/{orderId}/status/{status}")
    public ResponseEntity<List<WorkOrderAssignment>> getOrderAssignmentsByStatus(
            @PathVariable Integer orderId,
            @PathVariable AssignmentStatus status) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByRepairOrderAndStatus(orderId, status));
    }
    
    @GetMapping("/order/{orderId}/primary")
    public ResponseEntity<WorkOrderAssignment> getPrimaryAssignmentByOrderId(@PathVariable Integer orderId) {
        return assignmentService.getPrimaryAssignmentByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<WorkOrderAssignment> createAssignment(
            @RequestBody Map<String, Object> assignmentData) {
        Integer orderId = (Integer) assignmentData.get("orderId");
        Integer technicianId = (Integer) assignmentData.get("technicianId");
        Boolean isPrimary = (Boolean) assignmentData.get("isPrimary");
        
        return ResponseEntity.ok(
                assignmentService.createAssignment(orderId, technicianId, isPrimary != null && isPrimary)
        );
    }
    
    @PutMapping("/{assignmentId}/accept")
    public ResponseEntity<WorkOrderAssignment> acceptAssignment(@PathVariable Integer assignmentId) {
        return ResponseEntity.ok(assignmentService.acceptAssignment(assignmentId));
    }
    
    @PutMapping("/{assignmentId}/reject")
    public ResponseEntity<WorkOrderAssignment> rejectAssignment(
            @PathVariable Integer assignmentId,
            @RequestBody Map<String, String> rejectionData) {
        return ResponseEntity.ok(
                assignmentService.rejectAssignment(assignmentId, rejectionData.get("reason"))
        );
    }
    
    @PutMapping("/{assignmentId}/complete")
    public ResponseEntity<WorkOrderAssignment> completeAssignment(
            @PathVariable Integer assignmentId,
            @RequestBody Map<String, Integer> completionData) {
        return ResponseEntity.ok(
                assignmentService.completeAssignment(assignmentId, completionData.get("laborHours"))
        );
    }
    
    @PostMapping("/auto-reassign/{orderId}")
    public ResponseEntity<Void> autoReassignOrder(@PathVariable Integer orderId) {
        assignmentService.autoReassignOrder(orderId);
        return ResponseEntity.ok().build();
    }
} 