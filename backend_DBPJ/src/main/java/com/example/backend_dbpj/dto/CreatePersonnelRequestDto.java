package com.example.backend_dbpj.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreatePersonnelRequestDto {
    private String name;
    private String password;
    private String specialization;
    private BigDecimal hourlyRate;
} 