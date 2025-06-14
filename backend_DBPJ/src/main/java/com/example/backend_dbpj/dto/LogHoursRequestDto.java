package com.example.backend_dbpj.dto;

import java.math.BigDecimal;

public class LogHoursRequestDto {
    private BigDecimal hours;

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }
} 