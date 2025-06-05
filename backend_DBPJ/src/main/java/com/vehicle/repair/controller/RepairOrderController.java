package com.vehicle.repair.controller;

import com.vehicle.repair.dto.RepairOrderRequestDTO;
import com.vehicle.repair.entity.RepairOrder;
import com.vehicle.repair.entity.RepairOrderStatus;
import com.vehicle.repair.entity.User;
import com.vehicle.repair.entity.Vehicle;
import com.vehicle.repair.service.RepairOrderService;
import com.vehicle.repair.service.StatusChangeLogService;
import com.vehicle.repair.service.UserService;
import com.vehicle.repair.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/repair-orders")
public class RepairOrderController {
    @Autowired
    private RepairOrderService repairOrderService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private VehicleService vehicleService;
    
    @Autowired
    private StatusChangeLogService statusChangeLogService;

    @PostMapping
    public ResponseEntity<RepairOrder> createRepairOrder(@RequestBody RepairOrder repairOrder) {
        return ResponseEntity.ok(repairOrderService.createRepairOrder(repairOrder));
    }
    
    @PostMapping("/user-request")
    public ResponseEntity<RepairOrder> createUserRepairRequest(@RequestBody RepairOrderRequestDTO requestDTO) {
        // 验证用户和车辆
        User user = userService.getUserById(requestDTO.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的用户ID"));
        
        Vehicle vehicle = vehicleService.getVehicleById(requestDTO.getVehicleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的车辆ID"));
        
        // 验证车辆是否属于该用户
        if (!vehicle.getUser().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "该车辆不属于当前用户");
        }
        
        // 创建维修工单
        RepairOrder repairOrder = new RepairOrder();
        repairOrder.setUser(user);
        repairOrder.setVehicle(vehicle);
        repairOrder.setDescriptionOfIssue(requestDTO.getDescriptionOfIssue());
        repairOrder.setUrgencyLevel(requestDTO.getUrgencyLevel());
        repairOrder.setExpectedServiceDate(requestDTO.getExpectedServiceDate());
        repairOrder.setStatus(requestDTO.getStatus());
        repairOrder.setNotes(requestDTO.getNotes());
        
        return ResponseEntity.ok(repairOrderService.createRepairOrder(repairOrder));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<RepairOrder> getRepairOrderById(@PathVariable Integer orderId) {
        return repairOrderService.getRepairOrderById(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RepairOrder>> getRepairOrdersByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(repairOrderService.getRepairOrdersByUserId(userId));
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<RepairOrder>> getRepairOrdersByVehicleId(@PathVariable Integer vehicleId) {
        return ResponseEntity.ok(repairOrderService.getRepairOrdersByVehicleId(vehicleId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<RepairOrder>> getRepairOrdersByStatus(@PathVariable RepairOrderStatus status) {
        return ResponseEntity.ok(repairOrderService.getRepairOrdersByStatus(status));
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<RepairOrder>> getRepairOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(repairOrderService.getRepairOrdersByDateRange(startDate, endDate));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<RepairOrder> updateRepairOrderStatus(
            @PathVariable Integer orderId,
            @RequestParam RepairOrderStatus newStatus,
            @RequestParam(required = false) Integer operatorId,
            @RequestParam(required = false) String reason) {
        
        String statusReason = reason != null ? reason : "状态更新";
        return ResponseEntity.ok(repairOrderService.updateRepairOrderStatus(orderId, newStatus, operatorId, statusReason));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<RepairOrder> updateRepairOrder(
            @PathVariable Integer orderId,
            @RequestBody RepairOrder repairOrder,
            @RequestParam(required = false) Integer operatorId) {
        repairOrder.setOrderId(orderId);
        return ResponseEntity.ok(repairOrderService.updateRepairOrder(repairOrder, operatorId));
    }
    
    @PostMapping("/{orderId}/assign")
    public ResponseEntity<RepairOrder> assignRepairOrder(
            @PathVariable Integer orderId,
            @RequestParam Integer technicianId,
            @RequestParam(required = false) Integer operatorId) {
        
        return ResponseEntity.ok(repairOrderService.assignRepairOrder(orderId, technicianId, operatorId));
    }
    
    @PostMapping("/{orderId}/progress")
    public ResponseEntity<RepairOrder> updateProgress(
            @PathVariable Integer orderId,
            @RequestBody Map<String, Object> request) {
        
        String progressNotes = (String) request.get("progressNotes");
        Integer operatorId = (Integer) request.get("operatorId");
        
        if (progressNotes == null || progressNotes.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "进度备注不能为空");
        }
        
        return ResponseEntity.ok(repairOrderService.updateProgress(orderId, progressNotes, operatorId));
    }
    
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<RepairOrder> completeRepairOrder(
            @PathVariable Integer orderId,
            @RequestBody Map<String, Object> request) {
        
        String completionNotes = (String) request.get("completionNotes");
        Integer operatorId = (Integer) request.get("operatorId");
        
        return ResponseEntity.ok(repairOrderService.completeRepairOrder(orderId, completionNotes, operatorId));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteRepairOrder(@PathVariable Integer orderId) {
        repairOrderService.deleteRepairOrder(orderId);
        return ResponseEntity.ok().build();
    }
} 