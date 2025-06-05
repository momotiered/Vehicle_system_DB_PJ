package com.vehicle.repair.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "Technicians")
public class Technician {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer technicianId;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillType skillType;
    
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal hourlyRate; // 时薪
    
    private Integer yearsOfExperience; // 工作经验年限
    
    private String certifications; // 持有的资格证书
    
    @Column(nullable = false)
    private Boolean isAvailable = true; // 是否可接单
    
    @Column(nullable = false)
    private Integer maxWorkOrdersPerDay = 5; // 每日最大接单数
    
    @Column(nullable = false)
    private Integer currentWorkOrders = 0; // 当前处理中的工单数
} 