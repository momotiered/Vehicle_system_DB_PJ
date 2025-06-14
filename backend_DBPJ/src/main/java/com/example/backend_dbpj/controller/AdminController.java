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
import com.example.backend_dbpj.service.RepairOrderService;
import com.example.backend_dbpj.service.RepairPersonnelService;
import com.example.backend_dbpj.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private RepairOrderService repairOrderService;

    @Autowired
    private RepairPersonnelService repairPersonnelService;

    @Autowired
    private UserService userService;

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

    // 管理员相关接口将在这里定义

} 