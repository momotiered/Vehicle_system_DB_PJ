package com.example.backend_dbpj.service;

import com.example.backend_dbpj.dto.UserRegistrationRequest;
import com.example.backend_dbpj.dto.UserResponse;
import com.example.backend_dbpj.dto.LoginRequest;
import com.example.backend_dbpj.dto.CreateUserRequestDto;
import com.example.backend_dbpj.dto.UpdateUserRequestDto;
import com.example.backend_dbpj.entity.User;
import com.example.backend_dbpj.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import javax.naming.AuthenticationException;
import java.sql.Timestamp;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(UserRegistrationRequest registrationRequest) {
        // 遵循简化原则，我们跳过复杂的校验，如用户名/邮箱是否已存在
        // 在实际项目中，这里应该有严格的校验逻辑

        User newUser = new User();
        newUser.setUsername(registrationRequest.getUsername());
        
        // 设置密码相关字段
        newUser.setPassword(registrationRequest.getPassword()); // 明文密码
        newUser.setPasswordHash(registrationRequest.getPassword()); // 简化处理，实际应该加密
        
        newUser.setFullName(registrationRequest.getFullName());
        newUser.setContactPhone(registrationRequest.getContactPhone());
        newUser.setContactEmail(registrationRequest.getContactEmail());
        newUser.setAddress(registrationRequest.getAddress());
        
        // 设置必需的email字段
        if (registrationRequest.getContactEmail() != null && !registrationRequest.getContactEmail().trim().isEmpty()) {
            newUser.setEmail(registrationRequest.getContactEmail());
        } else {
            newUser.setEmail(""); // 设置为空字符串，避免null
        }
        
        // 设置phone_number字段
        newUser.setPhoneNumber(registrationRequest.getContactPhone());
        
        // 设置其他必需字段
        newUser.setRegistrationDate(new Timestamp(System.currentTimeMillis()));
        newUser.setRole("USER"); // 设置默认角色

        return userRepository.save(newUser);
    }

    public UserResponse getUserById(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID " + userId + " not found."));
        return new UserResponse(user);
    }

    public UserResponse login(LoginRequest loginRequest) throws AuthenticationException {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid username or password."));

        // 简化原则：明文密码比对
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            throw new AuthenticationException("Invalid username or password.");
        }

        return new UserResponse(user);
    }

    // --- Admin Management Methods ---

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(CreateUserRequestDto createUserRequestDto) {
        User user = new User();
        user.setUsername(createUserRequestDto.getUsername());
        user.setPassword(createUserRequestDto.getPassword()); // 明文密码
        user.setPasswordHash(createUserRequestDto.getPassword()); // 简化
        user.setContactPhone(createUserRequestDto.getContactInfo());
        user.setAddress(createUserRequestDto.getAddress());
        user.setEmail(createUserRequestDto.getUsername()); // 简化处理，用username作为email
        user.setRegistrationDate(new Timestamp(System.currentTimeMillis()));
        user.setRole("USER");
        return userRepository.save(user);
    }

    public User updateUser(int userId, UpdateUserRequestDto updateUserRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        user.setContactPhone(updateUserRequestDto.getContactInfo());
        user.setAddress(updateUserRequestDto.getAddress());

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        
        // 删除用户相关的所有记录
        // 1. 删除用户的车辆记录（由于设置了CASCADE，会自动删除）
        // 2. 删除用户的维修订单（由于设置了CASCADE，会自动删除）
        // 3. 删除用户的反馈记录（由于设置了CASCADE，会自动删除）
        
        // 最后删除用户本身
        userRepository.delete(user);
    }
} 