package com.vehicle.repair.service;

import com.vehicle.repair.entity.RepairMaterial;
import com.vehicle.repair.repository.RepairMaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RepairMaterialService {

    @Autowired
    private RepairMaterialRepository materialRepository;
    
    /**
     * 获取所有材料
     */
    public List<RepairMaterial> getAllMaterials() {
        return materialRepository.findAll();
    }
    
    /**
     * 根据ID获取材料
     */
    public Optional<RepairMaterial> getMaterialById(Integer materialId) {
        return materialRepository.findById(materialId);
    }
    
    /**
     * 根据类别获取材料
     */
    public List<RepairMaterial> getMaterialsByCategory(String category) {
        return materialRepository.findByCategory(category);
    }
    
    /**
     * 获取库存低于阈值的材料
     */
    public List<RepairMaterial> getLowStockMaterials(Integer threshold) {
        return materialRepository.findByStockQuantityLessThanEqual(threshold);
    }
    
    /**
     * 搜索材料
     */
    public List<RepairMaterial> searchMaterials(String keyword) {
        return materialRepository.findByNameContainingIgnoreCase(keyword);
    }
    
    /**
     * 创建新材料
     */
    @Transactional
    public RepairMaterial createMaterial(RepairMaterial material) {
        if (materialRepository.existsBySku(material.getSku())) {
            throw new RuntimeException("该SKU已存在");
        }
        
        return materialRepository.save(material);
    }
    
    /**
     * 更新材料信息
     */
    @Transactional
    public RepairMaterial updateMaterial(RepairMaterial material) {
        RepairMaterial existingMaterial = materialRepository.findById(material.getMaterialId())
                .orElseThrow(() -> new RuntimeException("材料不存在"));
        
        // 更新信息
        if (material.getName() != null) {
            existingMaterial.setName(material.getName());
        }
        if (material.getDescription() != null) {
            existingMaterial.setDescription(material.getDescription());
        }
        if (material.getSku() != null && !material.getSku().equals(existingMaterial.getSku())) {
            if (materialRepository.existsBySku(material.getSku())) {
                throw new RuntimeException("该SKU已存在");
            }
            existingMaterial.setSku(material.getSku());
        }
        if (material.getUnitPrice() != null) {
            existingMaterial.setUnitPrice(material.getUnitPrice());
        }
        if (material.getStockQuantity() != null) {
            existingMaterial.setStockQuantity(material.getStockQuantity());
        }
        if (material.getSupplier() != null) {
            existingMaterial.setSupplier(material.getSupplier());
        }
        if (material.getCategory() != null) {
            existingMaterial.setCategory(material.getCategory());
        }
        if (material.getUnit() != null) {
            existingMaterial.setUnit(material.getUnit());
        }
        
        return materialRepository.save(existingMaterial);
    }
    
    /**
     * 更新库存数量
     */
    @Transactional
    public RepairMaterial updateStock(Integer materialId, Integer quantity) {
        RepairMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("材料不存在"));
        
        material.setStockQuantity(quantity);
        return materialRepository.save(material);
    }
    
    /**
     * 增加库存数量
     */
    @Transactional
    public RepairMaterial increaseStock(Integer materialId, Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("增加的数量必须大于0");
        }
        
        RepairMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("材料不存在"));
        
        material.setStockQuantity(material.getStockQuantity() + amount);
        return materialRepository.save(material);
    }
    
    /**
     * 减少库存数量
     */
    @Transactional
    public RepairMaterial decreaseStock(Integer materialId, Integer amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("减少的数量必须大于0");
        }
        
        RepairMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("材料不存在"));
        
        if (material.getStockQuantity() < amount) {
            throw new RuntimeException("库存不足");
        }
        
        material.setStockQuantity(material.getStockQuantity() - amount);
        return materialRepository.save(material);
    }
    
    /**
     * 删除材料
     */
    @Transactional
    public void deleteMaterial(Integer materialId) {
        if (!materialRepository.existsById(materialId)) {
            throw new RuntimeException("材料不存在");
        }
        
        materialRepository.deleteById(materialId);
    }
} 