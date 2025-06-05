package com.vehicle.repair.controller;

import com.vehicle.repair.entity.RepairMaterial;
import com.vehicle.repair.service.RepairMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/materials")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class RepairMaterialController {

    @Autowired
    private RepairMaterialService materialService;
    
    @GetMapping
    public ResponseEntity<List<RepairMaterial>> getAllMaterials() {
        return ResponseEntity.ok(materialService.getAllMaterials());
    }
    
    @GetMapping("/{materialId}")
    public ResponseEntity<RepairMaterial> getMaterialById(@PathVariable Integer materialId) {
        return materialService.getMaterialById(materialId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<RepairMaterial>> getMaterialsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(materialService.getMaterialsByCategory(category));
    }
    
    @GetMapping("/low-stock")
    public ResponseEntity<List<RepairMaterial>> getLowStockMaterials(@RequestParam(defaultValue = "10") Integer threshold) {
        return ResponseEntity.ok(materialService.getLowStockMaterials(threshold));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<RepairMaterial>> searchMaterials(@RequestParam String keyword) {
        return ResponseEntity.ok(materialService.searchMaterials(keyword));
    }
    
    @PostMapping
    public ResponseEntity<RepairMaterial> createMaterial(@RequestBody RepairMaterial material) {
        return ResponseEntity.ok(materialService.createMaterial(material));
    }
    
    @PutMapping("/{materialId}")
    public ResponseEntity<RepairMaterial> updateMaterial(
            @PathVariable Integer materialId,
            @RequestBody RepairMaterial material) {
        material.setMaterialId(materialId);
        return ResponseEntity.ok(materialService.updateMaterial(material));
    }
    
    @PutMapping("/{materialId}/stock")
    public ResponseEntity<RepairMaterial> updateStock(
            @PathVariable Integer materialId,
            @RequestBody Map<String, Integer> stockData) {
        return ResponseEntity.ok(materialService.updateStock(materialId, stockData.get("quantity")));
    }
    
    @PutMapping("/{materialId}/stock/increase")
    public ResponseEntity<RepairMaterial> increaseStock(
            @PathVariable Integer materialId,
            @RequestBody Map<String, Integer> stockData) {
        return ResponseEntity.ok(materialService.increaseStock(materialId, stockData.get("amount")));
    }
    
    @PutMapping("/{materialId}/stock/decrease")
    public ResponseEntity<RepairMaterial> decreaseStock(
            @PathVariable Integer materialId,
            @RequestBody Map<String, Integer> stockData) {
        return ResponseEntity.ok(materialService.decreaseStock(materialId, stockData.get("amount")));
    }
    
    @DeleteMapping("/{materialId}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Integer materialId) {
        materialService.deleteMaterial(materialId);
        return ResponseEntity.ok().build();
    }
} 