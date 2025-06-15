package com.example.backend_dbpj.controller;

import com.example.backend_dbpj.dto.CreateRepairOrderRequest;
import com.example.backend_dbpj.entity.RepairOrder;
import com.example.backend_dbpj.service.RepairOrderService;
import com.example.backend_dbpj.dto.AddMaterialUsageRequestDto;
import com.example.backend_dbpj.dto.OrderMaterialUsedDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/repair-orders")
public class RepairOrderController {

    @Autowired
    private RepairOrderService repairOrderService;

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable int orderId) {
        try {
            RepairOrder repairOrder = repairOrderService.getOrderById(orderId);
            return ResponseEntity.ok(repairOrder);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        try {
            List<RepairOrder> orders = repairOrderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "获取工单列表失败: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createRepairOrder(@RequestBody CreateRepairOrderRequest request) {
        try {
            RepairOrder newRepairOrder = repairOrderService.createRepairOrder(request);
            return new ResponseEntity<>(newRepairOrder, HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/{orderId}/urge")
    public ResponseEntity<?> urgeRepairOrder(@PathVariable int orderId) {
        try {
            repairOrderService.urgeRepairOrder(orderId);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/{orderId}/materials")
    public ResponseEntity<?> addMaterialToOrder(
            @PathVariable int orderId,
            @RequestBody AddMaterialUsageRequestDto requestDto) {
        try {
            OrderMaterialUsedDto result = repairOrderService.addMaterialToOrder(orderId, requestDto);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/assign")
    public ResponseEntity<?> assignRepairOrder(
            @PathVariable int orderId,
            @RequestBody List<Integer> personnelIds) {
        try {
            RepairOrder result = repairOrderService.assignRepairOrder(orderId, personnelIds);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
} 