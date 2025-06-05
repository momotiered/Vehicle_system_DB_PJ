package com.vehicle.repair.repository;

import com.vehicle.repair.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Integer> {
    Optional<Feedback> findByOrder_OrderId(Integer orderId);
    List<Feedback> findByUser_UserId(Integer userId);
    boolean existsByOrder_OrderId(Integer orderId);
} 