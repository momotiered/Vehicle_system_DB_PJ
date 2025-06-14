package com.example.backend_dbpj.entity;

import com.example.backend_dbpj.entity.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "RepairOrders")
public class RepairOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private int orderId;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonManagedReference("repairOrder-vehicle")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonManagedReference("repairOrder-user")
    private User user;

    @Column(name = "description_of_issue")
    private String descriptionOfIssue;

    @Column(name = "report_date")
    private Timestamp reportDate;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "estimated_completion_date")
    private Date estimatedCompletionDate;

    @Column(name = "completion_date")
    private Date completionDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "total_material_cost")
    private BigDecimal totalMaterialCost;

    @Column(name = "total_labor_cost")
    private BigDecimal totalLaborCost;

    @Column(name = "grand_total_cost")
    private BigDecimal grandTotalCost;

    private String notes;

    @OneToMany(mappedBy = "repairOrder")
    @JsonBackReference("repairOrder-assignments")
    private List<RepairAssignment> repairAssignments;

    @OneToMany(mappedBy = "repairOrder")
    @JsonBackReference("repairOrder-materials")
    private List<OrderMaterialUsed> orderMaterialsUsed;

    @OneToMany(mappedBy = "repairOrder")
    @JsonBackReference("repairOrder-faults")
    private List<RepairOrderFault> repairOrderFaults;

    @OneToOne(mappedBy = "repairOrder")
    @JsonBackReference("repairOrder-feedback")
    private Feedback feedback;


    // Getters and Setters

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDescriptionOfIssue() {
        return descriptionOfIssue;
    }

    public void setDescriptionOfIssue(String descriptionOfIssue) {
        this.descriptionOfIssue = descriptionOfIssue;
    }

    public Timestamp getReportDate() {
        return reportDate;
    }

    public void setReportDate(Timestamp reportDate) {
        this.reportDate = reportDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEstimatedCompletionDate() {
        return estimatedCompletionDate;
    }

    public void setEstimatedCompletionDate(Date estimatedCompletionDate) {
        this.estimatedCompletionDate = estimatedCompletionDate;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalMaterialCost() {
        return totalMaterialCost;
    }

    public void setTotalMaterialCost(BigDecimal totalMaterialCost) {
        this.totalMaterialCost = totalMaterialCost;
    }

    public BigDecimal getTotalLaborCost() {
        return totalLaborCost;
    }

    public void setTotalLaborCost(BigDecimal totalLaborCost) {
        this.totalLaborCost = totalLaborCost;
    }

    public BigDecimal getGrandTotalCost() {
        return grandTotalCost;
    }

    public void setGrandTotalCost(BigDecimal grandTotalCost) {
        this.grandTotalCost = grandTotalCost;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<RepairAssignment> getRepairAssignments() {
        return repairAssignments;
    }

    public void setRepairAssignments(List<RepairAssignment> repairAssignments) {
        this.repairAssignments = repairAssignments;
    }

    public List<OrderMaterialUsed> getOrderMaterialsUsed() {
        return orderMaterialsUsed;
    }

    public void setOrderMaterialsUsed(List<OrderMaterialUsed> orderMaterialsUsed) {
        this.orderMaterialsUsed = orderMaterialsUsed;
    }

    public List<RepairOrderFault> getRepairOrderFaults() {
        return repairOrderFaults;
    }

    public void setRepairOrderFaults(List<RepairOrderFault> repairOrderFaults) {
        this.repairOrderFaults = repairOrderFaults;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }
} 