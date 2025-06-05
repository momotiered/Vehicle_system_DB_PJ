package com.vehicle.repair.service;

import com.vehicle.repair.entity.Vehicle;
import com.vehicle.repair.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
public class VehicleServiceImpl implements VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Override
    public Vehicle createVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    @Override
    public Optional<Vehicle> getVehicleById(Integer vehicleId) {
        return vehicleRepository.findById(vehicleId);
    }

    @Override
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    @Override
    public List<Vehicle> getVehiclesByUserId(Integer userId) {
        return vehicleRepository.findByUserUserId(userId);
    }

    @Override
    public Vehicle updateVehicle(Vehicle vehicle) {
        if (!vehicleRepository.existsById(vehicle.getVehicleId())) {
            throw new EntityNotFoundException("Vehicle with ID " + vehicle.getVehicleId() + " not found");
        }
        return vehicleRepository.save(vehicle);
    }

    @Override
    public void deleteVehicle(Integer vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new EntityNotFoundException("Vehicle with ID " + vehicleId + " not found");
        }
        vehicleRepository.deleteById(vehicleId);
    }
} 