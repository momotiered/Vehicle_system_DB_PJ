package com.example.backend_dbpj.dto;

import com.example.backend_dbpj.entity.Administrator;
import java.sql.Timestamp;

public class AdministratorResponse {
    private int adminId;
    private String username;
    private String fullName;
    private String email;
    private Timestamp lastLogin;
    private final String role = "ADMIN"; // 添加固定的角色字段

    public AdministratorResponse(Administrator admin) {
        this.adminId = admin.getAdminId();
        this.username = admin.getUsername();
        this.fullName = admin.getFullName();
        this.email = admin.getEmail();
        this.lastLogin = admin.getLastLogin();
    }

    // Getters and Setters
    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getRole() {
        return role;
    }
} 