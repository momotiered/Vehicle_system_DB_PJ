package com.vehicle.repair.repository;

import com.vehicle.repair.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUser_UserIdOrderByCreateTimeDesc(Integer userId);
    
    List<Notification> findByUser_UserIdAndIsReadOrderByCreateTimeDesc(Integer userId, boolean isRead);
    
    List<Notification> findByRelatedEntityIdAndRelatedEntityType(Integer entityId, String entityType);
    
    long countByUser_UserIdAndIsRead(Integer userId, boolean isRead);
} 