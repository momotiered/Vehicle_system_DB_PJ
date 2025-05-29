package com.vehicle.repair.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "RepairOrders")
public class RepairOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String descriptionOfIssue;

    @CreationTimestamp
    private LocalDateTime reportDate;

    private LocalDateTime startDate;

    private LocalDateTime estimatedCompletionDate;

    private LocalDateTime completionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepairOrderStatus status = RepairOrderStatus.PENDING_ASSIGNMENT;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalMaterialCost = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal totalLaborCost = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    private BigDecimal grandTotalCost = BigDecimal.ZERO;

    private String notes;
} 