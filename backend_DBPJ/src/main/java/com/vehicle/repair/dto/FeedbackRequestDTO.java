package com.vehicle.repair.dto;

import lombok.Data;

@Data
public class FeedbackRequestDTO {
    private Integer orderId;
    private Integer userId;
    private Integer ratingScore;
    private String comments;
} 