package com.vehicle.repair.repository;

import com.vehicle.repair.entity.AssignmentStatus;
import com.vehicle.repair.entity.WorkOrderAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkOrderAssignmentRepository extends JpaRepository<WorkOrderAssignment, Integer> {
    
    List<WorkOrderAssignment> findByRepairOrder_OrderId(Integer orderId);
    
    List<WorkOrderAssignment> findByTechnician_TechnicianId(Integer technicianId);
    
    List<WorkOrderAssignment> findByTechnician_TechnicianIdAndStatus(Integer technicianId, AssignmentStatus status);
    
    List<WorkOrderAssignment> findByRepairOrder_OrderIdAndStatus(Integer orderId, AssignmentStatus status);
    
    @Query("SELECT wa FROM WorkOrderAssignment wa WHERE wa.repairOrder.orderId = ?1 AND wa.isPrimary = true")
    List<WorkOrderAssignment> findPrimaryAssignmentByOrderId(Integer orderId);
    
    boolean existsByRepairOrder_OrderIdAndTechnician_TechnicianId(Integer orderId, Integer technicianId);
    
    @Query("SELECT COUNT(wa) FROM WorkOrderAssignment wa WHERE wa.technician.technicianId = ?1 AND wa.status IN ('PENDING', 'ACCEPTED', 'IN_PROGRESS')")
    int countActiveAssignmentsByTechnicianId(Integer technicianId);
} 