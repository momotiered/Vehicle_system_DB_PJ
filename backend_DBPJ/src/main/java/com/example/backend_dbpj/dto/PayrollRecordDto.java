package com.example.backend_dbpj.dto;

import com.example.backend_dbpj.entity.PayrollRecord;

import java.math.BigDecimal;
import java.sql.Date;

public class PayrollRecordDto {
    private int payrollRecordId;
    private Date paymentDate;
    private Date periodStartDate;
    private Date periodEndDate;
    private BigDecimal totalHoursWorked;
    private BigDecimal totalAmountPaid;
    private String notes;

    public PayrollRecordDto(PayrollRecord record) {
        this.payrollRecordId = record.getPayrollRecordId();
        this.paymentDate = record.getPaymentDate();
        this.periodStartDate = record.getPeriodStartDate();
        this.periodEndDate = record.getPeriodEndDate();
        this.totalHoursWorked = record.getTotalHoursWorked();
        this.totalAmountPaid = record.getTotalAmountPaid();
        this.notes = record.getNotes();
    }

    // Getters and Setters
    public int getPayrollRecordId() {
        return payrollRecordId;
    }

    public void setPayrollRecordId(int payrollRecordId) {
        this.payrollRecordId = payrollRecordId;
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