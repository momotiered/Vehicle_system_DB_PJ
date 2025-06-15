package com.example.backend_dbpj.repository;

import com.example.backend_dbpj.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    
    // 计算平均评分
    @Query("SELECT AVG(f.ratingScore) FROM Feedback f WHERE f.feedbackDate >= :startDate AND f.feedbackDate < :endDate")
    Double getAverageRatingInDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // 统计指定时间范围内的反馈总数
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.feedbackDate >= :startDate AND f.feedbackDate < :endDate")
    Long countFeedbackInDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // 按评分分组统计数量
    @Query("SELECT f.ratingScore, COUNT(f) FROM Feedback f WHERE f.feedbackDate >= :startDate AND f.feedbackDate < :endDate GROUP BY f.ratingScore")
    List<Object[]> getRatingDistributionInDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // 获取指定时间范围内的所有反馈
    @Query("SELECT f FROM Feedback f WHERE f.feedbackDate >= :startDate AND f.feedbackDate < :endDate ORDER BY f.feedbackDate DESC")
    List<Feedback> findFeedbackInDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    // 计算总平均评分
    @Query("SELECT AVG(f.ratingScore) FROM Feedback f")
    Double getOverallAverageRating();
    
    // 统计各评分的数量
    @Query("SELECT f.ratingScore, COUNT(f) FROM Feedback f GROUP BY f.ratingScore ORDER BY f.ratingScore")
    List<Object[]> getRatingDistribution();
} 