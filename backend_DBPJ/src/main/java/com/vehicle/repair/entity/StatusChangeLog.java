package com.vehicle.repair.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "StatusChangeLogs")
public class StatusChangeLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer logId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private RepairOrder repairOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepairOrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepairOrderStatus newStatus;

    private String changeReason;
    
    @ManyToOne
    @JoinColumn(name = "changed_by_user_id")
    private User changedByUser;

    @CreationTimestamp
    private LocalDateTime changeTime;
    
    private String notes;
} 