package com.vehicle.repair.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "RepairMaterials")
public class RepairMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer materialId;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private String sku; // 库存单位
    
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;
    
    @Column(nullable = false)
    private Integer stockQuantity;
    
    private String supplier;
    
    private String category;
    
    @Column(nullable = false)
    private String unit; // 单位（个、升、千克等）
} 