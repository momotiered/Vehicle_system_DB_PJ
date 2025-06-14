package com.example.backend_dbpj.service;

import com.example.backend_dbpj.dto.CreateFeedbackRequest;
import com.example.backend_dbpj.entity.Feedback;
import com.example.backend_dbpj.entity.RepairOrder;
import com.example.backend_dbpj.entity.User;
import com.example.backend_dbpj.repository.FeedbackRepository;
import com.example.backend_dbpj.repository.RepairOrderRepository;
import com.example.backend_dbpj.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.sql.Timestamp;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RepairOrderRepository repairOrderRepository;

    public Feedback createFeedback(CreateFeedbackRequest request) {
        // 1. 验证用户和工单是否存在
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.getUserId()));

        RepairOrder repairOrder = repairOrderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("RepairOrder not found with id: " + request.getOrderId()));

        // 2. 创建新的 Feedback 实例
        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setRepairOrder(repairOrder);
        feedback.setRatingScore(request.getRatingScore());
        feedback.setComments(request.getComments());

        // 3. 设置提交日期
        feedback.setFeedbackDate(new Timestamp(System.currentTimeMillis()));

        // 4. 保存并返回
        return feedbackRepository.save(feedback);
    }
} 