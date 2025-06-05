package com.vehicle.repair.controller;

import com.vehicle.repair.entity.SkillType;
import com.vehicle.repair.entity.Technician;
import com.vehicle.repair.entity.WorkOrderAssignment;
import com.vehicle.repair.entity.AssignmentStatus;
import com.vehicle.repair.service.TechnicianService;
import com.vehicle.repair.service.WorkOrderAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/technicians")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TechnicianController {

    @Autowired
    private TechnicianService technicianService;
    
    @Autowired
    private WorkOrderAssignmentService assignmentService;
    
    @GetMapping
    public ResponseEntity<List<Technician>> getAllTechnicians() {
        return ResponseEntity.ok(technicianService.getAllTechnicians());
    }
    
    @GetMapping("/{technicianId}")
    public ResponseEntity<Technician> getTechnicianById(@PathVariable Integer technicianId) {
        return technicianService.getTechnicianById(technicianId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Technician> getTechnicianByUserId(@PathVariable Integer userId) {
        return technicianService.getTechnicianByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/skill/{skillType}")
    public ResponseEntity<List<Technician>> getTechniciansBySkill(@PathVariable SkillType skillType) {
        return ResponseEntity.ok(technicianService.getTechniciansBySkill(skillType));
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<Technician>> getAvailableTechnicians() {
        return ResponseEntity.ok(technicianService.getAvailableTechnicians());
    }
    
    @GetMapping("/available/skill/{skillType}")
    public ResponseEntity<List<Technician>> getAvailableTechniciansBySkill(@PathVariable SkillType skillType) {
        return ResponseEntity.ok(technicianService.getAvailableTechniciansBySkill(skillType));
    }
    
    @PostMapping
    public ResponseEntity<Technician> createTechnician(
            @RequestBody Technician technician,
            @RequestParam Integer userId) {
        return ResponseEntity.ok(technicianService.createTechnician(technician, userId));
    }
    
    @PutMapping("/{technicianId}")
    public ResponseEntity<Technician> updateTechnician(
            @PathVariable Integer technicianId,
            @RequestBody Technician technician) {
        technician.setTechnicianId(technicianId);
        return ResponseEntity.ok(technicianService.updateTechnician(technician));
    }
    
    @DeleteMapping("/{technicianId}")
    public ResponseEntity<Void> deleteTechnician(@PathVariable Integer technicianId) {
        technicianService.deleteTechnician(technicianId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{technicianId}/availability")
    public ResponseEntity<Technician> updateAvailability(
            @PathVariable Integer technicianId,
            @RequestBody Map<String, Boolean> availability) {
        Technician technician = new Technician();
        technician.setTechnicianId(technicianId);
        technician.setIsAvailable(availability.get("available"));
        return ResponseEntity.ok(technicianService.updateTechnician(technician));
    }
    
    // 获取技术人员的所有工单分配
    @GetMapping("/{technicianId}/assignments")
    public ResponseEntity<List<WorkOrderAssignment>> getTechnicianAssignments(@PathVariable Integer technicianId) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByTechnicianId(technicianId));
    }
    
    // 获取技术人员特定状态的工单分配
    @GetMapping("/{technicianId}/assignments/status/{status}")
    public ResponseEntity<List<WorkOrderAssignment>> getTechnicianAssignmentsByStatus(
            @PathVariable Integer technicianId,
            @PathVariable AssignmentStatus status) {
        return ResponseEntity.ok(assignmentService.getTechnicianAssignmentsByStatus(technicianId, status));
    }
    
    // 技术人员接受工单
    @PutMapping("/assignments/{assignmentId}/accept")
    public ResponseEntity<WorkOrderAssignment> acceptAssignment(@PathVariable Integer assignmentId) {
        return ResponseEntity.ok(assignmentService.acceptAssignment(assignmentId));
    }
    
    // 技术人员拒绝工单
    @PutMapping("/assignments/{assignmentId}/reject")
    public ResponseEntity<WorkOrderAssignment> rejectAssignment(
            @PathVariable Integer assignmentId,
            @RequestBody Map<String, String> rejectionData) {
        return ResponseEntity.ok(assignmentService.rejectAssignment(assignmentId, rejectionData.get("reason")));
    }
    
    // 技术人员完成工单
    @PutMapping("/assignments/{assignmentId}/complete")
    public ResponseEntity<WorkOrderAssignment> completeAssignment(
            @PathVariable Integer assignmentId,
            @RequestBody Map<String, Integer> completionData) {
        return ResponseEntity.ok(assignmentService.completeAssignment(assignmentId, completionData.get("laborHours")));
    }
} 