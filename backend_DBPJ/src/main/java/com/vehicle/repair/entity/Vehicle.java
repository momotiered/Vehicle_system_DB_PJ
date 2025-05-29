package com.vehicle.repair.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Vehicles")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer vehicleId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String licensePlate;

    private String make;

    private String model;

    private Integer yearOfManufacture;

    @Column(unique = true)
    private String vin;

    private String color;

    @CreationTimestamp
    private LocalDateTime registrationDate;
} 