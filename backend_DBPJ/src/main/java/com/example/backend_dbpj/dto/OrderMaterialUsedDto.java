package com.example.backend_dbpj.dto;

import com.example.backend_dbpj.entity.OrderMaterialUsed;

import java.math.BigDecimal;

public class OrderMaterialUsedDto {
    private int orderMaterialId;
    private int orderId;
    private int materialId;
    private String materialName;
    private int quantityUsed;
    private BigDecimal pricePerUnitAtTimeOfUse;
    private BigDecimal totalCost;

    public OrderMaterialUsedDto(OrderMaterialUsed omu) {
        this.orderMaterialId = omu.getOrderMaterialId();
        this.orderId = omu.getRepairOrder().getOrderId();
        this.materialId = omu.getMaterial().getMaterialId();
        this.materialName = omu.getMaterial().getMaterialName();
        this.quantityUsed = omu.getQuantityUsed();
        this.pricePerUnitAtTimeOfUse = omu.getPricePerUnitAtTimeOfUse();
        this.totalCost = omu.getPricePerUnitAtTimeOfUse().multiply(BigDecimal.valueOf(omu.getQuantityUsed()));
    }

    // Getters and Setters
    public int getOrderMaterialId() {
        return orderMaterialId;
    }

    public void setOrderMaterialId(int orderMaterialId) {
        this.orderMaterialId = orderMaterialId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
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