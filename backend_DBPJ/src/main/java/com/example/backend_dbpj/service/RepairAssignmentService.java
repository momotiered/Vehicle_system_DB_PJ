package com.example.backend_dbpj.service;

import com.example.backend_dbpj.dto.RepairAssignmentDto;
import com.example.backend_dbpj.entity.RepairAssignment;
import com.example.backend_dbpj.entity.RepairOrder;
import com.example.backend_dbpj.entity.RepairPersonnel;
import com.example.backend_dbpj.entity.enums.AssignmentStatus;
import com.example.backend_dbpj.entity.enums.OrderStatus;
import com.example.backend_dbpj.repository.RepairAssignmentRepository;
import com.example.backend_dbpj.repository.RepairOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class RepairAssignmentService {

    @Autowired
    private RepairAssignmentRepository repairAssignmentRepository;

    @Autowired
    private RepairOrderRepository repairOrderRepository;

    @Transactional
    public RepairAssignmentDto updateAssignmentStatus(int assignmentId, AssignmentStatus newStatus) {
        RepairAssignment assignment = repairAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("工单分配不存在，ID: " + assignmentId));

        assignment.setStatus(newStatus);

        RepairOrder order = assignment.getRepairOrder();

        if (newStatus == AssignmentStatus.ACCEPTED) {
            order.setStatus(OrderStatus.IN_PROGRESS);
            order.setStartDate(new Date());
        } else if (newStatus == AssignmentStatus.REJECTED) {
            order.setStatus(OrderStatus.PENDING_ASSIGNMENT);
        }

        repairOrderRepository.save(order);
        RepairAssignment updatedAssignment = repairAssignmentRepository.save(assignment);

        return new RepairAssignmentDto(updatedAssignment);
    }

    @Transactional
    public RepairAssignmentDto logHoursToAssignment(int assignmentId, BigDecimal hoursToAdd) {
        // 1. 查找工单分配
        RepairAssignment assignment = repairAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("工单分配不存在，ID: " + assignmentId));

        // 2. 更新工单分配的工时和费用
        BigDecimal currentHours = assignment.getHoursWorked() != null ? assignment.getHoursWorked() : BigDecimal.ZERO;
        assignment.setHoursWorked(currentHours.add(hoursToAdd));

        RepairPersonnel personnel = assignment.getRepairPersonnel();
        BigDecimal costForThisLog = hoursToAdd.multiply(personnel.getHourlyRate());
        
        BigDecimal currentCost = assignment.getLaborCostForPersonnel() != null ? assignment.getLaborCostForPersonnel() : BigDecimal.ZERO;
        assignment.setLaborCostForPersonnel(currentCost.add(costForThisLog));

        // 3. 联动更新工单的总费用
        RepairOrder order = assignment.getRepairOrder();
        order.setTotalLaborCost(order.getTotalLaborCost().add(costForThisLog));
        order.setGrandTotalCost(order.getGrandTotalCost().add(costForThisLog));

        // 4. 保存所有变更
        repairOrderRepository.save(order);
        RepairAssignment updatedAssignment = repairAssignmentRepository.save(assignment);

        return new RepairAssignmentDto(updatedAssignment);
    }
} 