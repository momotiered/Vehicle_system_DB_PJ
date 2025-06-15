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
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;

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

    public List<RepairOrder> getAllOrders() {
        return repairOrderRepository.findAll();
    }

    @Transactional
    public RepairOrder createRepairOrder(CreateRepairOrderRequest request) {
        // 1. 验证用户和车辆
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + request.getUserId()));
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("车辆不存在，ID: " + request.getVehicleId()));

        // 2. 创建工单
        RepairOrder order = new RepairOrder();
        order.setUser(user);
        order.setVehicle(vehicle);
        order.setDescriptionOfIssue(request.getDescriptionOfIssue());
        order.setStatus(OrderStatus.PENDING_ASSIGNMENT);
        order.setReportDate(new Timestamp(System.currentTimeMillis()));
        
        // 3. 保存工单
        return repairOrderRepository.save(order);
    }

    @Transactional
    public RepairOrder assignRepairOrder(Integer orderId, List<Integer> personnelIds) {
        // 1. 验证工单
        RepairOrder order = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("工单不存在，ID: " + orderId));
        
        if (order.getStatus() != OrderStatus.PENDING_ASSIGNMENT) {
            throw new RuntimeException("工单状态不允许分配，当前状态: " + order.getStatus());
        }
        
        // 2. 验证并创建分配记录
        for (Integer personnelId : personnelIds) {
            RepairPersonnel personnel = repairPersonnelRepository.findById(personnelId)
                    .orElseThrow(() -> new RuntimeException("维修人员不存在，ID: " + personnelId));

            RepairAssignment assignment = new RepairAssignment();
            assignment.setRepairOrder(order);
            assignment.setRepairPersonnel(personnel);
            assignment.setStatus(AssignmentStatus.Assigned);
            assignment.setAssignmentDate(new Timestamp(System.currentTimeMillis()));

            repairAssignmentRepository.save(assignment);
        }

        // 3. 更新工单状态
        order.setStatus(OrderStatus.ASSIGNED);
        repairOrderRepository.save(order);
        
        return order;
    }

    public void urgeRepairOrder(Integer orderId) {
        // 1. 验证工单是否存在
        RepairOrder repairOrder = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("RepairOrder not found with id: " + orderId));

        // 2. 检查工单状态是否允许催单
        if (repairOrder.getStatus() != OrderStatus.PENDING_ASSIGNMENT && 
            repairOrder.getStatus() != OrderStatus.IN_PROGRESS && 
            repairOrder.getStatus() != OrderStatus.ASSIGNED) {
            throw new IllegalStateException("当前工单状态不允许催单。");
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

            RepairAssignment assignment = new RepairAssignment();
            assignment.setRepairOrder(order);
            assignment.setRepairPersonnel(personnel);
            assignment.setStatus(AssignmentStatus.Assigned);
            assignment.setAssignmentDate(new Timestamp(System.currentTimeMillis()));

            repairAssignmentRepository.save(assignment);
        }

        // 3. 更新工单状态
        order.setStatus(OrderStatus.ASSIGNED);
        repairOrderRepository.save(order);
    }

    // --- 监控统计方法 ---
    
    public Map<String, Object> getOverviewStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 今日工单统计  
        LocalDate today = LocalDate.now();
        Long todayOrders = repairOrderRepository.countByReportDateBetween(
            java.sql.Date.valueOf(today),
            java.sql.Date.valueOf(today.plusDays(1))
        );
        
        // 各状态工单数量
        Long assignedOrders = repairOrderRepository.countByStatus(OrderStatus.ASSIGNED);
        Long inProgressOrders = repairOrderRepository.countByStatus(OrderStatus.IN_PROGRESS);
        Long completedOrders = repairOrderRepository.countByStatus(OrderStatus.COMPLETED);
        
        // 总用户数
        Long totalUsers = userRepository.count();
        
        // 在职人员数
        Long activePersonnel = repairPersonnelRepository.countByIsActive(true);
        
        stats.put("todayOrders", todayOrders);
        stats.put("assignedOrders", assignedOrders);
        stats.put("inProgressOrders", inProgressOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("totalUsers", totalUsers);
        stats.put("activePersonnel", activePersonnel);
        
        return stats;
    }
    
    public Map<String, Object> getOrderStatusStats() {
        Map<String, Object> stats = new HashMap<>();
        
        for (OrderStatus status : OrderStatus.values()) {
            Long count = repairOrderRepository.countByStatus(status);
            stats.put(status.name(), count);
        }
        
        return stats;
    }
    
    public Map<String, Object> getFinancialStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 本月完成的工单统计
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1);
        
        List<RepairOrder> completedOrders = repairOrderRepository.findCompletedOrdersInDateRange(
            java.sql.Date.valueOf(startOfMonth),
            java.sql.Date.valueOf(endOfMonth),
            OrderStatus.COMPLETED
        );
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal materialRevenue = BigDecimal.ZERO;
        BigDecimal laborRevenue = BigDecimal.ZERO;
        BigDecimal maxOrderValue = BigDecimal.ZERO;
        
        for (RepairOrder order : completedOrders) {
            if (order.getGrandTotalCost() != null) {
                totalRevenue = totalRevenue.add(order.getGrandTotalCost());
                if (order.getGrandTotalCost().compareTo(maxOrderValue) > 0) {
                    maxOrderValue = order.getGrandTotalCost();
                }
            }
            if (order.getTotalMaterialCost() != null) {
                materialRevenue = materialRevenue.add(order.getTotalMaterialCost());
            }
            if (order.getTotalLaborCost() != null) {
                laborRevenue = laborRevenue.add(order.getTotalLaborCost());
            }
        }
        
        BigDecimal avgOrderValue = completedOrders.isEmpty() ? BigDecimal.ZERO : 
            totalRevenue.divide(BigDecimal.valueOf(completedOrders.size()), 2, BigDecimal.ROUND_HALF_UP);
        
        stats.put("totalRevenue", totalRevenue);
        stats.put("materialRevenue", materialRevenue);
        stats.put("laborRevenue", laborRevenue);
        stats.put("avgOrderValue", avgOrderValue);
        stats.put("maxOrderValue", maxOrderValue);
        stats.put("completedOrdersCount", completedOrders.size());
        
        return stats;
    }
} 