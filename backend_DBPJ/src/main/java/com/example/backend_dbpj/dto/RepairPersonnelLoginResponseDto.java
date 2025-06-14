package com.example.backend_dbpj.dto;

public class RepairPersonnelLoginResponseDto {
    private int personnelId;
    private String username;
    private String fullName;
    private String message;

    public RepairPersonnelLoginResponseDto(int personnelId, String username, String fullName, String message) {
        this.personnelId = personnelId;
        this.username = username;
        this.fullName = fullName;
        this.message = message;
    }

    // Getters and Setters
    public int getPersonnelId() {
        return personnelId;
    }

    public void setPersonnelId(int personnelId) {
        this.personnelId = personnelId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
} 