package com.example.backend_dbpj.service;

import com.example.backend_dbpj.dto.AdministratorResponse;
import com.example.backend_dbpj.dto.LoginRequest;
import com.example.backend_dbpj.entity.Administrator;
import com.example.backend_dbpj.repository.AdministratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;

@Service
public class AdministratorService {

    @Autowired
    private AdministratorRepository administratorRepository;

    public AdministratorResponse login(LoginRequest loginRequest) throws AuthenticationException {
        Administrator admin = administratorRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password."));

        // 简化原则：明文密码比对
        if (!admin.getPassword().equals(loginRequest.getPassword())) {
            throw new AuthenticationException("Invalid username or password.");
        }

        return new AdministratorResponse(admin);
    }
} 