package com.vehicle.repair.service;

import com.vehicle.repair.entity.*;
import com.vehicle.repair.repository.RepairOrderRepository;
import com.vehicle.repair.repository.TechnicianRepository;
import com.vehicle.repair.repository.WorkOrderAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkOrderAssignmentService {

    @Autowired
    private WorkOrderAssignmentRepository assignmentRepository;
    
    @Autowired
    private RepairOrderRepository repairOrderRepository;
    
    @Autowired
    private TechnicianRepository technicianRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 获取工单的所有分配
     */
    public List<WorkOrderAssignment> getAssignmentsByOrderId(Integer orderId) {
        return assignmentRepository.findByRepairOrder_OrderId(orderId);
    }
    
    /**
     * 获取技术人员的所有分配
     */
    public List<WorkOrderAssignment> getAssignmentsByTechnicianId(Integer technicianId) {
        return assignmentRepository.findByTechnician_TechnicianId(technicianId);
    }
    
    /**
     * 获取特定状态的技术人员分配
     */
    public List<WorkOrderAssignment> getTechnicianAssignmentsByStatus(Integer technicianId, AssignmentStatus status) {
        return assignmentRepository.findByTechnician_TechnicianIdAndStatus(technicianId, status);
    }
    
    /**
     * 获取特定状态的工单分配
     */
    public List<WorkOrderAssignment> getAssignmentsByRepairOrderAndStatus(Integer orderId, AssignmentStatus status) {
        return assignmentRepository.findByRepairOrder_OrderIdAndStatus(orderId, status);
    }
    
    /**
     * 获取工单的主要技术人员分配
     */
    public Optional<WorkOrderAssignment> getPrimaryAssignmentByOrderId(Integer orderId) {
        List<WorkOrderAssignment> primaryAssignments = assignmentRepository.findPrimaryAssignmentByOrderId(orderId);
        return primaryAssignments.isEmpty() ? Optional.empty() : Optional.of(primaryAssignments.get(0));
    }
    
    /**
     * 计算技术人员当前活跃工单数
     */
    public int countActiveTechnicianAssignments(Integer technicianId) {
        return assignmentRepository.countActiveAssignmentsByTechnicianId(technicianId);
    }
    
    /**
     * 创建工单分配
     */
    @Transactional
    public WorkOrderAssignment createAssignment(Integer orderId, Integer technicianId, boolean isPrimary) {
        // 验证工单和技术人员
        RepairOrder repairOrder = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("维修工单不存在"));
        
        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("技术人员不存在"));
        
        // 检查是否已存在分配
        if (assignmentRepository.existsByRepairOrder_OrderIdAndTechnician_TechnicianId(orderId, technicianId)) {
            throw new RuntimeException("该技术人员已被分配到此工单");
        }
        
        // 创建分配
        WorkOrderAssignment assignment = new WorkOrderAssignment();
        assignment.setRepairOrder(repairOrder);
        assignment.setTechnician(technician);
        assignment.setIsPrimary(isPrimary);
        
        // 保存分配
        WorkOrderAssignment savedAssignment = assignmentRepository.save(assignment);
        
        // 更新技术人员的工单计数
        int activeCount = countActiveTechnicianAssignments(technicianId);
        technician.setCurrentWorkOrders(activeCount);
        technicianRepository.save(technician);
        
        // 更新工单状态
        if (repairOrder.getStatus() == RepairOrderStatus.PENDING_ASSIGNMENT) {
            repairOrder.setStatus(RepairOrderStatus.ASSIGNED);
            repairOrderRepository.save(repairOrder);
        }
        
        // 发送通知给技术人员
        notificationService.createSystemMessage(
                technician.getUser().getUserId(),
                "新工单分配",
                String.format("您有一个新的维修工单 #%d 需要处理", orderId)
        );
        
        return savedAssignment;
    }
    
    /**
     * 接受工单分配
     */
    @Transactional
    public WorkOrderAssignment acceptAssignment(Integer assignmentId) {
        WorkOrderAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("工单分配不存在"));
        
        if (assignment.getStatus() != AssignmentStatus.PENDING) {
            throw new RuntimeException("只能接受待处理的工单");
        }
        
        // 更新分配状态
        assignment.setStatus(AssignmentStatus.ACCEPTED);
        assignment.setAcceptedTime(LocalDateTime.now());
        
        // 更新工单状态
        RepairOrder repairOrder = assignment.getRepairOrder();
        if (repairOrder.getStatus() == RepairOrderStatus.PENDING_ASSIGNMENT || 
            repairOrder.getStatus() == RepairOrderStatus.ASSIGNED) {
            repairOrder.setStatus(RepairOrderStatus.IN_PROGRESS);
            repairOrderRepository.save(repairOrder);
        }
        
        // 保存更新
        return assignmentRepository.save(assignment);
    }
    
    /**
     * 拒绝工单分配
     */
    @Transactional
    public WorkOrderAssignment rejectAssignment(Integer assignmentId, String reason) {
        WorkOrderAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("工单分配不存在"));
        
        if (assignment.getStatus() != AssignmentStatus.PENDING) {
            throw new RuntimeException("只能拒绝待处理的工单");
        }
        
        // 更新分配状态
        assignment.setStatus(AssignmentStatus.REJECTED);
        assignment.setRejectedTime(LocalDateTime.now());
        assignment.setRejectionReason(reason);
        
        // 保存更新
        WorkOrderAssignment rejectedAssignment = assignmentRepository.save(assignment);
        
        // 更新技术人员工单计数
        Technician technician = assignment.getTechnician();
        int activeCount = countActiveTechnicianAssignments(technician.getTechnicianId());
        technician.setCurrentWorkOrders(activeCount);
        technicianRepository.save(technician);
        
        // 自动重新分配工单
        autoReassignOrder(assignment.getRepairOrder().getOrderId());
        
        return rejectedAssignment;
    }
    
    /**
     * 完成工单分配
     */
    @Transactional
    public WorkOrderAssignment completeAssignment(Integer assignmentId, Integer laborHours) {
        WorkOrderAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("工单分配不存在"));
        
        if (assignment.getStatus() != AssignmentStatus.ACCEPTED && 
            assignment.getStatus() != AssignmentStatus.IN_PROGRESS) {
            throw new RuntimeException("只能完成已接受或进行中的工单");
        }
        
        // 更新分配状态
        assignment.setStatus(AssignmentStatus.COMPLETED);
        assignment.setCompletedTime(LocalDateTime.now());
        assignment.setLaborHours(laborHours);
        
        // 保存更新
        WorkOrderAssignment completedAssignment = assignmentRepository.save(assignment);
        
        // 更新技术人员工单计数
        Technician technician = assignment.getTechnician();
        int activeCount = countActiveTechnicianAssignments(technician.getTechnicianId());
        technician.setCurrentWorkOrders(activeCount);
        technicianRepository.save(technician);
        
        // 检查是否所有分配都已完成
        checkOrderCompletion(assignment.getRepairOrder().getOrderId());
        
        return completedAssignment;
    }
    
    /**
     * 自动重新分配工单
     */
    @Transactional
    public void autoReassignOrder(Integer orderId) {
        RepairOrder repairOrder = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("维修工单不存在"));
        
        // 找出拒绝分配的技术人员ID列表
        List<Integer> rejectedTechnicianIds = assignmentRepository
                .findByRepairOrder_OrderIdAndStatus(orderId, AssignmentStatus.REJECTED)
                .stream()
                .map(a -> a.getTechnician().getTechnicianId())
                .collect(Collectors.toList());
        
        // 查找合适的技术人员（排除已拒绝的）
        List<Technician> availableTechnicians = technicianRepository.findAvailableTechnicians()
                .stream()
                .filter(t -> !rejectedTechnicianIds.contains(t.getTechnicianId()))
                .collect(Collectors.toList());
        
        // 如果找到可用技术人员，创建新分配
        if (!availableTechnicians.isEmpty()) {
            Technician newTechnician = availableTechnicians.get(0); // 简单选择第一个可用的
            createAssignment(orderId, newTechnician.getTechnicianId(), true);
        } else {
            // 如果没有可用技术人员，将工单状态设置为等待分配
            repairOrder.setStatus(RepairOrderStatus.PENDING_ASSIGNMENT);
            repairOrderRepository.save(repairOrder);
        }
    }
    
    /**
     * 检查工单是否所有分配都已完成
     */
    private void checkOrderCompletion(Integer orderId) {
        List<WorkOrderAssignment> allAssignments = assignmentRepository.findByRepairOrder_OrderId(orderId);
        
        // 检查是否所有分配都已完成或拒绝
        boolean allCompleted = allAssignments.stream()
                .allMatch(a -> a.getStatus() == AssignmentStatus.COMPLETED || 
                               a.getStatus() == AssignmentStatus.REJECTED);
        
        // 如果所有分配都已处理，且至少有一个完成的分配，则将工单标记为已完成
        boolean hasCompletedAssignment = allAssignments.stream()
                .anyMatch(a -> a.getStatus() == AssignmentStatus.COMPLETED);
        
        if (allCompleted && hasCompletedAssignment) {
            RepairOrder repairOrder = repairOrderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("维修工单不存在"));
            
            repairOrder.setStatus(RepairOrderStatus.COMPLETED);
            repairOrder.setCompletionDate(LocalDateTime.now());
            repairOrderRepository.save(repairOrder);
            
            // 发送通知给用户
            notificationService.createCompletionNotification(
                    repairOrder.getUser().getUserId(),
                    orderId
            );
        }
    }
} 