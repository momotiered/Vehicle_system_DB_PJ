package com.vehicle.repair.controller;

import com.vehicle.repair.entity.User;
import com.vehicle.repair.entity.UserRole;
import com.vehicle.repair.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        String expectedRole = loginData.get("expectedRole");

        // 验证用户名和密码
        Optional<User> userOptional = userService.getUserByUsername(username);
        
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "用户名不存在"));
        }
        
        User user = userOptional.get();
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return ResponseEntity.badRequest().body(Map.of("message", "密码错误"));
        }
        
        // 验证用户角色是否匹配
        if (expectedRole != null && !expectedRole.isEmpty()) {
            try {
                UserRole expectedUserRole = UserRole.valueOf(expectedRole);
                if (!user.getRole().equals(expectedUserRole)) {
                    // 根据实际情况给出具体的提示信息
                    String errorMessage = generateRoleErrorMessage(user.getRole(), expectedUserRole);
                    return ResponseEntity.status(403).body(Map.of("message", errorMessage));
                }
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("message", "无效的用户角色"));
            }
        }
        
        // 登录成功，创建简单的令牌
        String token = "token-" + user.getUserId() + "-" + System.currentTimeMillis();
        
        // 返回用户信息和令牌
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        
        // 移除敏感信息
        user.setPasswordHash(null);
        response.put("user", user);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据用户实际角色和选择的角色生成具体的错误提示信息
     */
    private String generateRoleErrorMessage(UserRole actualRole, UserRole selectedRole) {
        if (actualRole == UserRole.USER && selectedRole == UserRole.TECHNICIAN) {
            return "您注册的身份是【客户】，无法以【维修人员】身份登录。如需成为维修人员，请联系系统管理员进行身份升级。";
        } else if (actualRole == UserRole.USER && selectedRole == UserRole.ADMIN) {
            return "您注册的身份是【客户】，无法以【管理员】身份登录。请选择【客户】身份进行登录。";
        } else if (actualRole == UserRole.TECHNICIAN && selectedRole == UserRole.USER) {
            return "您的身份是【维修人员】，无法以【客户】身份登录。请选择【维修人员】身份进行登录。";
        } else if (actualRole == UserRole.TECHNICIAN && selectedRole == UserRole.ADMIN) {
            return "您的身份是【维修人员】，无法以【管理员】身份登录。请选择【维修人员】身份进行登录。";
        } else if (actualRole == UserRole.ADMIN && selectedRole == UserRole.USER) {
            return "您的身份是【管理员】，无法以【客户】身份登录。请选择【管理员】身份进行登录。";
        } else if (actualRole == UserRole.ADMIN && selectedRole == UserRole.TECHNICIAN) {
            return "您的身份是【管理员】，无法以【维修人员】身份登录。请选择【管理员】身份进行登录。";
        } else {
            return "身份验证失败：您选择的登录身份与账户实际身份不符。请选择正确的身份进行登录。";
        }
    }
} 