package com.vehicle.repair.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    private String fullName;

    @Column(unique = true)
    private String contactPhone;

    @Column(unique = true)
    private String contactEmail;

    private String address;

    @CreationTimestamp
    private LocalDateTime registrationDate;
} 