package com.example.backend_dbpj.controller;

import com.example.backend_dbpj.dto.LoginRequest;
import com.example.backend_dbpj.dto.UserRegistrationRequest;
import com.example.backend_dbpj.dto.UserResponse;
import com.example.backend_dbpj.entity.User;
import com.example.backend_dbpj.entity.Vehicle;
import com.example.backend_dbpj.entity.RepairOrder;
import com.example.backend_dbpj.service.UserService;
import com.example.backend_dbpj.service.VehicleService;
import com.example.backend_dbpj.service.RepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import javax.persistence.EntityNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private RepairOrderService repairOrderService;

    @PostMapping("/register")
    public User registerUser(@RequestBody UserRegistrationRequest registrationRequest) {
        return userService.registerUser(registrationRequest);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            UserResponse userResponse = userService.login(loginRequest);
            return ResponseEntity.ok(userResponse);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable int userId) {
        try {
            UserResponse userResponse = userService.getUserById(userId);
            return ResponseEntity.ok(userResponse);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{userId}/vehicles")
    public ResponseEntity<List<Vehicle>> getVehiclesByUserId(@PathVariable int userId) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByUserId(userId);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{userId}/repair-orders")
    public ResponseEntity<List<RepairOrder>> getRepairOrdersByUserId(@PathVariable int userId) {
        List<RepairOrder> repairOrders = repairOrderService.getRepairOrdersByUserId(userId);
        return ResponseEntity.ok(repairOrders);
    }
} 