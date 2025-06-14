package com.example.backend_dbpj.entity;

import javax.persistence.*;
import java.util.List;

@Entity(name = "FaultTypes")
public class FaultType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fault_type_id")
    private int faultTypeId;

    @Column(name = "fault_name", unique = true)
    private String faultName;

    private String description;

    @OneToMany(mappedBy = "faultType")
    private List<RepairOrderFault> repairOrderFaults;


    // Getters and Setters

    public int getFaultTypeId() {
        return faultTypeId;
    }

    public void setFaultTypeId(int faultTypeId) {
        this.faultTypeId = faultTypeId;
    }

    public String getFaultName() {
        return faultName;
    }

    public void setFaultName(String faultName) {
        this.faultName = faultName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<RepairOrderFault> getRepairOrderFaults() {
        return repairOrderFaults;
    }

    public void setRepairOrderFaults(List<RepairOrderFault> repairOrderFaults) {
        this.repairOrderFaults = repairOrderFaults;
    }
} 