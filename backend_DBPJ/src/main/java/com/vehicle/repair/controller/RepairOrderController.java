package com.vehicle.repair.controller;

import com.vehicle.repair.dto.RepairOrderRequestDTO;
import com.vehicle.repair.entity.RepairOrder;
import com.vehicle.repair.entity.RepairOrderStatus;
import com.vehicle.repair.entity.UrgencyLevel;
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

import java.time.LocalDate;
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
    public ResponseEntity<RepairOrder> createUserRepairRequest(@RequestBody Map<String, Object> requestData) {
        try {
            // 从前端数据中提取必要信息
            Integer vehicleId = (Integer) requestData.get("vehicleId");
            Integer userId = (Integer) requestData.get("userId");
            String descriptionOfIssue = (String) requestData.get("descriptionOfIssue");
            String urgencyLevelStr = (String) requestData.get("urgencyLevel");
            String statusStr = (String) requestData.get("status");
            String notes = (String) requestData.get("notes");
            String expectedServiceDateStr = (String) requestData.get("expectedServiceDate");

            // 验证必要字段
            if (vehicleId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "车辆ID不能为空");
            }
            if (userId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户ID不能为空");
            }
            if (descriptionOfIssue == null || descriptionOfIssue.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "问题描述不能为空");
            }

            // 验证用户和车辆
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的用户ID"));
            
            Vehicle vehicle = vehicleService.getVehicleById(vehicleId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的车辆ID"));
            
            // 验证车辆是否属于该用户
            if (!vehicle.getUser().getUserId().equals(user.getUserId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "该车辆不属于当前用户");
            }
            
            // 创建维修工单
            RepairOrder repairOrder = new RepairOrder();
            repairOrder.setUser(user);
            repairOrder.setVehicle(vehicle);
            repairOrder.setDescriptionOfIssue(descriptionOfIssue);
            
            // 处理紧急程度枚举
            if (urgencyLevelStr != null) {
                try {
                    repairOrder.setUrgencyLevel(UrgencyLevel.valueOf(urgencyLevelStr));
                } catch (IllegalArgumentException e) {
                    repairOrder.setUrgencyLevel(UrgencyLevel.MEDIUM);
                }
            } else {
                repairOrder.setUrgencyLevel(UrgencyLevel.MEDIUM);
            }
            
            // 处理状态枚举
            if (statusStr != null) {
                try {
                    repairOrder.setStatus(RepairOrderStatus.valueOf(statusStr));
                } catch (IllegalArgumentException e) {
                    repairOrder.setStatus(RepairOrderStatus.PENDING_ASSIGNMENT);
                }
            } else {
                repairOrder.setStatus(RepairOrderStatus.PENDING_ASSIGNMENT);
            }
            
            // 处理期望服务日期
            if (expectedServiceDateStr != null && !expectedServiceDateStr.trim().isEmpty()) {
                try {
                    LocalDate expectedServiceDate = LocalDate.parse(expectedServiceDateStr);
                    repairOrder.setExpectedServiceDate(expectedServiceDate);
                } catch (Exception e) {
                    // 如果日期格式不正确，设置为空
                    repairOrder.setExpectedServiceDate(null);
                }
            }
            
            repairOrder.setNotes(notes);
            repairOrder.setReportDate(LocalDateTime.now());
            
            RepairOrder savedOrder = repairOrderService.createRepairOrder(repairOrder);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "创建维修申请时发生错误: " + e.getMessage());
        }
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