package com.vehicle.repair.controller;

import com.vehicle.repair.dto.StatusChangeDTO;
import com.vehicle.repair.service.StatusChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/status-logs")
public class StatusChangeLogController {

    @Autowired
    private StatusChangeLogService statusChangeLogService;
    
    /**
     * 获取工单的状态变更历史
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<StatusChangeDTO>> getOrderStatusHistory(@PathVariable Integer orderId) {
        return ResponseEntity.ok(statusChangeLogService.getStatusChangeHistory(orderId));
    }
    
    /**
     * 获取用户操作的状态变更历史
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<StatusChangeDTO>> getUserStatusChangeHistory(@PathVariable Integer userId) {
        return ResponseEntity.ok(statusChangeLogService.getUserChangeHistory(userId));
    }
} 