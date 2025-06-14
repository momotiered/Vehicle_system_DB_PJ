package com.example.backend_dbpj.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdatePersonnelRequestDto {
    private String specialization;
    private BigDecimal hourlyRate;
    private String contactInfo;
} 