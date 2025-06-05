package com.vehicle.repair.service;

import com.vehicle.repair.entity.SkillType;
import com.vehicle.repair.entity.Technician;
import com.vehicle.repair.entity.User;
import com.vehicle.repair.entity.UserRole;
import com.vehicle.repair.repository.TechnicianRepository;
import com.vehicle.repair.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TechnicianService {

    @Autowired
    private TechnicianRepository technicianRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WorkOrderAssignmentService assignmentService;
    
    /**
     * 获取所有技术人员
     */
    public List<Technician> getAllTechnicians() {
        return technicianRepository.findAll();
    }
    
    /**
     * 根据ID获取技术人员
     */
    public Optional<Technician> getTechnicianById(Integer technicianId) {
        return technicianRepository.findById(technicianId);
    }
    
    /**
     * 根据用户ID获取技术人员
     */
    public Optional<Technician> getTechnicianByUserId(Integer userId) {
        return technicianRepository.findByUser_UserId(userId);
    }
    
    /**
     * 根据技能类型获取技术人员
     */
    public List<Technician> getTechniciansBySkill(SkillType skillType) {
        return technicianRepository.findBySkillType(skillType);
    }
    
    /**
     * 获取可用的技术人员
     */
    public List<Technician> getAvailableTechnicians() {
        return technicianRepository.findAvailableTechnicians();
    }
    
    /**
     * 根据技能获取可用的技术人员
     */
    public List<Technician> getAvailableTechniciansBySkill(SkillType skillType) {
        return technicianRepository.findAvailableTechniciansBySkill(skillType);
    }
    
    /**
     * 创建技术人员
     */
    @Transactional
    public Technician createTechnician(Technician technician, Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 将用户角色设为技术人员
        user.setRole(UserRole.TECHNICIAN);
        userRepository.save(user);
        
        technician.setUser(user);
        return technicianRepository.save(technician);
    }
    
    /**
     * 更新技术人员信息
     */
    @Transactional
    public Technician updateTechnician(Technician technician) {
        Technician existingTechnician = technicianRepository.findById(technician.getTechnicianId())
                .orElseThrow(() -> new RuntimeException("技术人员不存在"));
        
        // 更新信息
        if (technician.getSkillType() != null) {
            existingTechnician.setSkillType(technician.getSkillType());
        }
        if (technician.getHourlyRate() != null) {
            existingTechnician.setHourlyRate(technician.getHourlyRate());
        }
        if (technician.getYearsOfExperience() != null) {
            existingTechnician.setYearsOfExperience(technician.getYearsOfExperience());
        }
        if (technician.getCertifications() != null) {
            existingTechnician.setCertifications(technician.getCertifications());
        }
        if (technician.getIsAvailable() != null) {
            existingTechnician.setIsAvailable(technician.getIsAvailable());
        }
        if (technician.getMaxWorkOrdersPerDay() != null) {
            existingTechnician.setMaxWorkOrdersPerDay(technician.getMaxWorkOrdersPerDay());
        }
        
        return technicianRepository.save(existingTechnician);
    }
    
    /**
     * 更新技术人员的工单计数
     */
    @Transactional
    public void updateWorkOrderCount(Integer technicianId) {
        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("技术人员不存在"));
        
        int activeOrderCount = assignmentService.countActiveTechnicianAssignments(technicianId);
        technician.setCurrentWorkOrders(activeOrderCount);
        
        technicianRepository.save(technician);
    }
    
    /**
     * 删除技术人员
     */
    @Transactional
    public void deleteTechnician(Integer technicianId) {
        Technician technician = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("技术人员不存在"));
        
        // 重置用户角色
        User user = technician.getUser();
        user.setRole(UserRole.USER);
        userRepository.save(user);
        
        technicianRepository.delete(technician);
    }
} 