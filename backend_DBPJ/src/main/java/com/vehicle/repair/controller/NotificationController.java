package com.vehicle.repair.controller;

import com.vehicle.repair.entity.Notification;
import com.vehicle.repair.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    
    /**
     * 获取用户所有通知
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Integer userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }
    
    /**
     * 获取用户未读通知
     */
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUserUnreadNotifications(@PathVariable Integer userId) {
        return ResponseEntity.ok(notificationService.getUserUnreadNotifications(userId));
    }
    
    /**
     * 获取用户已读通知
     */
    @GetMapping("/user/{userId}/read")
    public ResponseEntity<List<Notification>> getUserReadNotifications(@PathVariable Integer userId) {
        return ResponseEntity.ok(notificationService.getUserReadNotifications(userId));
    }
    
    /**
     * 获取未读通知数量
     */
    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<Map<String, Long>> countUnreadNotifications(@PathVariable Integer userId) {
        Map<String, Long> result = new HashMap<>();
        result.put("count", notificationService.countUnreadNotifications(userId));
        return ResponseEntity.ok(result);
    }
    
    /**
     * 标记通知为已读
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Integer notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }
    
    /**
     * 标记所有通知为已读
     */
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Integer userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 创建系统消息
     */
    @PostMapping("/system-message")
    public ResponseEntity<Notification> createSystemMessage(@RequestBody Map<String, Object> request) {
        Integer userId = (Integer) request.get("userId");
        String title = (String) request.get("title");
        String content = (String) request.get("content");
        
        return ResponseEntity.ok(notificationService.createSystemMessage(userId, title, content));
    }
} 