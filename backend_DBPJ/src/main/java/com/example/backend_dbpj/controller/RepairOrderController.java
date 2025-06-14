package com.example.backend_dbpj.controller;

import com.example.backend_dbpj.dto.CreateRepairOrderRequest;
import com.example.backend_dbpj.entity.RepairOrder;
import com.example.backend_dbpj.service.RepairOrderService;
import com.example.backend_dbpj.dto.AddMaterialUsageRequestDto;
import com.example.backend_dbpj.dto.OrderMaterialUsedDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import javax.persistence.EntityNotFoundException;
import java.util.Map;

@RestController
@RequestMapping("/api/repair-orders")
public class RepairOrderController {

    @Autowired
    private RepairOrderService repairOrderService;

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
} 