package com.example.backend_dbpj.controller;

import com.example.backend_dbpj.dto.AssignPersonnelRequestDto;
import com.example.backend_dbpj.dto.RepairOrderDto;
import com.example.backend_dbpj.dto.RepairPersonnelResponseDto;
import com.example.backend_dbpj.dto.CreateUserRequestDto;
import com.example.backend_dbpj.dto.UpdateUserRequestDto;
import com.example.backend_dbpj.dto.CreatePersonnelRequestDto;
import com.example.backend_dbpj.dto.UpdatePersonnelRequestDto;
import com.example.backend_dbpj.entity.User;
import com.example.backend_dbpj.entity.RepairPersonnel;
import com.example.backend_dbpj.entity.Material;
import com.example.backend_dbpj.entity.Vehicle;
import com.example.backend_dbpj.service.RepairOrderService;
import com.example.backend_dbpj.service.RepairPersonnelService;
import com.example.backend_dbpj.service.UserService;
import com.example.backend_dbpj.service.FeedbackService;
import com.example.backend_dbpj.repository.MaterialRepository;
import com.example.backend_dbpj.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private RepairOrderService repairOrderService;

    @Autowired
    private RepairPersonnelService repairPersonnelService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private FeedbackService feedbackService;
    
    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @GetMapping("/orders/pending")
    public ResponseEntity<List<RepairOrderDto>> getPendingAssignmentOrders() {
        List<RepairOrderDto> pendingOrders = repairOrderService.getPendingOrders();
        return ResponseEntity.ok(pendingOrders);
    }

    @GetMapping("/personnel/available")
    public ResponseEntity<List<RepairPersonnelResponseDto>> getAvailablePersonnel() {
        List<RepairPersonnelResponseDto> availablePersonnel = repairPersonnelService.getAvailablePersonnel();
        return ResponseEntity.ok(availablePersonnel);
    }

    @PostMapping("/orders/{orderId}/assign")
    public ResponseEntity<?> assignOrder(
            @PathVariable int orderId,
            @RequestBody AssignPersonnelRequestDto request) {
        try {
            repairOrderService.assignOrderToPersonnel(orderId, request.getPersonnelIds());
            return ResponseEntity.ok(Map.of("message", "工单分配成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // --- User Management Endpoints ---

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequestDto userDto) {
        return ResponseEntity.ok(userService.createUser(userDto));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable int id, @RequestBody UpdateUserRequestDto userDto) {
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    // --- Repair Personnel Management Endpoints ---

    @GetMapping("/personnel")
    public ResponseEntity<List<RepairPersonnel>> getAllPersonnel() {
        return ResponseEntity.ok(repairPersonnelService.getAllPersonnel());
    }

    @PostMapping("/personnel")
    public ResponseEntity<RepairPersonnel> createPersonnel(@RequestBody CreatePersonnelRequestDto personnelDto) {
        return ResponseEntity.ok(repairPersonnelService.createPersonnel(personnelDto));
    }

    @PutMapping("/personnel/{id}")
    public ResponseEntity<RepairPersonnel> updatePersonnel(@PathVariable int id, @RequestBody UpdatePersonnelRequestDto personnelDto) {
        return ResponseEntity.ok(repairPersonnelService.updatePersonnel(id, personnelDto));
    }

    @DeleteMapping("/personnel/{id}")
    public ResponseEntity<?> deletePersonnel(@PathVariable int id) {
        repairPersonnelService.deletePersonnel(id);
        return ResponseEntity.ok(Map.of("message", "Personnel deleted successfully"));
    }

    // --- Vehicle Management Endpoints ---
    @GetMapping("/vehicles")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(vehicleRepository.findAll());
    }

    @GetMapping("/vehicles/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable int id) {
        return ResponseEntity.ok(vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id)));
    }

    @PutMapping("/vehicles/{id}")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable int id, @RequestBody Vehicle vehicle) {
        Vehicle existingVehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + id));
        
        existingVehicle.setLicensePlate(vehicle.getLicensePlate());
        existingVehicle.setMake(vehicle.getMake());
        existingVehicle.setModel(vehicle.getModel());
        existingVehicle.setYearOfManufacture(vehicle.getYearOfManufacture());
        existingVehicle.setColor(vehicle.getColor());
        
        return ResponseEntity.ok(vehicleRepository.save(existingVehicle));
    }

    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable int id) {
        if (!vehicleRepository.existsById(id)) {
            throw new EntityNotFoundException("Vehicle not found with id: " + id);
        }
        vehicleRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Vehicle deleted successfully"));
    }

    // --- Monitor Dashboard Endpoints ---
    
    @GetMapping("/monitor/overview")
    public ResponseEntity<Map<String, Object>> getOverviewStats() {
        Map<String, Object> stats = repairOrderService.getOverviewStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/monitor/order-status")
    public ResponseEntity<Map<String, Object>> getOrderStatusStats() {
        Map<String, Object> stats = repairOrderService.getOrderStatusStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/monitor/financial")
    public ResponseEntity<Map<String, Object>> getFinancialStats() {
        Map<String, Object> stats = repairOrderService.getFinancialStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/monitor/inventory-alerts")
    public ResponseEntity<List<Map<String, Object>>> getInventoryAlerts() {
        List<Material> lowStockMaterials = materialRepository.findLowStockMaterials(20); // 安全库存阈值设为20
        List<Map<String, Object>> alerts = lowStockMaterials.stream()
            .map(material -> {
                Map<String, Object> alert = new HashMap<>();
                alert.put("materialId", material.getMaterialId());
                alert.put("materialName", material.getMaterialName());
                alert.put("stockQuantity", material.getStockQuantity());
                alert.put("safeStock", 20);
                
                String alertLevel;
                if (material.getStockQuantity() < 5) {
                    alertLevel = "紧急";
                } else if (material.getStockQuantity() < 10) {
                    alertLevel = "警告";
                } else {
                    alertLevel = "注意";
                }
                alert.put("alertLevel", alertLevel);
                
                return alert;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(alerts);
    }
    
    @GetMapping("/monitor/personnel")
    public ResponseEntity<Map<String, Object>> getPersonnelStats() {
        Map<String, Object> stats = repairPersonnelService.getPersonnelStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/monitor/satisfaction")
    public ResponseEntity<Map<String, Object>> getSatisfactionStats() {
        Map<String, Object> stats = feedbackService.getSatisfactionStats();
        return ResponseEntity.ok(stats);
    }

} 