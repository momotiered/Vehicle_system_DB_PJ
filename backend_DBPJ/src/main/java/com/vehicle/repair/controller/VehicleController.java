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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Vehicle> createVehicle(@RequestBody Map<String, Object> vehicleData) {
        try {
            // 从前端数据中提取必要信息
            String licensePlate = (String) vehicleData.get("licensePlate");
            Integer userId = (Integer) vehicleData.get("userId");
            String brand = (String) vehicleData.get("brand");
            String model = (String) vehicleData.get("model");
            Integer year = (Integer) vehicleData.get("year");
            String color = (String) vehicleData.get("color");

            // 验证必要字段
            if (licensePlate == null || licensePlate.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "车牌号不能为空");
            }
            
            if (userId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "用户ID不能为空");
            }

            // 验证用户是否存在
            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "无效的用户ID"));

            // 验证车牌号是否已存在
            if (vehicleService.findByLicensePlate(licensePlate).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "该车牌号已注册");
            }

            // 创建Vehicle对象
            Vehicle vehicle = new Vehicle();
            vehicle.setLicensePlate(licensePlate);
            vehicle.setUser(user);
            vehicle.setMake(brand);  // 前端的brand对应后端的make
            vehicle.setModel(model);
            vehicle.setYearOfManufacture(year);  // 前端的year对应后端的yearOfManufacture
            vehicle.setColor(color);
            vehicle.setRegistrationDate(LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(vehicleService.createVehicle(vehicle));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "创建车辆时发生错误: " + e.getMessage());
        }
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