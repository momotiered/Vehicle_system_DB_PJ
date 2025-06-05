package com.vehicle.repair.controller;

import com.vehicle.repair.entity.MaterialUsage;
import com.vehicle.repair.service.MaterialUsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/material-usage")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class MaterialUsageController {

    @Autowired
    private MaterialUsageService materialUsageService;
    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<MaterialUsage>> getUsagesByOrderId(@PathVariable Integer orderId) {
        return ResponseEntity.ok(materialUsageService.getUsagesByOrderId(orderId));
    }
    
    @GetMapping("/material/{materialId}")
    public ResponseEntity<List<MaterialUsage>> getUsagesByMaterialId(@PathVariable Integer materialId) {
        return ResponseEntity.ok(materialUsageService.getUsagesByMaterialId(materialId));
    }
    
    @GetMapping("/technician/{technicianId}")
    public ResponseEntity<List<MaterialUsage>> getUsagesByTechnicianId(@PathVariable Integer technicianId) {
        return ResponseEntity.ok(materialUsageService.getUsagesByTechnicianId(technicianId));
    }
    
    @GetMapping("/order/{orderId}/total-cost")
    public ResponseEntity<Map<String, BigDecimal>> getTotalMaterialCost(@PathVariable Integer orderId) {
        BigDecimal totalCost = materialUsageService.calculateTotalMaterialCost(orderId);
        return ResponseEntity.ok(Map.of("totalCost", totalCost));
    }
    
    @PostMapping
    public ResponseEntity<MaterialUsage> createMaterialUsage(
            @RequestBody MaterialUsage materialUsage,
            @RequestParam Integer orderId,
            @RequestParam Integer materialId,
            @RequestParam Integer technicianId) {
        return ResponseEntity.ok(
                materialUsageService.createMaterialUsage(materialUsage, orderId, materialId, technicianId)
        );
    }
    
    @PutMapping("/{usageId}")
    public ResponseEntity<MaterialUsage> updateMaterialUsage(
            @PathVariable Integer usageId,
            @RequestBody MaterialUsage materialUsage) {
        materialUsage.setUsageId(usageId);
        return ResponseEntity.ok(materialUsageService.updateMaterialUsage(materialUsage));
    }
    
    @DeleteMapping("/{usageId}")
    public ResponseEntity<Void> deleteMaterialUsage(@PathVariable Integer usageId) {
        materialUsageService.deleteMaterialUsage(usageId);
        return ResponseEntity.ok().build();
    }
} 