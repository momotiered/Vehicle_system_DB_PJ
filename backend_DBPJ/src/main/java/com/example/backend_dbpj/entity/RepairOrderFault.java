package com.example.backend_dbpj.entity;

import javax.persistence.*;

@Entity(name = "RepairOrderFaults")
public class RepairOrderFault {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_fault_id")
    private int orderFaultId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private RepairOrder repairOrder;

    @ManyToOne
    @JoinColumn(name = "fault_type_id", nullable = false)
    private FaultType faultType;


    // Getters and Setters

    public int getOrderFaultId() {
        return orderFaultId;
    }

    public void setOrderFaultId(int orderFaultId) {
        this.orderFaultId = orderFaultId;
    }

    public RepairOrder getRepairOrder() {
        return repairOrder;
    }

    public void setRepairOrder(RepairOrder repairOrder) {
        this.repairOrder = repairOrder;
    }

    public FaultType getFaultType() {
        return faultType;
    }

    public void setFaultType(FaultType faultType) {
        this.faultType = faultType;
    }
} 