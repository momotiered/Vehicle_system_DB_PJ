package com.example.backend_dbpj.service;

import com.example.backend_dbpj.entity.Vehicle;
import com.example.backend_dbpj.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    public List<Vehicle> getVehiclesByUserId(Integer userId) {
        return vehicleRepository.findByUser_UserId(userId);
    }
} 