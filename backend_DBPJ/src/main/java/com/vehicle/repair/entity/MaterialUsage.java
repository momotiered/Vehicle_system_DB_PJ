package com.vehicle.repair.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "MaterialUsages")
public class MaterialUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer usageId;
    
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private RepairOrder repairOrder;
    
    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private RepairMaterial material;
    
    @ManyToOne
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician technician;
    
    @Column(nullable = false)
    private Double quantity;
    
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;
    
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal totalPrice;
    
    private String notes;
    
    @CreationTimestamp
    private LocalDateTime usageTime;
} 