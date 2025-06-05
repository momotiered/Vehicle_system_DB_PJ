package com.vehicle.repair.controller;

import com.vehicle.repair.dto.FeedbackRequestDTO;
import com.vehicle.repair.entity.Feedback;
import com.vehicle.repair.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {
    @Autowired
    private FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<Feedback> createFeedback(@RequestBody FeedbackRequestDTO request) {
        if (request.getRatingScore() == null || request.getRatingScore() < 1 || request.getRatingScore() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "评分必须在1-5之间");
        }
        
        if (request.getComments() == null || request.getComments().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "评价内容不能为空");
        }
        
        Feedback feedback = new Feedback();
        feedback.setRatingScore(request.getRatingScore());
        feedback.setComments(request.getComments());
        
        return ResponseEntity.ok(feedbackService.createFeedback(feedback, request.getOrderId(), request.getUserId()));
    }

    @GetMapping("/{feedbackId}")
    public ResponseEntity<Feedback> getFeedbackById(@PathVariable Integer feedbackId) {
        return feedbackService.getFeedbackById(feedbackId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Feedback> getFeedbackByOrderId(@PathVariable Integer orderId) {
        return feedbackService.getFeedbackByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Feedback>> getFeedbacksByUserId(@PathVariable Integer userId) {
        return ResponseEntity.ok(feedbackService.getFeedbacksByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<List<Feedback>> getAllFeedbacks() {
        return ResponseEntity.ok(feedbackService.getAllFeedbacks());
    }
    
    @GetMapping("/stats/average-rating")
    public ResponseEntity<Map<String, Object>> getAverageRating() {
        return ResponseEntity.ok(feedbackService.calculateAverageRating());
    }
    
    @GetMapping("/stats/rating-distribution")
    public ResponseEntity<Map<Integer, Long>> getRatingDistribution() {
        return ResponseEntity.ok(feedbackService.calculateRatingDistribution());
    }
    
    @GetMapping("/stats/technician/{technicianId}/rating")
    public ResponseEntity<Map<String, Object>> getTechnicianRating(@PathVariable Integer technicianId) {
        return ResponseEntity.ok(feedbackService.calculateTechnicianRating(technicianId));
    }

    @PutMapping("/{feedbackId}")
    public ResponseEntity<Feedback> updateFeedback(
            @PathVariable Integer feedbackId,
            @RequestBody Feedback feedback) {
        return ResponseEntity.ok(feedbackService.updateFeedback(feedbackId, feedback));
    }

    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Integer feedbackId) {
        feedbackService.deleteFeedback(feedbackId);
        return ResponseEntity.ok().build();
    }
} 