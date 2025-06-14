package com.example.backend_dbpj.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "Feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private int feedbackId;

    @OneToOne
    @JoinColumn(name = "order_id", referencedColumnName = "order_id", unique = true)
    @JsonManagedReference("feedback-repairOrder")
    private RepairOrder repairOrder;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonManagedReference("feedback-user")
    private User user;

    @Column(name = "rating_score")
    private Integer ratingScore;

    private String comments;

    @Column(name = "feedback_date")
    private Timestamp feedbackDate;


    // Getters and Setters

    public int getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(int feedbackId) {
        this.feedbackId = feedbackId;
    }

    public RepairOrder getRepairOrder() {
        return repairOrder;
    }

    public void setRepairOrder(RepairOrder repairOrder) {
        this.repairOrder = repairOrder;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getRatingScore() {
        return ratingScore;
    }

    public void setRatingScore(Integer ratingScore) {
        this.ratingScore = ratingScore;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Timestamp getFeedbackDate() {
        return feedbackDate;
    }

    public void setFeedbackDate(Timestamp feedbackDate) {
        this.feedbackDate = feedbackDate;
    }
} 