package com.example.backend_dbpj.dto;

import lombok.Data;

@Data
public class CreateUserRequestDto {
    private String username;
    private String password;
    private String contactInfo;
    private String address;
} 