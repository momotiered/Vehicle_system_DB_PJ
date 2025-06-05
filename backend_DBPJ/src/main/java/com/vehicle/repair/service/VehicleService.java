package com.vehicle.repair.service;

import com.vehicle.repair.entity.Vehicle;

import java.util.List;
import java.util.Optional;

public interface VehicleService {
    
    Vehicle createVehicle(Vehicle vehicle);
    
    Optional<Vehicle> getVehicleById(Integer vehicleId);
    
    List<Vehicle> getAllVehicles();
    
    List<Vehicle> getVehiclesByUserId(Integer userId);
    
    Vehicle updateVehicle(Vehicle vehicle);
    
    void deleteVehicle(Integer vehicleId);
} 