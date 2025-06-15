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
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RepairOrderRepository repairOrderRepository;

    @Transactional
    public Feedback createFeedback(CreateFeedbackRequest request) {
        // 1. 验证请求参数
        if (request.getUserId() == null || request.getOrderId() == null) {
            throw new IllegalArgumentException("用户ID和订单ID不能为空");
        }
        
        if (request.getRatingScore() == null || request.getRatingScore() < 1 || request.getRatingScore() > 5) {
            throw new IllegalArgumentException("评分应在1-5之间");
        }

        // 2. 验证用户和工单是否存在
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.getUserId()));

        RepairOrder repairOrder = repairOrderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("RepairOrder not found with id: " + request.getOrderId()));

        // 3. 检查该订单是否已有反馈记录
        if (repairOrder.getFeedback() != null) {
            throw new IllegalStateException("该订单已存在评价记录");
        }

        // 4. 验证订单状态（只有已完成的订单才能评价）
        if (!"COMPLETED".equals(repairOrder.getStatus().toString())) {
            throw new IllegalStateException("只有已完成的订单才能进行评价");
        }

        // 5. 创建新的 Feedback 实例
        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setRepairOrder(repairOrder);
        feedback.setRatingScore(request.getRatingScore());
        feedback.setComments(request.getComments());

        // 6. 设置提交日期
        feedback.setFeedbackDate(new Timestamp(System.currentTimeMillis()));

        // 7. 保存并返回
        return feedbackRepository.save(feedback);
    }

    /**
     * 获取满意度统计数据
     * @return 满意度统计结果
     */
    public Map<String, Object> getSatisfactionStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 计算本月的时间范围
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1);
        
        Date startDate = Date.valueOf(startOfMonth);
        Date endDate = Date.valueOf(endOfMonth);
        
        // 本月平均评分
        Double avgRating = feedbackRepository.getAverageRatingInDateRange(startDate, endDate);
        if (avgRating != null) {
            // 保留一位小数
            avgRating = BigDecimal.valueOf(avgRating)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        
        // 本月反馈总数
        Long totalFeedback = feedbackRepository.countFeedbackInDateRange(startDate, endDate);
        
        // 本月评分分布
        List<Object[]> ratingDistribution = feedbackRepository.getRatingDistributionInDateRange(startDate, endDate);
        Map<Integer, Long> ratingDistributionMap = new HashMap<>();
        
        // 初始化1-5星的数量为0
        for (int i = 1; i <= 5; i++) {
            ratingDistributionMap.put(i, 0L);
        }
        
        // 填充实际数据
        for (Object[] row : ratingDistribution) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            if (rating != null && rating >= 1 && rating <= 5) {
                ratingDistributionMap.put(rating, count);
            }
        }
        
        // 计算总体满意度（总平均评分）
        Double overallAvgRating = feedbackRepository.getOverallAverageRating();
        if (overallAvgRating != null) {
            overallAvgRating = BigDecimal.valueOf(overallAvgRating)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        
        // 总体评分分布
        List<Object[]> overallRatingDistribution = feedbackRepository.getRatingDistribution();
        Map<Integer, Long> overallRatingDistributionMap = new HashMap<>();
        
        // 初始化1-5星的数量为0
        for (int i = 1; i <= 5; i++) {
            overallRatingDistributionMap.put(i, 0L);
        }
        
        // 填充实际数据
        for (Object[] row : overallRatingDistribution) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            if (rating != null && rating >= 1 && rating <= 5) {
                overallRatingDistributionMap.put(rating, count);
            }
        }
        
        // 计算满意度等级
        String satisfactionLevel = getSatisfactionLevel(avgRating);
        
        // 构建返回结果
        stats.put("avgRating", avgRating != null ? avgRating : 0.0);
        stats.put("totalFeedback", totalFeedback != null ? totalFeedback : 0L);
        stats.put("ratingDistribution", ratingDistributionMap);
        stats.put("overallAvgRating", overallAvgRating != null ? overallAvgRating : 0.0);
        stats.put("overallRatingDistribution", overallRatingDistributionMap);
        stats.put("satisfactionLevel", satisfactionLevel);
        stats.put("month", now.getMonthValue());
        stats.put("year", now.getYear());
        
        return stats;
    }
    
    /**
     * 根据平均评分计算满意度等级
     * @param avgRating 平均评分
     * @return 满意度等级
     */
    private String getSatisfactionLevel(Double avgRating) {
        if (avgRating == null || avgRating == 0.0) {
            return "暂无数据";
        } else if (avgRating >= 4.5) {
            return "非常满意";
        } else if (avgRating >= 4.0) {
            return "满意";
        } else if (avgRating >= 3.0) {
            return "一般";
        } else if (avgRating >= 2.0) {
            return "不满意";
        } else {
            return "非常不满意";
        }
    }
    
    /**
     * 获取最近的反馈列表
     * @param limit 限制数量
     * @return 反馈列表
     */
    public List<Feedback> getRecentFeedback(int limit) {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusDays(30); // 最近30天
        Date sqlStartDate = Date.valueOf(startDate);
        Date sqlEndDate = Date.valueOf(now.plusDays(1));
        
        List<Feedback> feedbacks = feedbackRepository.findFeedbackInDateRange(sqlStartDate, sqlEndDate);
        
        // 如果需要限制数量
        if (feedbacks.size() > limit) {
            return feedbacks.subList(0, limit);
        }
        
        return feedbacks;
    }
} 