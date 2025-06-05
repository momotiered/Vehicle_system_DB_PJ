package com.vehicle.repair.service;

import com.vehicle.repair.entity.Feedback;
import com.vehicle.repair.entity.RepairOrder;
import com.vehicle.repair.entity.User;
import com.vehicle.repair.repository.FeedbackRepository;
import com.vehicle.repair.repository.RepairOrderRepository;
import com.vehicle.repair.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FeedbackService {
    @Autowired
    private FeedbackRepository feedbackRepository;
    
    @Autowired
    private RepairOrderRepository repairOrderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public Feedback createFeedback(Feedback feedback, Integer orderId, Integer userId) {
        // 验证维修工单存在
        RepairOrder order = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("维修工单不存在"));
        
        // 验证用户存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查是否已经有评价
        if (feedbackRepository.existsByOrder_OrderId(orderId)) {
            throw new RuntimeException("此维修工单已有评价");
        }
        
        feedback.setOrder(order);
        feedback.setUser(user);
        return feedbackRepository.save(feedback);
    }
    
    public Optional<Feedback> getFeedbackById(Integer feedbackId) {
        return feedbackRepository.findById(feedbackId);
    }
    
    public Optional<Feedback> getFeedbackByOrderId(Integer orderId) {
        return feedbackRepository.findByOrder_OrderId(orderId);
    }
    
    public List<Feedback> getFeedbacksByUserId(Integer userId) {
        return feedbackRepository.findByUser_UserId(userId);
    }
    
    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }
    
    /**
     * 计算总体平均评分
     */
    public Map<String, Object> calculateAverageRating() {
        List<Feedback> allFeedbacks = feedbackRepository.findAll();
        
        if (allFeedbacks.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("averageRating", 0.0);
            result.put("totalFeedbacks", 0);
            return result;
        }
        
        double averageRating = allFeedbacks.stream()
                .mapToInt(Feedback::getRatingScore)
                .average()
                .orElse(0.0);
        
        Map<String, Object> result = new HashMap<>();
        result.put("averageRating", Math.round(averageRating * 10.0) / 10.0); // 保留一位小数
        result.put("totalFeedbacks", allFeedbacks.size());
        
        return result;
    }
    
    /**
     * 计算评分分布
     */
    public Map<Integer, Long> calculateRatingDistribution() {
        List<Feedback> allFeedbacks = feedbackRepository.findAll();
        
        // 初始化评分分布表 (1-5分)
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }
        
        // 统计各评分的数量
        Map<Integer, Long> actualDistribution = allFeedbacks.stream()
                .collect(Collectors.groupingBy(Feedback::getRatingScore, Collectors.counting()));
        
        // 合并两个Map，确保所有评分都有统计值
        distribution.putAll(actualDistribution);
        
        return distribution;
    }
    
    /**
     * 计算特定技术人员的评分
     */
    public Map<String, Object> calculateTechnicianRating(Integer technicianId) {
        // TODO: 待实现技术人员与工单的关联后完善此方法
        
        Map<String, Object> result = new HashMap<>();
        result.put("technicianId", technicianId);
        result.put("averageRating", 0.0);
        result.put("totalFeedbacks", 0);
        result.put("message", "技术人员评分功能尚未实现");
        
        return result;
    }
    
    @Transactional
    public Feedback updateFeedback(Integer feedbackId, Feedback feedback) {
        Feedback existingFeedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("评价不存在"));
        
        // 只更新评分和评价内容
        existingFeedback.setRatingScore(feedback.getRatingScore());
        existingFeedback.setComments(feedback.getComments());
        
        return feedbackRepository.save(existingFeedback);
    }
    
    @Transactional
    public void deleteFeedback(Integer feedbackId) {
        if (!feedbackRepository.existsById(feedbackId)) {
            throw new RuntimeException("评价不存在");
        }
        feedbackRepository.deleteById(feedbackId);
    }
} 