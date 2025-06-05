package com.vehicle.repair.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "WorkOrderAssignments")
public class WorkOrderAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer assignmentId;
    
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private RepairOrder repairOrder;
    
    @ManyToOne
    @JoinColumn(name = "technician_id", nullable = false)
    private Technician technician;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.PENDING; // 默认为待接单
    
    @CreationTimestamp
    private LocalDateTime assignedTime;
    
    private LocalDateTime acceptedTime;
    
    private LocalDateTime rejectedTime;
    
    private LocalDateTime completedTime;
    
    private String rejectionReason;
    
    private Integer laborHours; // 实际工时
    
    @Column(nullable = false)
    private Boolean isPrimary = false; // 是否为主要技术人员
} 