package com.vehicle.repair.controller;

import com.vehicle.repair.entity.User;
import com.vehicle.repair.entity.Vehicle;
import com.vehicle.repair.service.UserService;
import com.vehicle.repair.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(vehicleService.getAllVehicles());
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Integer vehicleId) {
        return vehicleService.getVehicleById(vehicleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Vehicle>> getVehiclesByUserId(@PathVariable Integer userId) {
        // 验证用户是否存在
        if (!userService.getUserById(userId).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(vehicleService.getVehiclesByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<Vehicle> createVehicle(@RequestBody Vehicle vehicle) {
        // 验证用户是否存在
        User user = userService.getUserById(vehicle.getUser().getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的用户ID"));
        
        // 设置用户
        vehicle.setUser(user);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vehicleService.createVehicle(vehicle));
    }

    @PutMapping("/{vehicleId}")
    public ResponseEntity<Vehicle> updateVehicle(
            @PathVariable Integer vehicleId,
            @RequestBody Vehicle vehicle) {
        // 设置ID
        vehicle.setVehicleId(vehicleId);
        
        // 验证车辆是否存在
        if (!vehicleService.getVehicleById(vehicleId).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        // 验证用户是否存在
        if (vehicle.getUser() != null && vehicle.getUser().getUserId() != null) {
            User user = userService.getUserById(vehicle.getUser().getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的用户ID"));
            vehicle.setUser(user);
        }
        
        return ResponseEntity.ok(vehicleService.updateVehicle(vehicle));
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Integer vehicleId) {
        // 验证车辆是否存在
        if (!vehicleService.getVehicleById(vehicleId).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        vehicleService.deleteVehicle(vehicleId);
        return ResponseEntity.ok().build();
    }
} 