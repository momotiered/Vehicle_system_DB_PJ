package com.example.backend_dbpj.service;

import com.example.backend_dbpj.dto.AddMaterialUsageRequestDto;
import com.example.backend_dbpj.dto.CreateRepairOrderRequest;
import com.example.backend_dbpj.dto.OrderMaterialUsedDto;
import com.example.backend_dbpj.dto.RepairOrderDto;
import com.example.backend_dbpj.entity.Material;
import com.example.backend_dbpj.entity.OrderMaterialUsed;
import com.example.backend_dbpj.entity.RepairOrder;
import com.example.backend_dbpj.entity.RepairAssignment;
import com.example.backend_dbpj.entity.RepairPersonnel;
import com.example.backend_dbpj.entity.User;
import com.example.backend_dbpj.entity.Vehicle;
import com.example.backend_dbpj.entity.enums.OrderStatus;
import com.example.backend_dbpj.entity.enums.AssignmentStatus;
import com.example.backend_dbpj.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RepairOrderService {

    @Autowired
    private RepairOrderRepository repairOrderRepository;

    @Autowired
    private RepairAssignmentRepository repairAssignmentRepository;

    @Autowired
    private RepairPersonnelRepository repairPersonnelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private OrderMaterialUsedRepository orderMaterialUsedRepository;

    public List<RepairOrder> getRepairOrdersByUserId(Integer userId) {
        return repairOrderRepository.findByUser_UserId(userId);
    }

    @Transactional
    public RepairOrder createRepairOrder(CreateRepairOrderRequest request) {
        // 1. 验证用户和车辆是否存在
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.getUserId()));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id: " + request.getVehicleId()));

        // 2. 创建新的RepairOrder实例
        RepairOrder repairOrder = new RepairOrder();
        repairOrder.setUser(user);
        repairOrder.setVehicle(vehicle);
        repairOrder.setDescriptionOfIssue(request.getDescriptionOfIssue());

        // 3. 设置初始状态和报修日期
        repairOrder.setStatus(OrderStatus.PENDING_ASSIGNMENT);
        repairOrder.setReportDate(new Timestamp(System.currentTimeMillis()));

        // 4. 初始化费用
        repairOrder.setTotalMaterialCost(BigDecimal.ZERO);
        repairOrder.setTotalLaborCost(BigDecimal.ZERO);
        repairOrder.setGrandTotalCost(BigDecimal.ZERO);

        return repairOrderRepository.save(repairOrder);
    }

    public void urgeRepairOrder(Integer orderId) {
        // 1. 验证工单是否存在
        RepairOrder repairOrder = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("RepairOrder not found with id: " + orderId));

        // 2. 检查工单状态是否允许催单
        if (repairOrder.getStatus() != OrderStatus.IN_PROGRESS && repairOrder.getStatus() != OrderStatus.ASSIGNED) {
            throw new IllegalStateException("Order status does not allow reminder.");
        }

        System.out.println("用户对工单 #" + orderId + " 进行了催单。");
        // 3. (可选) 可以添加一个催单记录表来记录催单历史
    }

    @Transactional
    public OrderMaterialUsedDto addMaterialToOrder(int orderId, AddMaterialUsageRequestDto requestDto) {
        // 1. 查找工单和材料
        RepairOrder order = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("工单不存在，ID: " + orderId));
        Material material = materialRepository.findById(requestDto.getMaterialId())
                .orElseThrow(() -> new RuntimeException("材料不存在，ID: " + requestDto.getMaterialId()));

        int quantityToUse = requestDto.getQuantity();

        // 2. 检查库存
        if (material.getStockQuantity() < quantityToUse) {
            throw new RuntimeException("材料库存不足: " + material.getMaterialName());
        }

        // 3. 创建材料使用记录
        OrderMaterialUsed newUsage = new OrderMaterialUsed();
        newUsage.setRepairOrder(order);
        newUsage.setMaterial(material);
        newUsage.setQuantityUsed(quantityToUse);
        newUsage.setPricePerUnitAtTimeOfUse(material.getUnitPrice());
        orderMaterialUsedRepository.save(newUsage);

        // 4. 更新材料库存
        material.setStockQuantity(material.getStockQuantity() - quantityToUse);
        materialRepository.save(material);

        // 5. 更新工单总费用
        BigDecimal materialCost = material.getUnitPrice().multiply(BigDecimal.valueOf(quantityToUse));
        
        // 确保费用字段不为null
        if (order.getTotalMaterialCost() == null) {
            order.setTotalMaterialCost(BigDecimal.ZERO);
        }
        if (order.getGrandTotalCost() == null) {
            order.setGrandTotalCost(BigDecimal.ZERO);
        }
        
        order.setTotalMaterialCost(order.getTotalMaterialCost().add(materialCost));
        order.setGrandTotalCost(order.getGrandTotalCost().add(materialCost));
        repairOrderRepository.save(order);

        // 6. 返回创建的记录DTO
        return new OrderMaterialUsedDto(newUsage);
    }

    public RepairOrder getOrderById(int orderId) {
        return repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("工单不存在，ID: " + orderId));
    }

    public List<RepairOrderDto> getPendingOrders() {
        List<RepairOrder> orders = repairOrderRepository.findByStatus(OrderStatus.PENDING_ASSIGNMENT);
        return orders.stream()
                .map(RepairOrderDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignOrderToPersonnel(int orderId, List<Integer> personnelIds) {
        // 1. 验证工单
        RepairOrder order = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("工单不存在，ID: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING_ASSIGNMENT) {
            throw new RuntimeException("工单状态不是'待分配'，无法分配");
        }

        if (personnelIds == null || personnelIds.isEmpty()) {
            throw new RuntimeException("必须至少指定一名维修人员");
        }

        // 2. 验证并创建分配记录
        for (Integer personnelId : personnelIds) {
            RepairPersonnel personnel = repairPersonnelRepository.findById(personnelId)
                    .orElseThrow(() -> new RuntimeException("维修人员不存在，ID: " + personnelId));

            if (!personnel.isActive()) {
                throw new RuntimeException("维修人员 " + personnel.getFullName() + " 已离职，无法分配");
            }

            RepairAssignment assignment = new RepairAssignment();
            assignment.setRepairOrder(order);
            assignment.setRepairPersonnel(personnel);
            assignment.setAssignmentDate(new Timestamp(System.currentTimeMillis()));
            // 状态默认为 Assigned, 在实体类中已定义, 但需要显式设置
            assignment.setStatus(AssignmentStatus.Assigned);
            repairAssignmentRepository.save(assignment);
        }

        // 3. 更新工单状态
        order.setStatus(OrderStatus.ASSIGNED);
        repairOrderRepository.save(order);
    }
} 