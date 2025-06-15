package com.example.backend_dbpj.repository;

import com.example.backend_dbpj.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
 
@Repository
public interface MaterialRepository extends JpaRepository<Material, Integer> {
    
    // 查找低库存材料 (库存量小于指定阈值)
    @Query("SELECT m FROM Materials m WHERE m.stockQuantity < :threshold")
    List<Material> findLowStockMaterials(@Param("threshold") int threshold);
} 