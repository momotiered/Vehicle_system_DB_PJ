package com.vehicle.repair.service;

import com.vehicle.repair.entity.User;
import com.vehicle.repair.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查手机号是否已存在
        if (user.getContactPhone() != null && userRepository.existsByContactPhone(user.getContactPhone())) {
            throw new RuntimeException("手机号已被注册");
        }

        // 检查邮箱是否已存在
        if (user.getContactEmail() != null && userRepository.existsByContactEmail(user.getContactEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 加密密码
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        
        return userRepository.save(user);
    }

    public Optional<User> getUserById(Integer userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 更新用户信息
        if (user.getFullName() != null) {
            existingUser.setFullName(user.getFullName());
        }
        if (user.getContactPhone() != null) {
            existingUser.setContactPhone(user.getContactPhone());
        }
        if (user.getContactEmail() != null) {
            existingUser.setContactEmail(user.getContactEmail());
        }
        if (user.getAddress() != null) {
            existingUser.setAddress(user.getAddress());
        }

        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(Integer userId) {
        userRepository.deleteById(userId);
    }
} 