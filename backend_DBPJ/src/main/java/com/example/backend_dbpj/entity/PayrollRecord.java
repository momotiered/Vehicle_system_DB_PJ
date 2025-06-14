package com.example.backend_dbpj.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;

@Entity(name = "PayrollRecords")
public class PayrollRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payroll_record_id")
    private int payrollRecordId;

    @ManyToOne
    @JoinColumn(name = "personnel_id", nullable = false)
    private RepairPersonnel repairPersonnel;

    @Column(name = "payment_date")
    private Date paymentDate;

    @Column(name = "period_start_date")
    private Date periodStartDate;

    @Column(name = "period_end_date")
    private Date periodEndDate;

    @Column(name = "total_hours_worked")
    private BigDecimal totalHoursWorked;

    @Column(name = "total_amount_paid")
    private BigDecimal totalAmountPaid;

    private String notes;


    // Getters and Setters

    public int getPayrollRecordId() {
        return payrollRecordId;
    }

    public void setPayrollRecordId(int payrollRecordId) {
        this.payrollRecordId = payrollRecordId;
    }

    public RepairPersonnel getRepairPersonnel() {
        return repairPersonnel;
    }

    public void setRepairPersonnel(RepairPersonnel repairPersonnel) {
        this.repairPersonnel = repairPersonnel;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Date getPeriodStartDate() {
        return periodStartDate;
    }

    public void setPeriodStartDate(Date periodStartDate) {
        this.periodStartDate = periodStartDate;
    }

    public Date getPeriodEndDate() {
        return periodEndDate;
    }

    public void setPeriodEndDate(Date periodEndDate) {
        this.periodEndDate = periodEndDate;
    }

    public BigDecimal getTotalHoursWorked() {
        return totalHoursWorked;
    }

    public void setTotalHoursWorked(BigDecimal totalHoursWorked) {
        this.totalHoursWorked = totalHoursWorked;
    }

    public BigDecimal getTotalAmountPaid() {
        return totalAmountPaid;
    }

    public void setTotalAmountPaid(BigDecimal totalAmountPaid) {
        this.totalAmountPaid = totalAmountPaid;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
} 