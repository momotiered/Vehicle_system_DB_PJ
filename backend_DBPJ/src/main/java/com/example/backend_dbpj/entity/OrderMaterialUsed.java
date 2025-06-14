package com.example.backend_dbpj.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity(name = "OrderMaterialsUsed")
public class OrderMaterialUsed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_material_id")
    private int orderMaterialId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference("repairOrder-materials")
    private RepairOrder repairOrder;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "quantity_used")
    private int quantityUsed;

    @Column(name = "price_per_unit_at_time_of_use")
    private BigDecimal pricePerUnitAtTimeOfUse;

    @Column(name = "total_cost", insertable = false, updatable = false)
    private BigDecimal totalCost;


    // Getters and Setters

    public int getOrderMaterialId() {
        return orderMaterialId;
    }

    public void setOrderMaterialId(int orderMaterialId) {
        this.orderMaterialId = orderMaterialId;
    }

    public RepairOrder getRepairOrder() {
        return repairOrder;
    }

    public void setRepairOrder(RepairOrder repairOrder) {
        this.repairOrder = repairOrder;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public int getQuantityUsed() {
        return quantityUsed;
    }

    public void setQuantityUsed(int quantityUsed) {
        this.quantityUsed = quantityUsed;
    }

    public BigDecimal getPricePerUnitAtTimeOfUse() {
        return pricePerUnitAtTimeOfUse;
    }

    public void setPricePerUnitAtTimeOfUse(BigDecimal pricePerUnitAtTimeOfUse) {
        this.pricePerUnitAtTimeOfUse = pricePerUnitAtTimeOfUse;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
} 