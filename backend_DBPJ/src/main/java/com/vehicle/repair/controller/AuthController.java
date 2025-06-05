package com.vehicle.repair.controller;

import com.vehicle.repair.entity.User;
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

        // 验证用户名和密码
        Optional<User> userOptional = userService.getUserByUsername(username);
        
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "用户名不存在"));
        }
        
        User user = userOptional.get();
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return ResponseEntity.badRequest().body(Map.of("message", "密码错误"));
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
} 