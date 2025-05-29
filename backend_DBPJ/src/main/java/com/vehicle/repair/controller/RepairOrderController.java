package com.vehicle.repair.controller;

import com.vehicle.repair.entity.RepairOrder;
import com.vehicle.repair.entity.RepairOrderStatus;
import com.vehicle.repair.service.RepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/repair-orders")
public class RepairOrderController {
    @Autowired
    private RepairOrderService repairOrderService;

    @PostMapping
    public ResponseEntity<RepairOrder> createRepairOrder(@RequestBody RepairOrder repairOrder) {
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
            @RequestParam RepairOrderStatus newStatus) {
        return ResponseEntity.ok(repairOrderService.updateRepairOrderStatus(orderId, newStatus));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<RepairOrder> updateRepairOrder(
            @PathVariable Integer orderId,
            @RequestBody RepairOrder repairOrder) {
        repairOrder.setOrderId(orderId);
        return ResponseEntity.ok(repairOrderService.updateRepairOrder(repairOrder));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteRepairOrder(@PathVariable Integer orderId) {
        repairOrderService.deleteRepairOrder(orderId);
        return ResponseEntity.ok().build();
    }
} 