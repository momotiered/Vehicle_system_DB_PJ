package com.example.backend_dbpj.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity(name = "Materials")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_id")
    private int materialId;

    @Column(name = "material_name", unique = true)
    private String materialName;

    private String description;

    @Column(name = "stock_quantity")
    private int stockQuantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @OneToMany(mappedBy = "material")
    private List<OrderMaterialUsed> orderMaterialsUsed;


    // Getters and Setters

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public List<OrderMaterialUsed> getOrderMaterialsUsed() {
        return orderMaterialsUsed;
    }

    public void setOrderMaterialsUsed(List<OrderMaterialUsed> orderMaterialsUsed) {
        this.orderMaterialsUsed = orderMaterialsUsed;
    }
} 