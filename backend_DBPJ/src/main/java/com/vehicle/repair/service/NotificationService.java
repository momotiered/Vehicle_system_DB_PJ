package com.vehicle.repair.service;

import com.vehicle.repair.entity.*;
import com.vehicle.repair.repository.NotificationRepository;
import com.vehicle.repair.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 获取用户所有通知
     */
    public List<Notification> getUserNotifications(Integer userId) {
        return notificationRepository.findByUser_UserIdOrderByCreateTimeDesc(userId);
    }
    
    /**
     * 获取用户未读通知
     */
    public List<Notification> getUserUnreadNotifications(Integer userId) {
        return notificationRepository.findByUser_UserIdAndIsReadOrderByCreateTimeDesc(userId, false);
    }
    
    /**
     * 获取用户已读通知
     */
    public List<Notification> getUserReadNotifications(Integer userId) {
        return notificationRepository.findByUser_UserIdAndIsReadOrderByCreateTimeDesc(userId, true);
    }
    
    /**
     * 获取未读通知数量
     */
    public long countUnreadNotifications(Integer userId) {
        return notificationRepository.countByUser_UserIdAndIsRead(userId, false);
    }
    
    /**
     * 标记通知为已读
     */
    @Transactional
    public Notification markAsRead(Integer notificationId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (!optionalNotification.isPresent()) {
            throw new RuntimeException("通知不存在");
        }
        
        Notification notification = optionalNotification.get();
        notification.setRead(true);
        notification.setReadTime(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }
    
    /**
     * 标记所有通知为已读
     */
    @Transactional
    public void markAllAsRead(Integer userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUser_UserIdAndIsReadOrderByCreateTimeDesc(userId, false);
        
        LocalDateTime now = LocalDateTime.now();
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadTime(now);
        }
        
        if (!unreadNotifications.isEmpty()) {
            notificationRepository.saveAll(unreadNotifications);
        }
    }
    
    /**
     * 创建状态变更通知
     */
    @Transactional
    public Notification createStatusChangeNotification(Integer userId, Integer orderId, 
                                                      RepairOrderStatus oldStatus, 
                                                      RepairOrderStatus newStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        String title = "维修工单状态变更";
        String content = String.format("您的维修工单 #%d 状态已从 %s 变更为 %s", 
                orderId, getStatusText(oldStatus), getStatusText(newStatus));
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(NotificationType.STATUS_CHANGE);
        notification.setRelatedEntityId(orderId);
        notification.setRelatedEntityType("RepairOrder");
        
        return notificationRepository.save(notification);
    }
    
    /**
     * 创建维修完成通知
     */
    @Transactional
    public Notification createCompletionNotification(Integer userId, Integer orderId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        String title = "维修工单已完成";
        String content = String.format("您的维修工单 #%d 已完成维修，请查看详情并评价", orderId);
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(NotificationType.COMPLETED_REPAIR);
        notification.setRelatedEntityId(orderId);
        notification.setRelatedEntityType("RepairOrder");
        
        return notificationRepository.save(notification);
    }
    
    /**
     * 创建新工单通知
     */
    @Transactional
    public Notification createNewOrderNotification(Integer userId, Integer orderId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        String title = "新维修工单创建成功";
        String content = String.format("您的维修工单 #%d 已创建成功，我们会尽快处理", orderId);
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(NotificationType.NEW_REPAIR_ORDER);
        notification.setRelatedEntityId(orderId);
        notification.setRelatedEntityType("RepairOrder");
        
        return notificationRepository.save(notification);
    }
    
    /**
     * 创建系统消息
     */
    @Transactional
    public Notification createSystemMessage(Integer userId, String title, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(NotificationType.SYSTEM_MESSAGE);
        
        return notificationRepository.save(notification);
    }
    
    /**
     * 获取状态的中文描述
     */
    private String getStatusText(RepairOrderStatus status) {
        if (status == null) {
            return "未定义";
        }
        
        switch (status) {
            case PENDING_ASSIGNMENT: return "待分配";
            case ASSIGNED: return "已分配";
            case IN_PROGRESS: return "维修中";
            case AWAITING_PARTS: return "等待配件";
            case COMPLETED: return "已完成";
            case CANCELLED_BY_USER: return "用户取消";
            case CANNOT_REPAIR: return "无法维修";
            case PENDING_USER_CONFIRMATION: return "待用户确认";
            default: return status.name();
        }
    }
} 