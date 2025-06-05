package com.vehicle.repair.service;

import com.vehicle.repair.entity.MaterialUsage;
import com.vehicle.repair.entity.RepairMaterial;
import com.vehicle.repair.entity.RepairOrder;
import com.vehicle.repair.entity.Technician;
import com.vehicle.repair.repository.MaterialUsageRepository;
import com.vehicle.repair.repository.RepairMaterialRepository;
import com.vehicle.repair.repository.RepairOrderRepository;
import com.vehicle.repair.repository.TechnicianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class MaterialUsageService {

    @Autowired
    private MaterialUsageRepository materialUsageRepository;
    
    @Autowired
    private RepairOrderRepository repairOrderRepository;
    
    @Autowired
    private RepairMaterialRepository materialRepository;
    
    @Autowired
    private TechnicianRepository technicianRepository;
    
    @Autowired
    private RepairMaterialService materialService;
    
    /**
     * 获取工单的所有材料使用记录
     */
    public List<MaterialUsage> getUsagesByOrderId(Integer orderId) {
        return materialUsageRepository.findByRepairOrder_OrderId(orderId);
    }
    
    /**
     * 获取材料的所有使用记录
     */
    public List<MaterialUsage> getUsagesByMaterialId(Integer materialId) {
        return materialUsageRepository.findByMaterial_MaterialId(materialId);
    }
    
    /**
     * 获取技术人员的所有材料使用记录
     */
    public List<MaterialUsage> getUsagesByTechnicianId(Integer technicianId) {
        return materialUsageRepository.findByTechnician_TechnicianId(technicianId);
    }
    
    /**
     * 计算工单的材料总成本
     */
    public BigDecimal calculateTotalMaterialCost(Integer orderId) {
        BigDecimal totalCost = materialUsageRepository.calculateTotalMaterialCostByOrderId(orderId);
        return totalCost != null ? totalCost : BigDecimal.ZERO;
    }
    
    /**
     * 创建材料使用记录
     */
    @Transactional
    public MaterialUsage createMaterialUsage(MaterialUsage usage, Integer orderId, Integer materialId, Integer technicianId) {
        // 验证工单、材料和技术人员
        RepairOrder repairOrder = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("维修工单不存在"));
        
        RepairMaterial material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("材料不存在"));
        
        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("技术人员不存在"));
        
        // 检查库存是否足够
        if (material.getStockQuantity() < usage.getQuantity()) {
            throw new RuntimeException("材料库存不足");
        }
        
        // 设置关联实体
        usage.setRepairOrder(repairOrder);
        usage.setMaterial(material);
        usage.setTechnician(technician);
        
        // 设置单价
        usage.setUnitPrice(material.getUnitPrice());
        
        // 计算总价
        BigDecimal totalPrice = material.getUnitPrice().multiply(BigDecimal.valueOf(usage.getQuantity()));
        usage.setTotalPrice(totalPrice);
        
        // 保存使用记录
        MaterialUsage savedUsage = materialUsageRepository.save(usage);
        
        // 减少库存
        materialService.decreaseStock(materialId, usage.getQuantity().intValue());
        
        // 更新工单的材料总成本
        updateOrderMaterialCost(orderId);
        
        return savedUsage;
    }
    
    /**
     * 更新材料使用记录
     */
    @Transactional
    public MaterialUsage updateMaterialUsage(MaterialUsage usage) {
        MaterialUsage existingUsage = materialUsageRepository.findById(usage.getUsageId())
                .orElseThrow(() -> new RuntimeException("材料使用记录不存在"));
        
        RepairMaterial material = existingUsage.getMaterial();
        
        // 计算数量差异
        double quantityDiff = 0;
        if (usage.getQuantity() != null && usage.getQuantity() != existingUsage.getQuantity()) {
            quantityDiff = usage.getQuantity() - existingUsage.getQuantity();
            
            // 如果需要增加材料使用量，检查库存
            if (quantityDiff > 0 && material.getStockQuantity() < quantityDiff) {
                throw new RuntimeException("材料库存不足");
            }
            
            existingUsage.setQuantity(usage.getQuantity());
            
            // 计算新的总价
            existingUsage.setTotalPrice(material.getUnitPrice().multiply(BigDecimal.valueOf(usage.getQuantity())));
            
            // 更新库存
            if (quantityDiff > 0) {
                materialService.decreaseStock(material.getMaterialId(), (int) quantityDiff);
            } else if (quantityDiff < 0) {
                materialService.increaseStock(material.getMaterialId(), (int) Math.abs(quantityDiff));
            }
        }
        
        // 更新备注
        if (usage.getNotes() != null) {
            existingUsage.setNotes(usage.getNotes());
        }
        
        // 保存更新
        MaterialUsage updatedUsage = materialUsageRepository.save(existingUsage);
        
        // 更新工单的材料总成本
        updateOrderMaterialCost(existingUsage.getRepairOrder().getOrderId());
        
        return updatedUsage;
    }
    
    /**
     * 删除材料使用记录
     */
    @Transactional
    public void deleteMaterialUsage(Integer usageId) {
        Optional<MaterialUsage> optionalUsage = materialUsageRepository.findById(usageId);
        
        if (!optionalUsage.isPresent()) {
            throw new RuntimeException("材料使用记录不存在");
        }
        
        MaterialUsage usage = optionalUsage.get();
        Integer orderId = usage.getRepairOrder().getOrderId();
        
        // 恢复库存
        materialService.increaseStock(
                usage.getMaterial().getMaterialId(), 
                usage.getQuantity().intValue()
        );
        
        // 删除记录
        materialUsageRepository.deleteById(usageId);
        
        // 更新工单的材料总成本
        updateOrderMaterialCost(orderId);
    }
    
    /**
     * 更新工单的材料总成本
     */
    private void updateOrderMaterialCost(Integer orderId) {
        RepairOrder repairOrder = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("维修工单不存在"));
        
        BigDecimal totalMaterialCost = calculateTotalMaterialCost(orderId);
        repairOrder.setTotalMaterialCost(totalMaterialCost);
        
        // 重新计算总费用
        BigDecimal laborCost = repairOrder.getTotalLaborCost() != null ? repairOrder.getTotalLaborCost() : BigDecimal.ZERO;
        repairOrder.setGrandTotalCost(totalMaterialCost.add(laborCost));
        
        repairOrderRepository.save(repairOrder);
    }
} 