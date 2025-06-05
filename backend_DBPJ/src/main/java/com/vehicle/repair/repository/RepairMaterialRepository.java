package com.vehicle.repair.repository;

import com.vehicle.repair.entity.RepairMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepairMaterialRepository extends JpaRepository<RepairMaterial, Integer> {
    
    List<RepairMaterial> findByCategory(String category);
    
    List<RepairMaterial> findByStockQuantityLessThanEqual(Integer threshold);
    
    List<RepairMaterial> findByNameContainingIgnoreCase(String keyword);
    
    boolean existsBySku(String sku);
} 