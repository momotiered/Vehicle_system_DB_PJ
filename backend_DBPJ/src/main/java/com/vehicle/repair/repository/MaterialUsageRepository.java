package com.vehicle.repair.repository;

import com.vehicle.repair.entity.MaterialUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MaterialUsageRepository extends JpaRepository<MaterialUsage, Integer> {
    
    List<MaterialUsage> findByRepairOrder_OrderId(Integer orderId);
    
    List<MaterialUsage> findByMaterial_MaterialId(Integer materialId);
    
    List<MaterialUsage> findByTechnician_TechnicianId(Integer technicianId);
    
    @Query("SELECT SUM(mu.totalPrice) FROM MaterialUsage mu WHERE mu.repairOrder.orderId = ?1")
    BigDecimal calculateTotalMaterialCostByOrderId(Integer orderId);
    
    @Query("SELECT SUM(mu.quantity) FROM MaterialUsage mu WHERE mu.material.materialId = ?1")
    Double getTotalUsageByMaterialId(Integer materialId);
} 